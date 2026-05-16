package com.payplatform.events;

public final class EventTopics {
    public static final String PAYMENT_INITIATED = "payment.initiated";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String FRAUD_ALERT = "fraud.alert";

    private EventTopics() {
    }
}
