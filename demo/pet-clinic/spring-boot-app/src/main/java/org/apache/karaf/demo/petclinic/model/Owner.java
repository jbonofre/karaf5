/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.demo.petclinic.model;

import lombok.Data;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "owners")
public class Owner implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "telephone")
    private String telephone;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    @OrderBy("name")
    private List<Pet> pets = new ArrayList<>();

    public void addPet(Pet pet) {
        if (pet.isNew()) {
            getPets().add(pet);
        }
    }

    public Pet getPet(String name) {
        return getPet(name, false);
    }

    public Pet getPet(Integer id) {
        for (Pet pet : getPets()) {
            if (!pet.isNew()) {
                Integer compId = pet.getId();
                if (compId.equals(id)) {
                    return pet;
                }
            }
        }
        return null;
    }

    public Pet getPet(String name, boolean ignoreNew) {
        name = name.toLowerCase();
        for (Pet pet : getPets()) {
            if (!ignoreNew || !pet.isNew()) {
                String compName = pet.getName();
                compName = compName == null ? "" : compName.toLowerCase();
                if (compName.equals(name)) {
                    return pet;
                }
            }
        }
        return null;
    }

    public Owner addVisit(Integer petId, Visit visit) {
        Assert.notNull(petId, "Pet identifier must not be null!");
        Assert.notNull(visit, "Visit must not be null!");

        Pet pet = getPet(petId);

        Assert.notNull(pet, "Invalid Pet identifier!");

        pet.addVisit(visit);

        return this;
    }

}
