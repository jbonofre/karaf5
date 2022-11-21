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
package org.apache.karaf.minho.jpa.openjpa;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.stream.Stream;

public class OpenJPAServiceTest {
    
    @Test
    public void simple() throws Exception {
        System.setProperty("derby.system.home", "target/derby");
        System.setProperty("derby.stream.error.file", "target/derby.log");

        try (final var minho =  Minho.builder().loader(() -> Stream.of(new ConfigService(), new LifeCycleService(), new OpenJPAService())).build().start()) {

            EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyEntity", System.getProperties());
            EntityManager em = factory.createEntityManager();

            MyEntity first = new MyEntity();
            first.setKey("foo");
            first.setValue("bar");

            em.getTransaction().begin();
            em.persist(first);
            em.getTransaction().commit();

            Query query = em.createQuery("SELECT my FROM MyEntity my");
            Assertions.assertEquals(1, query.getResultList().size());

            MyEntity result = (MyEntity) query.getResultList().get(0);
            Assertions.assertEquals("foo", result.getKey());
            Assertions.assertEquals("bar", result.getValue());

            em.close();
            factory.close();

        }
    }

}
