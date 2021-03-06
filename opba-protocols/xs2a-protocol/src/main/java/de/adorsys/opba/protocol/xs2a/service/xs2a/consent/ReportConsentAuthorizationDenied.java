package de.adorsys.opba.protocol.xs2a.service.xs2a.consent;

import de.adorsys.opba.protocol.bpmnshared.dto.messages.ConsentAcquired;
import de.adorsys.opba.protocol.bpmnshared.service.exec.ValidatedExecution;
import de.adorsys.opba.protocol.xs2a.context.Xs2aContext;
import de.adorsys.opba.protocol.xs2a.service.xs2a.Xs2aRedirectExecutor;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service("xs2aReportToFintechConsentDenied")
@RequiredArgsConstructor
public class ReportConsentAuthorizationDenied extends ValidatedExecution<Xs2aContext> {

    private final Xs2aRedirectExecutor redirectExecutor;

    @Override
    protected void doRealExecution(DelegateExecution execution, Xs2aContext context) {
        redirectExecutor.redirect(
            execution,
            context,
            context.getFintechRedirectUriNok(),
            context.getFintechRedirectUriNok(),
            redirect -> new ConsentAcquired(redirect.build()));
    }
}
