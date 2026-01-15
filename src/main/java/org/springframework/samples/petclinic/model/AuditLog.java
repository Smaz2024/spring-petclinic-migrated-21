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
package org.springframework.samples.petclinic.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Audit log entity for compliance tracking.
 *
 * <p>
 * Stores historical data about changes to business entities, including
 * who made the change, when it occurred, and the associated trace ID for
 * distributed tracing correlation.
 *
 * @author Spring Petclinic Team
 * @version 2.0.0
 * @since 2.0.0
 */
@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

  private static final long serialVersionUID = 1L;

  @Column(name = "entity_type")
  @NotNull
  private String entityType;

  @Column(name = "entity_id")
  private Long entityId;

  @Column(name = "action")
  @NotNull
  private String action;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "timestamp")
  @NotNull
  private LocalDateTime timestamp;

  @Column(name = "old_value", columnDefinition = "TEXT")
  private String oldValue;

  @Column(name = "new_value", columnDefinition = "TEXT")
  private String newValue;

  @Column(name = "trace_id")
  private String traceId;

  @Column(name = "ip_address")
  private String ipAddress;

  public AuditLog() {
    this.timestamp = LocalDateTime.now();
  }

  // Getters and Setters
  public String getEntityType() {

    return entityType;
  }

  public void setEntityType(String entityType) {

    this.entityType = entityType;
  }

  public Long getEntityId() {

    return entityId;
  }

  public void setEntityId(Long entityId) {

    this.entityId = entityId;
  }

  public String getAction() {

    return action;
  }

  public void setAction(String action) {

    this.action = action;
  }

  public String getUserId() {

    return userId;
  }

  public void setUserId(String userId) {

    this.userId = userId;
  }

  public LocalDateTime getTimestamp() {

    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {

    this.timestamp = timestamp;
  }

  public String getOldValue() {

    return oldValue;
  }

  public void setOldValue(String oldValue) {

    this.oldValue = oldValue;
  }

  public String getNewValue() {

    return newValue;
  }

  public void setNewValue(String newValue) {

    this.newValue = newValue;
  }

  public String getTraceId() {

    return traceId;
  }

  public void setTraceId(String traceId) {

    this.traceId = traceId;
  }

  public String getIpAddress() {

    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {

    this.ipAddress = ipAddress;
  }
}
