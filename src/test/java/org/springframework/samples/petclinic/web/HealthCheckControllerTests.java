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
package org.springframework.samples.petclinic.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Test class for {@link HealthCheckController}
 */
@ExtendWith(MockitoExtension.class)
class HealthCheckControllerTests {

    @InjectMocks
    private HealthCheckController healthCheckController;

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Environment environment;

    @Mock
    private Cache cache;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(healthCheckController).build();

        // Setup default environment properties
        org.mockito.Mockito.lenient().when(environment.getProperty("spring.application.name", "spring-petclinic"))
                .thenReturn("spring-petclinic");
        org.mockito.Mockito.lenient().when(environment.getProperty("application.version", "unknown"))
                .thenReturn("2.0.0-SNAPSHOT");
        org.mockito.Mockito.lenient().when(environment.getProperty("spring.profiles.active", "default"))
                .thenReturn("dev");
        org.mockito.Mockito.lenient().when(environment.getProperty("jdbc.url", "unknown"))
                .thenReturn("jdbc:postgresql://localhost:5432/petclinic");
    }

    @Test
    void testBasicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("spring-petclinic"))
                .andExpect(jsonPath("$.version").value("2.0.0-SNAPSHOT"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testDetailedHealthEndpointWhenHealthy() throws Exception {
        // Mock successful database query
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForObject("SELECT version()", String.class))
                .thenReturn("PostgreSQL 16.0");

        // Mock cache manager
        List<String> cacheNames = Arrays.asList("vets", "specialties", "petTypes");
        when(cacheManager.getCacheNames()).thenReturn(cacheNames);

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("spring-petclinic"))
                .andExpect(jsonPath("$.profile").value("dev"))
                .andExpect(jsonPath("$.checks.database.status").value("UP"))
                .andExpect(jsonPath("$.checks.database.version").value("PostgreSQL 16.0"))
                .andExpect(jsonPath("$.checks.cache.status").value("UP"))
                .andExpect(jsonPath("$.checks.cache.cacheCount").value(3));
    }

    @Test
    void testDetailedHealthEndpointWhenDatabaseDown() throws Exception {
        // Mock database failure
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new DataAccessException("Connection refused") {
                });

        // Mock cache manager (still healthy)
        List<String> cacheNames = Arrays.asList("vets", "specialties", "petTypes");
        when(cacheManager.getCacheNames()).thenReturn(cacheNames);

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.checks.database.status").value("DOWN"))
                .andExpect(jsonPath("$.checks.database.error").exists())
                .andExpect(jsonPath("$.checks.cache.status").value("UP"));
    }

    @Test
    void testLivenessProbe() throws Exception {
        mockMvc.perform(get("/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.check").value("liveness"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testReadinessProbeWhenReady() throws Exception {
        // Mock successful database query
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForObject("SELECT version()", String.class))
                .thenReturn("PostgreSQL 16.0");

        mockMvc.perform(get("/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.check").value("readiness"))
                .andExpect(jsonPath("$.database.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testReadinessProbeWhenNotReady() throws Exception {
        // Mock database failure
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new DataAccessException("Connection timeout") {
                });

        mockMvc.perform(get("/health/readiness"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.check").value("readiness"))
                .andExpect(jsonPath("$.database.status").value("DOWN"))
                .andExpect(jsonPath("$.database.error").exists());
    }

    @Test
    void testDetailedHealthWithNoCacheManager() throws Exception {
        // Mock successful database query
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        // Set cache manager to null (simulating no cache configuration)
        healthCheckController = new HealthCheckController();

        org.springframework.test.util.ReflectionTestUtils.setField(
                healthCheckController, "jdbcTemplate", jdbcTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(
                healthCheckController, "cacheManager", null);
        org.springframework.test.util.ReflectionTestUtils.setField(
                healthCheckController, "environment", environment);

        mockMvc = MockMvcBuilders.standaloneSetup(healthCheckController).build();

        mockMvc.perform(get("/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.checks.database.status").value("UP"))
                .andExpect(jsonPath("$.checks.cache.status").value("UP"))
                .andExpect(jsonPath("$.checks.cache.warning").exists());
    }
}
