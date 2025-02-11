// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.starrocks.catalog;

import com.starrocks.common.DdlException;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;

public class FlatJsonProperty {
    public static final String FLAT_JSON_PROPERTY_PREFIX = "flat_json";

    public static final String ENABLE = "flat_json.enable";
    public static final String NULL_FACTOR = "flat_json.null.factor";
    public static final String SPARSITY_FACTOR = "flat_json.sparsity.factor";
    public static final String COLUMN_MAX = "flat_json.column.max";

    public static final double DEFAULT_NULL_FACTOR = 0.3;
    public static final double DEFAULT_SPARSITY_FACTOR = 0.9;
    public static final int DEFAULT_COLUMN_MAX = 100;

    private boolean enabled;
    private double nullFactor;
    private double sparsityFactor;
    private int columnMax;

    public FlatJsonProperty(Map<String, String> properties) {
        if (properties != null && !properties.isEmpty()) {
            this.enabled = Boolean.parseBoolean(properties.get(ENABLE));
            this.nullFactor =
                    Double.parseDouble(properties.getOrDefault(NULL_FACTOR, String.valueOf(DEFAULT_NULL_FACTOR)));
            this.sparsityFactor =
                    Double.parseDouble(properties.getOrDefault(SPARSITY_FACTOR, String.valueOf(DEFAULT_SPARSITY_FACTOR)));
            this.columnMax =
                    Integer.parseInt(properties.getOrDefault(COLUMN_MAX, String.valueOf(DEFAULT_COLUMN_MAX)));
        }
    }

    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(ENABLE, String.valueOf(enabled));
        properties.put(NULL_FACTOR, String.valueOf(nullFactor));
        properties.put(SPARSITY_FACTOR, String.valueOf(sparsityFactor));
        properties.put(COLUMN_MAX, String.valueOf(columnMax));
        return properties;
    }

    // TODO: 后面再补充，看下如何检查输入的flat json的参数
    public static boolean checkInputFlatJsonProperties(OlapTable olapTable, Map<String, String> properties)
            throws DdlException {
        if (properties == null || properties.isEmpty()) {
            return false;
        }

        Map<String, String> checkProp = new CaseInsensitiveMap<>(properties);

        String enable = checkProp.get(FlatJsonProperty.ENABLE);
        return true;
    }
}
