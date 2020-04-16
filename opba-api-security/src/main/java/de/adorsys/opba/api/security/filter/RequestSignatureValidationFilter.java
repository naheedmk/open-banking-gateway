package de.adorsys.opba.api.security.filter;


import de.adorsys.opba.api.security.domain.HttpHeaders;
import de.adorsys.opba.api.security.domain.SignData;
import de.adorsys.opba.api.security.service.RequestVerifyingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class RequestSignatureValidationFilter extends OncePerRequestFilter {
    private final RequestVerifyingService requestVerifyingService;
    private final Duration requestTimeLimit;
    private final Environment environment;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String fintechId = request.getHeader(HttpHeaders.FINTECH_ID);
        String xRequestId = request.getHeader(HttpHeaders.X_REQUEST_ID);
        String requestTimeStamp = request.getHeader(HttpHeaders.X_TIMESTAMP_UTC);
        String xRequestSignature = request.getHeader(HttpHeaders.X_REQUEST_SIGNATURE);

        OffsetDateTime dateTime = OffsetDateTime.parse(requestTimeStamp);

        String fintechApiKey = environment.getProperty(fintechId);

        if (fintechApiKey == null) {
            log.warn("Api key for fintech ID {} has not find ", fintechId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Wrong Fintech ID");
            return;
        }

        SignData signData = new SignData(UUID.fromString(xRequestId), dateTime);

        boolean verificationResult = requestVerifyingService.verify(xRequestSignature, fintechApiKey, signData);

        if (!verificationResult) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Signature verification error");
            return;
        }

        if (operationDateTimeNowWithinLimit(dateTime)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Timestamp validation failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean operationDateTimeNowWithinLimit(OffsetDateTime dateTime) {
        return OffsetDateTime.now().plus(requestTimeLimit).isBefore(dateTime)
                       || OffsetDateTime.now().minus(requestTimeLimit).isAfter(dateTime);
    }

}