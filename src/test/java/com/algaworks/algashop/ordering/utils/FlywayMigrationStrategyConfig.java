package com.algaworks.algashop.ordering.utils;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

@TestConfiguration
public class FlywayMigrationStrategyConfig {

   @Bean
   public FlywayMigrationStrategy cleanMigrateStrategy(DataSource dataSource) throws SQLException {
       String catalog = dataSource.getConnection().getCatalog();

       if (catalog == null || !catalog.endsWith("test")) {
           throw new RuntimeException(String.format("Cannot clear database tables because %s is not a test database" +
                   " (suffix 'test' not found).", catalog));
       }
        return flyway -> {
//                flyway.clean();
                flyway.migrate();
        };
   }
}