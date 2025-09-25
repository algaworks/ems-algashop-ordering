package com.algaworks.algashop.ordering.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.SQLException;

@Sql(scripts = "classpath:sql/clean-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public abstract class AbstractAutoCleanableIT {

    @BeforeEach
    public void setup() throws SQLException {
    }

}
