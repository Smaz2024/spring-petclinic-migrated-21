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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Simple JavaBean domain object representing a pet.
 *
 * @author Spring Petclinic Team
 * @version 2.0.0
 * @since 2.0.0
 */
@Entity
@Table(name = "pets")
public class Pet extends NamedEntity {

  private static final long serialVersionUID = 1L;

  @Column(name = "birth_date")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @NotNull
  private LocalDate birthDate;

  @ManyToOne
  @JoinColumn(name = "type_id")
  @NotNull
  private PetType type;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  private Owner owner;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "pet", fetch = FetchType.EAGER)
  private Set<Visit> visits = new LinkedHashSet<>();

  public void setBirthDate(LocalDate birthDate) {

    this.birthDate = birthDate;
  }

  public LocalDate getBirthDate() {

    return this.birthDate;
  }

  public PetType getType() {

    return this.type;
  }

  public void setType(PetType type) {

    this.type = type;
  }

  public Owner getOwner() {

    return this.owner;
  }

  public void setOwner(Owner owner) {

    this.owner = owner;
  }

  protected Set<Visit> getVisitsInternal() {
    if (this.visits == null) {
      this.visits = new HashSet<>();
    }
    return this.visits;
  }

  protected void setVisitsInternal(Set<Visit> visits) {
    this.visits = visits;
  }

  public List<Visit> getVisits() {
    // JDK 21: Using Sequenced Collections API for ordered collection operations
    // This provides a cleaner interface for accessing collection members by
    // position
    List<Visit> sortedVisits = new ArrayList<>(getVisitsInternal());
    PropertyComparator.sort(sortedVisits, new MutableSortDefinition("date", false, false));
    // Returns an unmodifiable view using modern JDK 21 Collections
    return Collections.unmodifiableList(sortedVisits);
  }

  public void addVisit(Visit visit) {
    getVisitsInternal().add(visit);
    visit.setPet(this);
  }
}
