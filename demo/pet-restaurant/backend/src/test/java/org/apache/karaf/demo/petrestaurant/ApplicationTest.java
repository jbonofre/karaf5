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
package org.apache.karaf.demo.petrestaurant;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.karaf.boot.Karaf;
import org.apache.karaf.demo.petrestaurant.model.Pet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ApplicationTest {

    @Test
    public void bootstrap() throws Exception {
        System.setProperty("derby.system.home", "target/derby");
        System.setProperty("derby.stream.error.file", "target/derby.log");

        Karaf karaf = Karaf.builder().build().start();

        Client restClient = ClientBuilder.newClient();

        Pet pet = new Pet();
        pet.setFirstName("John");
        pet.setLastName("Doo");
        Response response = restClient.target("http://localhost:8080/petrestaurant/pet").request(MediaType.APPLICATION_JSON).post(Entity.entity(pet, MediaType.APPLICATION_JSON));
        Assertions.assertEquals(204, response.getStatus());

        List<Pet> pets = restClient.target("http://localhost:8080/petrestaurant/pet")
                        .request(MediaType.APPLICATION_JSON).get(new GenericType<List<Pet>>() {});
        Assertions.assertEquals(1, pets.size());

        pet = pets.get(0);
        Assertions.assertEquals(1, pet.getId());
        Assertions.assertEquals("John", pet.getFirstName());
        Assertions.assertEquals("Doo", pet.getLastName());

        pet = restClient.target("http://localhost:8080/petrestaurant/pet/1").request(MediaType.APPLICATION_JSON).get(Pet.class);
        Assertions.assertEquals(1, pet.getId());
        Assertions.assertEquals("John", pet.getFirstName());
        Assertions.assertEquals("Doo", pet.getLastName());

        karaf.close();
    }

}
