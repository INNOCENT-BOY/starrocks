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
package com.starrocks.scheduler;

import com.google.common.collect.Maps;
import com.starrocks.catalog.BaseTableInfo;
import com.starrocks.catalog.MaterializedView;
import com.starrocks.catalog.Table;

import java.util.Map;
import java.util.Objects;

/**
 * `TableSnapshotInfo` represents a snapshot of the base table of materialized view.
 *  To avoid changes of the base table during mv's refresh period, collect base tables' snapshot info before refresh
 *  and use those to update refreshed meta of base tables after refresh finished.
 */
public class TableSnapshotInfo {
    private final BaseTableInfo baseTableInfo;
    private final Table baseTable;

    // partition's base info to be used in `updateMeta`
    Map<String, MaterializedView.BasePartitionInfo> refreshedPartitionInfos = Maps.newHashMap();

    public TableSnapshotInfo(BaseTableInfo baseTableInfo, Table baseTable) {
        Objects.requireNonNull(baseTableInfo);
        Objects.requireNonNull(baseTable);
        this.baseTableInfo = baseTableInfo;
        this.baseTable = baseTable;
    }

    public BaseTableInfo getBaseTableInfo() {
        return baseTableInfo;
    }

    public long getId() {
        return baseTable.getId();
    }

    public String getName() {
        return baseTable.getName();
    }

    /**
     * NOTE: Base table is only copied from the real table if it's an OlapTable or MaterializedView,
     * otherwise the real table is returned.
     */
    public Table getBaseTable() {
        return baseTable;
    }

    public Map<String, MaterializedView.BasePartitionInfo> getRefreshedPartitionInfos() {
        return refreshedPartitionInfos;
    }

    public void setRefreshedPartitionInfos(Map<String, MaterializedView.BasePartitionInfo> refreshedPartitionInfos) {
        this.refreshedPartitionInfos = refreshedPartitionInfos;
    }

    @Override
    public String toString() {
        return "baseTable=" + baseTable.getName() +
                ", refreshedPartitionInfos=" + refreshedPartitionInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableSnapshotInfo that = (TableSnapshotInfo) o;
        return Objects.equals(baseTableInfo, that.baseTableInfo) &&
                Objects.equals(baseTable, that.baseTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseTableInfo, baseTable);
    }
}