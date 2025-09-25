package com.algaworks.algashop.ordering.infrastructure.persistence.repository;

import com.algaworks.algashop.ordering.domain.model.customer.CustomerId;
import com.algaworks.algashop.ordering.domain.model.customer.CustomerTestDataBuilder;
import com.algaworks.algashop.ordering.infrastructure.persistence.SpringDataAuditingConfig;
import com.algaworks.algashop.ordering.infrastructure.persistence.customer.CustomerPersistenceEntity;
import com.algaworks.algashop.ordering.infrastructure.persistence.customer.CustomerPersistenceEntityRepository;
import com.algaworks.algashop.ordering.infrastructure.persistence.entity.CustomerPersistenceEntityTestDataBuilder;
import com.algaworks.algashop.ordering.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntity;
import com.algaworks.algashop.ordering.infrastructure.persistence.entity.ShoppingCartPersistenceEntityTestDataBuilder;
import com.algaworks.algashop.ordering.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntityRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SpringDataAuditingConfig.class)
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/testdata")
class ShoppingCartPersistenceEntityRepositoryIT {

    private final ShoppingCartPersistenceEntityRepository shoppingCartPersistenceEntityRepository;
    private final CustomerPersistenceEntityRepository customerPersistenceEntityRepository;

    private CustomerPersistenceEntity customerPersistenceEntity;

    private UUID customerIdWithoutShoppingCart = UUID.fromString("3a4b5c6d-7e8f-9a0b-1c2d-3e4f5a6b7c8d");

    @Autowired
    public ShoppingCartPersistenceEntityRepositoryIT(ShoppingCartPersistenceEntityRepository shoppingCartPersistenceEntityRepository,
                                                     CustomerPersistenceEntityRepository customerPersistenceEntityRepository) {
        this.shoppingCartPersistenceEntityRepository = shoppingCartPersistenceEntityRepository;
        this.customerPersistenceEntityRepository = customerPersistenceEntityRepository;
    }

    @BeforeEach
    public void setup() {
        customerPersistenceEntity = customerPersistenceEntityRepository
                .getReferenceById(customerIdWithoutShoppingCart);
    }

    @Test
    public void shouldPersist() {
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingShoppingCart()
                .customer(customerPersistenceEntity)
                .build();

        shoppingCartPersistenceEntityRepository.saveAndFlush(entity);
        Assertions.assertThat(shoppingCartPersistenceEntityRepository.existsById(entity.getId())).isTrue();

        ShoppingCartPersistenceEntity savedEntity = shoppingCartPersistenceEntityRepository.findById(entity.getId()).orElseThrow();

        Assertions.assertThat(savedEntity.getItems()).isNotEmpty();
    }

    @Test
    public void shouldCount() {
        long shoppingCartsCount = shoppingCartPersistenceEntityRepository.count();
        Assertions.assertThat(shoppingCartsCount).isEqualTo(2L);
    }

    @Test
    public void shouldSetAuditingValues() {
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingShoppingCart()
                .customer(customerPersistenceEntity)
                .build();
        entity = shoppingCartPersistenceEntityRepository.saveAndFlush(entity);

        Assertions.assertThat(entity.getCreatedByUserId()).isNotNull();

        Assertions.assertThat(entity.getLastModifiedAt()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedByUserId()).isNotNull();
    }

}