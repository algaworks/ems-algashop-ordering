package com.algaworks.algashop.ordering.core.application.order.event;

public record RecipientSnapshot(String firstName, String lastName, String document, String phone) {
    }