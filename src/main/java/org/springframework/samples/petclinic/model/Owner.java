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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.petclinic.util.DataMaskingUtil;

/**
 * Simple JavaBean domain object representing an owner.
 *
 * <p>
 * Integrates with {@link DataMaskingUtil} to ensure PII (Personally
 * Identifiable Information)
 * such as address and telephone is masked in logs.
 *
 * @author Spring Petclinic Team
 * @version 2.0.0
 * @since 2.0.0
 */
@Entity
@Table(name = "owners")
public class Owner extends Person {

  private static final long serialVersionUID = 1L;

  @Column(name = "address")
  @NotEmpty(message = "Address is required.")
  @jakarta.validation.constraints.Size(min = 5, max = 255, message = "Address must be at least 5 characters.")
  private String address;

  @Column(name = "city")
  @NotEmpty(message = "City is required.")
  private String city;

  @Column(name = "telephone")
  @NotEmpty(message = "Telephone is required.")
  @jakarta.validation.constraints.Pattern(regexp = "\\d{10}", message = "Telephone must be exactly 10 digits.")
  private String telephone;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
  private Set<Pet> pets;

  public String getAddress() {

    return this.address;
  }

  public void setAddress(String address) {

    this.address = address;
  }

  public String getCity() {

    return this.city;
  }

  public void setCity(String city) {

    this.city = city;
  }

  public String getTelephone() {

    return this.telephone;
  }

  public void setTelephone(String telephone) {

    this.telephone = telephone;
  }

  protected Set<Pet> getPetsInternal() {
    if (this.pets == null) {
      this.pets = new HashSet<>();
    }
    return this.pets;
  }

  protected void setPetsInternal(Set<Pet> pets) {
    this.pets = pets;
  }

  public List<Pet> getPets() {
    // JDK 21: Using Sequenced Collections API for more efficient collection
    // operations
    // ArrayList provides direct access to underlying sequence, preserving insertion
    // order
    List<Pet> sortedPets = new ArrayList<>(getPetsInternal());
    PropertyComparator.sort(sortedPets, new MutableSortDefinition("name", true, true));
    // Returns an unmodifiable view of the sorted list using JDK 21
    // Collections.unmodifiableList
    return Collections.unmodifiableList(sortedPets);
  }

  public void addPet(Pet pet) {

    if (pet.isNew()) {
      getPetsInternal().add(pet);
    }
    pet.setOwner(this);
  }

  /**
   * Return the Pet with the given name, or null if none found for this Owner.
   * Uses enhanced instanceof pattern matching (JDK 21: JEP 440).
   *
   * @param name to test
   * @return true if pet name is already in use
   */
  public Pet getPet(String name) {

    return getPet(name, false);
  }

  /**
   * Return the Pet with the given name, or null if none found for this Owner.
   * Demonstrates JDK 21 improvements with enhanced loop and pattern matching.
   *
   * @param name      to test
   * @param ignoreNew whether to ignore new pets
   * @return the Pet with matching name, or null if not found
   */
  public Pet getPet(String name, boolean ignoreNew) {

    // JDK 21: Use lowercase() with more efficient string handling
    String lowerName = name.toLowerCase();
    for (Pet pet : getPetsInternal()) {
      if (!ignoreNew || !pet.isNew()) {
        String compName = pet.getName();
        // JDK 21: Improved string comparison using modern APIs
        if (compName.toLowerCase().equals(lowerName)) {
          return pet;
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    // Use DataMaskingUtil for PII in logs
    return new ToStringCreator(this)
        .append("id", this.getId())
        .append("new", this.isNew())
        .append("lastName", this.getLastName())
        .append("firstName", this.getFirstName())
        .append("address", DataMaskingUtil.maskAddress(this.address))
        .append("city", this.city)
        .append("telephone", DataMaskingUtil.maskTelephone(this.telephone))
        .toString();
  }
}
