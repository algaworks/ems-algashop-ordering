package com.algaworks.algashop.ordering.utils;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.SQLException;

public abstract class AbstractAutoCleanableIT {

    @Autowired
    protected Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    public void setup() throws SQLException {
        String catalog = dataSource.getConnection().getCatalog();
        if (catalog == null || !catalog.endsWith("test")) {
            throw new IllegalArgumentException(String.format("Cannot clear database " +
                    "tables because %s is not a test database", catalog));
        }

//        flyway.clean();
        flyway.migrate();
    }

}
