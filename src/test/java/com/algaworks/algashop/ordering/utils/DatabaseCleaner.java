package com.algaworks.algashop.ordering.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    private static final String[] STATEMENTS = {
            "truncate table order_item cascade",
            "truncate table \"order\" cascade",
            "truncate table shopping_cart_item cascade",
            "truncate table shopping_cart cascade",
            "truncate table customer cascade"
    };

    @Autowired
    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clearTables() {
        checkTestDatabase();
        runScript();
    }

    private void runScript() {
        for (String sql : STATEMENTS) {
            jdbcTemplate.update(sql);
        }
    }

    private void checkTestDatabase() {
        String catalog;
        try {
            catalog = jdbcTemplate.getDataSource().getConnection().getCatalog();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (catalog == null || !catalog.endsWith("test")) {
            throw new RuntimeException(
                    "Cannot clear database tables because '" + catalog +
                            "' is not a test database (suffix 'test' not found).");
        }
    }
}
