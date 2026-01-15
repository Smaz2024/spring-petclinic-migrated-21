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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check Controller for Load Balancer Integration.
 *
 * <p>
 * This controller provides health check endpoints that can be used by load
 * balancers
 * to determine if the application instance is healthy and ready to receive
 * traffic.
 * 
 * <p>
 * Endpoints:
 * <ul>
 * <li>/health - Basic health check (application status only)</li>
 * <li>/health/detailed - Detailed health check (database, cache, etc.)</li>
 * <li>/health/liveness - Kubernetes-style liveness probe</li>
 * <li>/health/readiness - Kubernetes-style readiness probe</li>
 * </ul>
 *
 * @author Spring Petclinic Team
 * @version 2.0.0
 * @since 2.0.0
 */
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired
    private Environment environment;

    /**
     * Basic health check endpoint.
     * 
     * <p>
     * Returns a simple UP status with minimal information.
     * This is suitable for basic load balancer health checks.
     *
     * @return ResponseEntity with health status
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("application", environment.getProperty("spring.application.name", "spring-petclinic"));
        response.put("version", environment.getProperty("application.version", "unknown"));

        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check endpoint.
     * 
     * <p>
     * Performs comprehensive health checks including:
     * - Database connectivity
     * - Cache availability
     * - Active profile information
     * 
     * <p>
     * Returns HTTP 200 if all checks pass, HTTP 503 if any check fails.
     *
     * @return ResponseEntity with detailed health status
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> checks = new HashMap<>();
        boolean allHealthy = true;

        // Application info
        response.put("timestamp", Instant.now().toString());
        response.put("application", environment.getProperty("spring.application.name", "spring-petclinic"));
        response.put("version", environment.getProperty("application.version", "unknown"));
        response.put("profile", environment.getProperty("spring.profiles.active", "default"));

        // Database health check
        Map<String, Object> dbHealth = checkDatabase();
        checks.put("database", dbHealth);
        if (!"UP".equals(dbHealth.get("status"))) {
            allHealthy = false;
        }

        // Cache health check
        Map<String, Object> cacheHealth = checkCache();
        checks.put("cache", cacheHealth);
        if (!"UP".equals(cacheHealth.get("status"))) {
            allHealthy = false;
        }

        response.put("checks", checks);
        response.put("status", allHealthy ? "UP" : "DOWN");

        HttpStatus httpStatus = allHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Liveness probe endpoint.
     * 
     * <p>
     * Indicates whether the application is running.
     * This is used by Kubernetes to determine if the container should be restarted.
     * 
     * <p>
     * This endpoint only checks if the application is alive, not if it's ready to
     * serve traffic.
     *
     * @return ResponseEntity with liveness status
     */
    @GetMapping("/liveness")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("check", "liveness");

        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe endpoint.
     * 
     * <p>
     * Indicates whether the application is ready to serve traffic.
     * This is used by Kubernetes and load balancers to determine if traffic should
     * be routed to this instance.
     * 
     * <p>
     * Checks database connectivity to ensure the application can handle requests.
     *
     * @return ResponseEntity with readiness status
     */
    @GetMapping("/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("check", "readiness");

        // Check database connectivity for readiness
        Map<String, Object> dbHealth = checkDatabase();
        boolean ready = "UP".equals(dbHealth.get("status"));

        response.put("status", ready ? "UP" : "DOWN");
        response.put("database", dbHealth);

        HttpStatus httpStatus = ready ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Checks database connectivity.
     *
     * @return Map containing database health status
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbHealth = new HashMap<>();

        try {
            // Execute a simple query to verify database connectivity
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            if (result != null && result == 1) {
                dbHealth.put("status", "UP");
                dbHealth.put("database", environment.getProperty("jdbc.url", "unknown"));

                // Get additional database info
                try {
                    String dbVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
                    dbHealth.put("version", dbVersion);
                } catch (Exception e) {
                    logger.debug("Could not retrieve database version", e);
                }
            } else {
                dbHealth.put("status", "DOWN");
                dbHealth.put("error", "Unexpected query result");
            }
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }

        return dbHealth;
    }

    /**
     * Checks cache availability.
     *
     * @return Map containing cache health status
     */
    private Map<String, Object> checkCache() {
        Map<String, Object> cacheHealth = new HashMap<>();

        try {
            if (cacheManager != null) {
                // Check if cache manager is available and has configured caches
                var cacheNames = cacheManager.getCacheNames();

                if (cacheNames != null && !cacheNames.isEmpty()) {
                    cacheHealth.put("status", "UP");
                    cacheHealth.put("cacheNames", cacheNames);
                    cacheHealth.put("cacheCount", cacheNames.size());
                } else {
                    cacheHealth.put("status", "UP");
                    cacheHealth.put("warning", "No caches configured");
                }
            } else {
                cacheHealth.put("status", "UP");
                cacheHealth.put("warning", "Cache manager not available");
            }
        } catch (Exception e) {
            logger.error("Cache health check failed", e);
            cacheHealth.put("status", "DOWN");
            cacheHealth.put("error", e.getMessage());
        }

        return cacheHealth;
    }
}
