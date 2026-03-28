package com.algaworks.algashop.ordering.infrastructure.adapters.in.web;

import com.algaworks.algashop.ordering.utils.MockJwtDecoderConfig;
import com.algaworks.algashop.ordering.utils.MockJwtDecoderFactory;
import com.algaworks.algashop.ordering.utils.TestcontainerPostgreSQLConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.specification.RequestSpecification;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.config.JsonConfig.jsonConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:db/testdata/afterMigrate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/clean/afterMigrate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@Import({TestcontainerPostgreSQLConfig.class, MockJwtDecoderConfig.class})
public abstract class AbstractPresentationIT {

    @LocalServerPort
    protected int port;

    protected static WireMockServer wireMockProductCatalog;
    protected static WireMockServer wireMockRapidex;

    protected void beforeEach() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.port = port;
        RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }

    protected RequestSpecification givenAuthenticated(String tokenValue) {
        return RestAssured.given()
                .header("Authorization",
                        "Bearer " + tokenValue);
    }

    protected RequestSpecification givenAuthenticated() {
        return givenAuthenticated(MockJwtDecoderFactory.DEFAULT_TOKEN_VALUE);
    }

    protected RequestSpecification givenWithExpiredToken() {
        return givenAuthenticated(MockJwtDecoderFactory.EXPIRED_TOKEN_VALUE);
    }

    protected RequestSpecification givenAuthenticatedWithNoScopeToken() {
        return givenAuthenticated(MockJwtDecoderFactory.NO_SCOPE_TOKEN_VALUE);
    }

    protected static void initWireMock() {
        wireMockRapidex = new WireMockServer(options()
                .templatingEnabled(true)
                .port(8780)
                .usingFilesUnderDirectory("src/test/resources/wiremock/rapidex"));

        wireMockProductCatalog = new WireMockServer(options()
                .templatingEnabled(true)
                .port(8781)
                .usingFilesUnderDirectory("src/test/resources/wiremock/product-catalog"));

        wireMockRapidex.start();
        wireMockProductCatalog.start();
    }

    protected static void stopMock() {
        wireMockRapidex.stop();
        wireMockProductCatalog.stop();
    }
}
