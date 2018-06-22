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
package com.tmobile.opensource.casquatch.models.system_schema;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;

@Table(
        keyspace = "system_schema",
        name="columns"
)

public class Columns extends AbstractCassandraTable {

    @PartitionKey
    @Column(name = "keyspace_name")
    private String keyspaceName;

    @ClusteringColumn(0)
    @Column(name = "table_name")
    private String tableName;

    @ClusteringColumn(1)
    @Column(name = "column_name")
    private String columnName;

    @Column(name = "clustering_order")
    private String clusteringOrder;

    @Column(name = "kind")
    String kind;

    @Column(name = "position")
    int position;

    @Column(name = "type")
    String type;

    public Columns() {
    }

    public Columns(String keyspaceName, String tableName, String columnName) {
        this.setKeyspaceName(keyspaceName);
        this.setTableName(tableName);
        this.setColumnName(columnName);
    }

    @Transient
    public Object[] getID() {
        return new Object[]{this.getKeyspaceName(), this.getTableName(), this.getColumnName()};
    }

    public String getKeyspaceName() {return this.keyspaceName; }
    public void setKeyspaceName(String keyspaceName) { this.keyspaceName = keyspaceName; }

    public String getTableName() {return this.tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getColumnName() {return this.columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public String getClusteringOrder() {return this.clusteringOrder; }
    public void setClusteringOrder(String keyspaceName) { this.clusteringOrder = clusteringOrder; }

    public String getKind() {return this.kind; }
    public void setKind(String kind) { this.kind = kind; }

    public int getPosition() {return this.position; }
    public void setPosition(int position) { this.position = position; }

    public String getType() {return this.type; }
    public void setType(String type) { this.type = type; }
}
