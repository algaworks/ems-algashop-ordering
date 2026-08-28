package com.algaworks.algashop.ordering.core.application.checkout.command;

public record AddressSnapshot(
            String street,
            String complement,
            String neighborhood,
            String number,
            String city,
            String state,
            String zipCode
    ) {
    }