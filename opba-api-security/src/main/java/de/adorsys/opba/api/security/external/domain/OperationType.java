package de.adorsys.opba.api.security.external.domain;

import org.springframework.http.HttpMethod;

public enum OperationType {
    AIS,
    PIS,
    BANK_SEARCH,
    CONFIRM_CONSENT,
    CONFIRM_PAYMENT;

    public static boolean isTransactionsPath(String path) {
        return path.contains("/transactions");
    }

    public static boolean isBankSearchPath(String path) {
        return path.contains("/bank-search");
    }

    public static boolean isGetPaymentStatus(String path) {
        return path.contains("/status");
    }

    public static boolean isGetPayment(String method) {
        return HttpMethod.GET.name().equals(method);
    }
}
