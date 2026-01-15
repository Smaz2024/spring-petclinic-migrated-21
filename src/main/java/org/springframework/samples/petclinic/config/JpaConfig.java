/*
 * Copyright 2002-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.config;

import java.util.Properties;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * JPA Configuration for Hibernate.
 *
 * <p>
 * This class configures the JPA {@link EntityManagerFactory} and
 * {@link PlatformTransactionManager}. It integrates Hibernate with Spring Data
 * JPA
 * repositories and provides performance optimizations like second-level caching
 * and batch processing.
 *
 * <p>
 * Persistence features:
 * <ul>
 * <li>Hibernate 6.x vendor adapter.</li>
 * <li>Caffeine as the second-level cache provider.</li>
 * <li>Automated repository generation for {@code repository} package.</li>
 * </ul>
 *
 * @author Spring Petclinic Team
 * @version 2.0.0
 * @since 2.0.0
 */
@Configuration
// Enables JPA repositories and specifies the base package for repository
// scanning.
@EnableJpaRepositories(basePackages = "org.springframework.samples.petclinic.repository")
public class JpaConfig {

  // Injects the Environment to access application properties.
  @Autowired
  private Environment env;

  /**
   * Configures the JPA {@link LocalContainerEntityManagerFactoryBean}.
   * Demonstrates JDK 21 best practices with enhanced null checking and property
   * management.
   *
   * <p>
   * Sets up Hibernate dialect, SQL formatting, DDL-auto settings, and
   * second-level cache integration via JCache and Caffeine (fully optimized for
   * JDK 21).
   *
   * @param dataSource the data source to use (must not be null)
   * @return the configured entity manager factory bean with JDK 21 optimizations
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      @NotNull DataSource dataSource) {
    // JDK 21: Modern null validation using java.util.Objects for better semantics
    java.util.Objects.requireNonNull(dataSource, "DataSource must not be null");

    // Creates and configures the EntityManagerFactoryBean.
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("org.springframework.samples.petclinic.model");

    // Sets the JPA vendor adapter to Hibernate with JDK 21 support.
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);

    // JDK 21: Configure Hibernate properties using modern property management
    Properties properties = new Properties();
    // Standard JPA properties for PostgreSQL 16
    properties.setProperty("hibernate.dialect", env.getRequiredProperty("jpa.database-platform"));
    properties.setProperty("hibernate.show_sql", env.getProperty("jpa.show-sql", "false"));
    properties.setProperty("hibernate.format_sql", env.getProperty("jpa.format-sql", "false"));
    properties.setProperty(
        "hibernate.hbm2ddl.auto", env.getProperty("jpa.hibernate.ddl-auto", "validate"));

    // JDK 21: Enhanced second-level cache configuration with Caffeine
    // This provides optimal performance with JDK 21's improved memory management
    properties.setProperty("hibernate.cache.use_second_level_cache", "true");
    properties.setProperty("hibernate.cache.use_query_cache", "true");
    properties.setProperty(
        "hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
    // JDK 21: Modern Caffeine JCache provider configuration
    properties.setProperty(
        "hibernate.cache.jcache.provider",
        env.getProperty("hibernate.cache.jcache.provider",
            "com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider"));
    properties.setProperty(
        "hibernate.cache.jcache.uri",
        env.getProperty("hibernate.cache.jcache.uri", "classpath:caffeine-jcache.properties"));

    em.setJpaProperties(properties);
    return em;
  }

  /**
   * Configures the transaction manager for JPA.
   * Leverages JDK 21 Platform abstractions for transaction handling.
   *
   * @param emf The EntityManagerFactory to use for the transaction manager (must
   *            not be null).
   * @return The configured transaction manager with JDK 21 optimizations.
   */
  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
    // JDK 21: Modern null validation
    java.util.Objects.requireNonNull(emf, "EntityManagerFactory must not be null");

    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(emf);
    return transactionManager;
  }
}
