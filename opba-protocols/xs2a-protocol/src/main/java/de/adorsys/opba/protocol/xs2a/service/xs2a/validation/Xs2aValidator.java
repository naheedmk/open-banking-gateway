package de.adorsys.opba.protocol.xs2a.service.xs2a.validation;

import com.google.common.collect.Iterables;
import de.adorsys.opba.protocol.api.dto.ValidationIssue;
import de.adorsys.opba.protocol.bpmnshared.dto.context.BaseContext;
import de.adorsys.opba.protocol.bpmnshared.service.context.ContextUtil;
import de.adorsys.opba.protocol.xs2a.domain.ValidationIssueException;
import de.adorsys.opba.protocol.xs2a.service.xs2a.annotations.ValidationInfo;
import de.adorsys.opba.protocol.xs2a.service.xs2a.context.BaseContext;
import de.adorsys.opba.protocol.xs2a.service.xs2a.context.Xs2aContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.adorsys.opba.protocol.api.common.Approach.EMBEDDED;
import static de.adorsys.opba.protocol.api.common.Approach.REDIRECT;
import static de.adorsys.opba.protocol.bpmnshared.dto.context.ContextMode.REAL_CALLS;


/**
 * Key validation service that uses Hibernate-validator to check that required parameters are available before doing
 * ASPSP API call.
 * For {@link de.adorsys.opba.protocol.bpmnshared.dto.context.ContextMode#MOCK_REAL_CALLS} collects
 * all violations into the context to emit message that requires user to provide inputs that fix the violations.
 * For {@link de.adorsys.opba.protocol.bpmnshared.dto.context.ContextMode#REAL_CALLS} causes Runtime error
 * if API object fails the validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aValidator {

    private final Validator validator;

    /**
     * Validates that all parameters necessary to perform ASPSP API call is present.
     * In {@link de.adorsys.opba.protocol.bpmnshared.dto.context.ContextMode#MOCK_REAL_CALLS}
     * reports all violations into {@link BaseContext#getViolations()} (merging with already existing ones)
     * @param exec Current execution that will be updated with violations if present.
     * @param dtosToValidate ASPSP API call parameter objects to validate.
     */
    public void validate(DelegateExecution exec, Xs2aContext context, Object... dtosToValidate) {
        Set<ConstraintViolation<Object>> allErrors = new HashSet<>();

        Set<String> fieldsToIgnore = getFieldsToIgnoreValidate(context);

        for (Object value : dtosToValidate) {
            Set<ConstraintViolation<Object>> errors = validator.validate(value)
                    .stream()
                    .filter(f -> fieldsToIgnore.contains(findInfoOnViolation(f).ctx().value().name()))
                    .collect(Collectors.toSet());
            allErrors.addAll(errors);
        }

        if (allErrors.isEmpty()) {
            return;
        }

        ContextUtil.getAndUpdateContext(
                exec,
                (BaseContext ctx) -> {
                    ctx.getViolations().addAll(allErrors.stream().map(this::toIssue).collect(Collectors.toSet()));
                    // Only when doing real calls validations cause termination of flow
                    // TODO: Those validation in real call should be propagated and handled
                    if (REAL_CALLS == ctx.getMode()) {
                        log.error("Fatal validation error for requestId={},sagaId={} - violations {}", ctx.getRequestId(), ctx.getSagaId(), allErrors);
                        throw new ValidationIssueException();
                    }
                }
        );
    }

    private Set<String> getFieldsToIgnoreValidate(Xs2aContext context) {
        String approach = context.getAspspScaApproach();
        return context.getRequestScoped().getValidationRules().stream()
                .filter(it -> !it.getEndpointClass().equals(context.getClassName()))
                .filter(it -> it.isForEmbedded() && !EMBEDDED.name().equalsIgnoreCase(approach))
                .filter(it -> it.isForRedirect() && !REDIRECT.name().equalsIgnoreCase(approach))
                .map(bankValidationRuleDto -> bankValidationRuleDto.getValidationCode().toUpperCase())
                .collect(Collectors.toSet());
    }

    private ValidationIssue toIssue(ConstraintViolation<Object> violation) {
        ValidationInfo info = findInfoOnViolation(violation);
        return ValidationIssue.builder()
                .type(info.ui().value())
                .scope(info.ctx().target())
                .code(info.ctx().value())
                .captionMessage(violation.getMessage())
                .build();
    }

    @SneakyThrows
    private ValidationInfo findInfoOnViolation(ConstraintViolation<Object> violation) {
        String name = Iterables.getLast(violation.getPropertyPath()).getName();
        Field fieldValue = ReflectionUtils.findField(violation.getLeafBean().getClass(), name);

        if (null == fieldValue) {
            throw new IllegalStateException("Validated field not found " + name);
        }

        if (!fieldValue.isAnnotationPresent(ValidationInfo.class)) {
            throw new IllegalStateException("Field " + name + " not annotated with @ValidationInfo");
        }

        return fieldValue.getAnnotationsByType(ValidationInfo.class)[0];
    }
}
