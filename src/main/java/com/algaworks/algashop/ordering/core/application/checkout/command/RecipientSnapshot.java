package com.algaworks.algashop.ordering.core.application.checkout.command;

public record RecipientSnapshot(String firstName, String lastName, String document, String phone) {
    }