package org.kineticsfoundation.dao.provider;

/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * limitations under the License
 */


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A convenience wrapper for a projection map.  Makes it easier to create and use projection maps.
 */
public class ProjectionMap extends HashMap<String, String> {

    private String[] mColumns;

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        throw new UnsupportedOperationException();
    }

    private void putColumn(String alias, String column) {
        super.put(alias, column);
    }

    public static class Builder {

        private final ProjectionMap mMap = new ProjectionMap();

        public Builder add(String column) {
            mMap.putColumn(column, column);
            return this;
        }

        public Builder add(String alias, String expression) {
            mMap.putColumn(alias, expression + " AS " + alias);
            return this;
        }

        public ProjectionMap build() {
            String[] columns = new String[mMap.size()];
            mMap.keySet().toArray(columns);
            Arrays.sort(columns);
            mMap.mColumns = columns;
            return mMap;
        }

    }
}

