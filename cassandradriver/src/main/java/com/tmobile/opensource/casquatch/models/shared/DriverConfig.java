/* Copyright 2018 T-Mobile US, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tmobile.opensource.casquatch.models.shared;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.tmobile.opensource.casquatch.models.AbstractCachable;

/**
 * Generated: Generated class for acms_odp.driver_config
 */
@Table(
        name="driver_config"
)
public class DriverConfig extends AbstractCachable {

    @Column(name="data_center")
    private String dataCenter;

    @Column(name="read_consistency")
    private String readConsistency;

    @PartitionKey(0)
    @Column(name="table_name")
    private String tableName;

    @Column(name="write_consistency")
    private String writeConsistency;

    /**
     * Generated: Empty Initializer
     */
    public DriverConfig() {}

    /**
     * Generated: Initalize with Partition Keys
     * @param tableName Partition Key Named tableName
     */
    public DriverConfig(String tableName) {
        this.tableName = tableName;
    }


    /**
     * Generated: Set the primary keys based on provided key
     * @param key dot separated primary key
     */
    @Transient
    public void setCacheKey(String key) {
        String keyVals[];
        if(key.contains(".")) {
            keyVals = key.split(".");
        } else {
            keyVals = new String[] {key};
        }
        this.setTableName(keyVals[0]);
    }

    /**
     * Generated: Get the cache key which is a dot separated list of primary keys
     * @return key dot separated primary key
     */
    @Transient
    public String getCacheKey() {
        return this.getTableName();
    }

    /**
     * Generated: Implement getID function from AbstractCassandraTable
     * @return Array of keys
     */
    @Transient
    public Object[] getID() {
        return new Object[]{this.getTableName()};
    }

    /**
     * Generated: Get procedure for DataCenter
     * @return Value of DataCenter
     */
    public String getDataCenter() {
        return this.dataCenter;
    }

    /**
     * Generated: Set procedure for DataCenter
     * @param dataCenter value to set
     */
    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }
    /**
     * Generated: Get procedure for ReadConsistency
     * @return Value of ReadConsistency
     */
    public String getReadConsistency() {
        return this.readConsistency;
    }

    /**
     * Generated: Set procedure for ReadConsistency
     * @param readConsistency value to set
     */
    public void setReadConsistency(String readConsistency) {
        this.readConsistency = readConsistency;
    }
    /**
     * Generated: Get procedure for TableName
     * @return Value of TableName
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Generated: Set procedure for TableName
     * @param tableName value to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    /**
     * Generated: Get procedure for WriteConsistency
     * @return Value of WriteConsistency
     */
    public String getWriteConsistency() {
        return this.writeConsistency;
    }

    /**
     * Generated: Set procedure for WriteConsistency
     * @param writeConsistency value to set
     */
    public void setWriteConsistency(String writeConsistency) {
        this.writeConsistency = writeConsistency;
    }

}
