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
package org.apache.karaf.demo.petrestaurant.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.karaf.demo.petrestaurant.model.Pet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

@Path("/pet")
public class PetService {

    private EntityManager entityManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Pet> getAll() {
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createQuery("SELECT pet FROM Pet pet");
        return query.getResultList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Pet getPath(@PathParam("id") int id) {
        EntityManager entityManager = getEntityManager();
        return entityManager.find(Pet.class, id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void add(Pet pet) {
        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(pet);
        entityManager.getTransaction().commit();
    }

    private EntityManager getEntityManager() {
        if (entityManager != null) {
            return entityManager;
        }
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("PetRestaurant", System.getProperties());
        return factory.createEntityManager();
    }

}
