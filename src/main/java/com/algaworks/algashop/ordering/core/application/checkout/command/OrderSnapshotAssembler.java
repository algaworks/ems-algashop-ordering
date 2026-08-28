package com.algaworks.algashop.ordering.core.application.checkout.command;

import com.algaworks.algashop.ordering.core.domain.model.commons.*;
import com.algaworks.algashop.ordering.core.domain.model.customer.CustomerId;
import com.algaworks.algashop.ordering.core.domain.model.order.*;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductId;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductName;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderSnapshotAssembler {

    public OrderSnapshot toSnapshot(Order order) {
        return toSnapshot(order, null);
    }

    public OrderSnapshot toSnapshot(Order order, UUID shoppingCartId) {
        return new OrderSnapshot(
                order.id().toString(),
                order.customerId().value(),
                order.placedAt(),
                order.status(),
                toSnapshot(order.billing()),
                toSnapshot(order.shipping()),
                order.paymentMethod().name(),
                order.creditCardId() == null ? null : order.creditCardId().id(),
                order.totalAmount().value(),
                order.totalItems().value(),
                order.items().stream().map(this::toSnapshot).collect(Collectors.toSet()),
                shoppingCartId
        );
    }

    public Order toDomain(OrderSnapshot snapshot) {
        CreditCardId creditCardId = snapshot.creditCardId() == null ? null : new CreditCardId(snapshot.creditCardId());
        Set<OrderItem> items = snapshot.items().stream().map(item -> toDomain(snapshot.orderId(), item))
                .collect(Collectors.toSet());

        return Order.existing()
                .id(new OrderId(snapshot.orderId()))
                .version(null)
                .customerId(new CustomerId(snapshot.customerId()))
                .totalAmount(new Money(snapshot.totalAmount()))
                .totalItems(new Quantity(snapshot.totalItems()))
                .placedAt(snapshot.placedAt())
                .paidAt(null)
                .canceledAt(null)
                .readyAt(null)
                .billing(toDomain(snapshot.billing()))
                .shipping(toDomain(snapshot.shipping()))
                .status(snapshot.status())
                .paymentMethod(PaymentMethod.valueOf(snapshot.paymentMethod()))
                .items(items)
                .creditCardId(creditCardId)
                .build();
    }

    private BillingSnapshot toSnapshot(Billing billing) {
        return new BillingSnapshot(
                billing.fullName().firstName(), billing.fullName().lastName(), billing.document().value(),
                billing.phone().value(), billing.email().value(), toSnapshot(billing.address())
        );
    }

    private ShippingSnapshot toSnapshot(Shipping shipping) {
        var recipient = shipping.recipient();
        return new ShippingSnapshot(
                shipping.cost().value(), shipping.expectedDate(),
                new RecipientSnapshot(
                        recipient.fullName().firstName(), recipient.fullName().lastName(),
                        recipient.document().value(), recipient.phone().value()),
                toSnapshot(shipping.address())
        );
    }

    private AddressSnapshot toSnapshot(Address address) {
        return new AddressSnapshot(address.street(), address.complement(), address.neighborhood(),
                address.number(), address.city(), address.state(), address.zipCode().value());
    }

    private OrderItemSnapshot toSnapshot(OrderItem item) {
        return new OrderItemSnapshot(item.id().toString(), item.productId().value(), item.productName().value(),
                item.price().value(), item.quantity().value(), item.totalAmount().value());
    }

    private Billing toDomain(BillingSnapshot billing) {
        return Billing.builder()
                .fullName(new FullName(billing.firstName(), billing.lastName()))
                .document(new Document(billing.document()))
                .phone(new Phone(billing.phone()))
                .email(new Email(billing.email()))
                .address(toDomain(billing.address()))
                .build();
    }

    private Shipping toDomain(ShippingSnapshot shipping) {
        var recipient = shipping.recipient();
        return Shipping.builder()
                .cost(new Money(shipping.cost()))
                .expectedDate(shipping.expectedDate())
                .recipient(Recipient.builder()
                        .fullName(new FullName(recipient.firstName(), recipient.lastName()))
                        .document(new Document(recipient.document()))
                        .phone(new Phone(recipient.phone()))
                        .build())
                .address(toDomain(shipping.address()))
                .build();
    }

    private Address toDomain(AddressSnapshot address) {
        return Address.builder()
                .street(address.street())
                .complement(address.complement())
                .neighborhood(address.neighborhood())
                .number(address.number())
                .city(address.city())
                .state(address.state())
                .zipCode(new ZipCode(address.zipCode()))
                .build();
    }

    private OrderItem toDomain(String orderId, OrderItemSnapshot item) {
        return OrderItem.existing()
                .id(new OrderItemId(item.orderItemId()))
                .orderId(new OrderId(orderId))
                .productId(new ProductId(item.productId()))
                .productName(new ProductName(item.productName()))
                .price(new Money(item.price()))
                .quantity(new Quantity(item.quantity()))
                .totalAmount(new Money(item.totalAmount()))
                .build();
    }
}