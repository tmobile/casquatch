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
package com.tmobile.opensource.casquatch.models.junittest;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;

import com.datastax.driver.mapping.annotations.Frozen;
import com.tmobile.opensource.casquatch.models.junittest.JunitUdt;
import java.util.UUID;

/**
 * Generated: Generated class for junittest.junit_udt_table
 */
@Table(
    keyspace = "junittest",
    name="junit_udt_table"
)
public class JunitUdtTable extends AbstractCassandraTable {

    @Frozen
		@Column(name="udt")
		//Cassandra Type: frozen<junit_udt>
    private JunitUdt udt;
    @PartitionKey(0)
		@Column(name="id")
		//Cassandra Type: uuid
    private UUID id;

    /**
     * Generated: Empty Initializer
     */
    public JunitUdtTable() {}

    /**
     * Generated: Initalize with Partition Keys
     * @param id Partition Key Named id
     */
    public JunitUdtTable(UUID id) {
        this.id = id;
    }


    /**
     * Generated: Implement getID function from AbstractCassandraTable
     * @return Array of keys
     */
    @Transient
    public Object[] getID() {
        return new Object[]{this.getId()};
    }

    /**
     * Generated: Get procedure for Udt
     * @return Value of Udt
     */
    public JunitUdt getUdt() {
        return this.udt;
    }

    /**
     * Generated: Set procedure for Udt
     * @param udt value to set
     */
    public void setUdt(JunitUdt udt) {
        this.udt = udt;
    }
    /**
     * Generated: Get procedure for Id
     * @return Value of Id
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Generated: Set procedure for Id
     * @param id value to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

}
