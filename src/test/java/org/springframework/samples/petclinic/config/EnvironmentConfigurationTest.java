package org.springframework.samples.petclinic.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify environment-specific configurations are loaded correctly.
 * Using @SpringJUnitConfig as this is a standard Spring Framework project
 * (non-Boot).
 */
@SpringJUnitConfig(classes = { AppConfig.class, DataSourceConfig.class })
class EnvironmentConfigurationTest {

    /**
     * Test DEV profile configuration
     */
    @ActiveProfiles("dev")
    static class DevProfileTest {

        @Autowired
        private Environment env;

        @Test
        void shouldLoadDevConfiguration() {
            assertThat(env.getActiveProfiles()).contains("dev");
            // Values from application-dev.properties
            assertThat(env.getProperty("jdbc.url")).contains("jdbc:postgresql://localhost:5432/petclinic");
            assertThat(env.getProperty("hikari.maximum-pool-size")).isEqualTo("10");
        }
    }

    /**
     * Test SIT profile configuration
     */
    @ActiveProfiles("sit")
    static class SitProfileTest {

        @Autowired
        private Environment env;

        @Test
        void shouldLoadSitConfiguration() {
            assertThat(env.getActiveProfiles()).contains("sit");
            // Values from application-sit.properties
            assertThat(env.getProperty("jdbc.url")).isEqualTo("jdbc:postgresql://sit-db-server:5432/petclinic_sit");
            assertThat(env.getProperty("hikari.maximum-pool-size")).isEqualTo("15");
        }
    }

    /**
     * Test UAT profile configuration
     */
    @ActiveProfiles("uat")
    static class UatProfileTest {

        @Autowired
        private Environment env;

        @Test
        void shouldLoadUatConfiguration() {
            assertThat(env.getActiveProfiles()).contains("uat");
            // Values from application-uat.properties
            assertThat(env.getProperty("jdbc.url")).isEqualTo("jdbc:postgresql://uat-db-server:5432/petclinic_uat");
            assertThat(env.getProperty("hikari.maximum-pool-size")).isEqualTo("20");
        }
    }

    /**
     * Test PROD profile configuration
     */
    @ActiveProfiles("prod")
    static class ProdProfileTest {

        @Autowired
        private Environment env;

        @Test
        void shouldLoadProdConfiguration() {
            assertThat(env.getActiveProfiles()).contains("prod");
            // Values from application-prod.properties
            assertThat(env.getProperty("jdbc.url")).isEqualTo("jdbc:postgresql://prod-db-primary:5432/petclinic_prod");
            assertThat(env.getProperty("hikari.maximum-pool-size")).isEqualTo("30");
        }
    }
}
