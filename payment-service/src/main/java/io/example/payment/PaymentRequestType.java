/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.example.payment;

public enum PaymentRequestType {
    REQUEST, CANCEL;

    public boolean isRequest() {
        return REQUEST == this;
    }
}
