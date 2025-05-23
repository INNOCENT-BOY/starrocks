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

package com.starrocks.scheduler.mv;

import com.starrocks.catalog.OlapTable;
import com.starrocks.catalog.Partition;
import com.starrocks.catalog.Table;
import com.starrocks.qe.ConnectContext;
import com.starrocks.qe.SimpleExecutor;
import com.starrocks.sql.optimizer.rule.transformation.partition.PartitionSelector;
import com.starrocks.statistic.StatisticExecutor;
import com.starrocks.statistic.StatisticSQLBuilder;
import com.starrocks.statistic.StatisticUtils;
import com.starrocks.thrift.TResultBatch;
import com.starrocks.thrift.TResultSinkType;
import com.starrocks.thrift.TStatisticData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MVRefreshPartitionSelector {

    private long currentTotalRows = 0;
    private long currentTotalBytes = 0;
    private int currentTotalSelectedPartitions = 0;

    private final long maxRowsThreshold;
    private final long maxBytesThreshold;
    private final int maxSelectedPartitions;

    // ensure that at least one partition is selected
    private boolean isFirstPartition = true;

    private StatisticExecutor statisticExecutor = new StatisticExecutor();
    private final SimpleExecutor executor = new SimpleExecutor("SPMExecutor", TResultSinkType.HTTP_PROTOCAL);

    public MVRefreshPartitionSelector(long maxRowsThreshold, long maxBytesThreshold, int maxPartitionNum) {
        this.maxRowsThreshold = maxRowsThreshold;
        this.maxBytesThreshold = maxBytesThreshold;
        this.maxSelectedPartitions = maxPartitionNum;
    }

    /**
     * Check if the incoming partition set can be added based on current total usage and thresholds.
     * Always allows the first partition.
     */
    public boolean canAddPartition(Map<Table, Set<String>> partitionSet) {
        if (isFirstPartition) {
            return true;
        }

        if (currentTotalSelectedPartitions >= maxSelectedPartitions) {
            return false;
        }

        long[] usage = estimatePartitionUsage(partitionSet);
        long incomingRows = usage[0];
        long incomingBytes = usage[1];

        return (currentTotalRows + incomingRows <= maxRowsThreshold) &&
                (currentTotalBytes + incomingBytes <= maxBytesThreshold);
    }

    /**
     * Actually add the given partition set to the total usage.
     * This should be called after canAddPartitionSet() returns true.
     */
    public void addPartition(Map<Table, Set<String>> partitionSet) {
        long[] usage = estimatePartitionUsage(partitionSet);
        currentTotalRows += usage[0];
        currentTotalBytes += usage[1];
        isFirstPartition = false;
        currentTotalSelectedPartitions++;
    }

    /**
     * Estimate total row count and data size of a partition set.
     *
     * @return array of [rows, bytes]
     */
    private long[] estimatePartitionUsage(Map<Table, Set<String>> partitionSet) {
        long totalRows = 0;
        long totalBytes = 0;

        for (Map.Entry<Table, Set<String>> entry : partitionSet.entrySet()) {
            Table table = entry.getKey();
            Map<String, Pair<Long, Long>> partitionToStats = getPartitionStats(table);
            for (String partitionName : entry.getValue()) {
                Pair<Long, Long> stat = partitionToStats.get(partitionName);
                totalRows += stat.getLeft();
                totalBytes += stat.getRight();
            }
        }
        return new long[] {totalRows, totalBytes};
    }

    /**
     * Returns a map of partition statistics for the given table.
     * Each entry in the map represents a partition, with the key being the partition name,
     * and the value being a pair of (rowCount, dataSize).
     * For internal OLAP tables, statistics are retrieved directly from the partition metadata.
     * For external tables, only Hive, Iceberg, Hudi, and Delta Lake are currently supported for statistics collection.
     * Statistics for these external tables are retrieved via the statistic executor and may be partial.
     * Note: For external tables, statistics are aggregated by partition name,
     * since there may be multiple statistic records for the same partition.
     *
     * @param table the table to collect statistics from
     * @return a map of partition name to (rowCount, dataSize)
     */
    private Map<String, Pair<Long, Long>> getPartitionStats(Table table) {
        Map<String, Pair<Long, Long>> result = new HashMap<>();

        if (table instanceof OlapTable) {
            OlapTable olapTable = (OlapTable) table;
            for (Partition partition : olapTable.getPartitions()) {
                String partitionName = partition.getName();
                long rowCount = partition.getRowCount();
                long dataSize = partition.getDataSize();
                result.put(partitionName, Pair.of(rowCount, dataSize));
            }
        } else {
            result = PartitionSelector.getExternalTablePartitionStats(table);
        }

        return result;
    }
}