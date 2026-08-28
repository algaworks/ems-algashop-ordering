package com.algaworks.algashop.ordering.core.application.checkout.command;

public record BillingSnapshot(
            String firstName,
            String lastName,
            String document,
            String phone,
            String email,
            AddressSnapshot address
    ) {
    }