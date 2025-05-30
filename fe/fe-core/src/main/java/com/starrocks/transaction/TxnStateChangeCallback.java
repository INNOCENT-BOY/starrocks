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

// This file is based on code available under the Apache license here:
//   https://github.com/apache/incubator-doris/blob/master/fe/fe-core/src/main/java/org/apache/doris/transaction/TxnStateChangeCallback.java

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.starrocks.transaction;

import com.starrocks.common.StarRocksException;

public interface TxnStateChangeCallback {

    long getId();

    /**
     * this interface is executed before txn committed, it will check if txn could be commit
     *
     * @param txnState
     * @throws TransactionException if transaction could not be commit or there are some exception before committed,
     *                              it will throw this exception. The txn will be committed failed.
     */
    void beforeCommitted(TransactionState txnState) throws TransactionException;

    /**
     * this interface is executed before txn aborted, it will check if txn could be abort
     *
     * @param txnState
     * @throws TransactionException if transaction could not be abort or there are some exception before aborted,
     *                              it will throw this exception. The txn will be aborted failed.
     */
    void beforeAborted(TransactionState txnState) throws TransactionException;

    /**
     * update globalStateMgr of job which has related txn after transaction has been committed
     *
     * @param txnState
     */
    void afterCommitted(TransactionState txnState, boolean txnOperated) throws StarRocksException;

    void replayOnCommitted(TransactionState txnState);

    /**
     * this interface is executed when transaction has been aborted
     *
     * @param txnState
     * @param txnStatusChangeReason maybe null
     * @return
     */
    void afterAborted(TransactionState txnState, boolean txnOperated, String txnStatusChangeReason)
            throws StarRocksException;

    void replayOnAborted(TransactionState txnState);

    void afterVisible(TransactionState txnState, boolean txnOperated);

    void replayOnVisible(TransactionState txnState);

    void beforePrepared(TransactionState txnState) throws TransactionException;

    void afterPrepared(TransactionState txnState, boolean txnOperated) throws StarRocksException;

    void replayOnPrepared(TransactionState txnState);
}
