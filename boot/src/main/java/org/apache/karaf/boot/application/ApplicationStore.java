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
package org.apache.karaf.boot.application;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationStore {

    private final List<Holder> store = Collections.synchronizedList(new ArrayList<>());

    public void add(String id, String url, String manager) {
        Holder holder = new Holder();
        holder.setId(id);
        holder.setUrl(url);
        holder.setManager(manager);
        store.add(holder);
    }

    public void remove(String id) {
        if (searchHolderById(id) != null) {
            store.remove(searchHolderById(id));
        }
    }

    public boolean urlExists(String url) {
        for (Holder holder : store) {
            if (holder.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }

    public boolean idExists(String id) {
        for (Holder holder : store) {
            if (holder.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public String getManager(String id) {
        for (Holder holder : store) {
            if (holder.getId().equals(id)) {
                return holder.getManager();
            }
        }
        return null;
    }

    public String getUrl(String id) {
        for (Holder holder : store) {
            if (holder.getId().equals(id)) {
                return holder.getUrl();
            }
        }
        return null;
    }

    private Holder searchHolderById(String id) {
        for (Holder holder : store) {
            if (holder.getId().equals(id)) {
                return holder;
            }
        }
        return null;
    }

    public List<String> getIds() {
        List<String> ids = new ArrayList<>();
        store.forEach(holder -> {
            ids.add(holder.getId());
        });
        return ids;
    }

    @Data
    public class Holder {

        private String id;
        private String url;
        private String manager;

    }
}
