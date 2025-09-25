package com.algaworks.algashop.ordering.presentation.shoppincart;

import com.algaworks.algashop.ordering.infrastructure.persistence.customer.CustomerPersistenceEntityRepository;
import com.algaworks.algashop.ordering.infrastructure.persistence.entity.CustomerPersistenceEntityTestDataBuilder;
import com.algaworks.algashop.ordering.infrastructure.persistence.entity.ShoppingCartPersistenceEntityTestDataBuilder;
import com.algaworks.algashop.ordering.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntity;
import com.algaworks.algashop.ordering.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntityRepository;
import com.algaworks.algashop.ordering.utils.AbstractAutoCleanableIT;
import com.algaworks.algashop.ordering.utils.AlgaShopResourceUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.sql.SQLException;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.config.JsonConfig.jsonConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/testdata")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ShoppingCartControllerIT extends AbstractAutoCleanableIT {

    @LocalServerPort
    private int port;

    @Autowired
    private CustomerPersistenceEntityRepository customerRepository;

    @Autowired
    private ShoppingCartPersistenceEntityRepository shoppingCartRepository;

    private static final UUID customerIdWithoutShoppingCart = UUID.fromString("3a4b5c6d-7e8f-9a0b-1c2d-3e4f5a6b7c8d");
    private static final UUID validShoppingCartId = UUID.fromString("9c8d7e6f-5a4b-3c2d-1e0f-9d8c7b6a5f4e");

    private static WireMockServer wireMockProductCatalog;

    @BeforeAll
    public static void beforeAll() {
        wireMockProductCatalog = new WireMockServer(options()
                .port(8781)
                .usingFilesUnderDirectory("src/test/resources/wiremock/product-catalog")
                .extensions(new ResponseTemplateTransformer(true)));
        wireMockProductCatalog.start();
    }

    @BeforeEach
    public void setup() throws SQLException {
        super.setup();

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.port = port;

        RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }

    @AfterAll
    public static void after() {
        wireMockProductCatalog.stop();
    }

    @Test
    public void shouldCreateShoppingCart() {
        String json = AlgaShopResourceUtils.readContent("json/create-shopping-cart.json");

        UUID createdShoppingCart = RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(json)
            .when()
                .post("/api/v1/shopping-carts")
            .then()
                .assertThat()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .statusCode(HttpStatus.CREATED.value())
                .body("id", Matchers.not(Matchers.emptyString()))
                .extract()
            .jsonPath().getUUID("id");

        Assertions.assertThat(shoppingCartRepository.existsById(createdShoppingCart)).isTrue();
    }

    @Test
    public void shouldAddProductToShoppingCart() {
        String json = AlgaShopResourceUtils.readContent("json/add-product-to-shopping-cart.json");

        RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(json)
            .when()
                .post("/api/v1/shopping-carts/{shoppingCartId}/items", validShoppingCartId)
            .then()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value());

        var shoppingCartPersistenceEntity = shoppingCartRepository.findById(validShoppingCartId).orElseThrow();
        Assertions.assertThat(shoppingCartPersistenceEntity.getTotalItems()).isEqualTo(4);
    }

}
