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

import java.util.List;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;

/**
 * Generated: Generated class for system_schema.types
 */ 
@Table(
    keyspace = "system_schema",
    name="types"
)
public class Types extends AbstractCassandraTable {

    @Column(name="field_names")
    private List<String> fieldNames;
    @Column(name="field_types")
    private List<String> fieldTypes;
    @PartitionKey(0)
    @Column(name="keyspace_name")
    private String keyspaceName;
    @ClusteringColumn(0)
    @Column(name="type_name")
    private String typeName;

    /**
     * Generated: Empty Initializer
     */     
    public Types() {}    

    /**
     * Generated: Initalize with Partition Keys
     * @param keyspaceName Partition Key Named keyspaceName
     */    
    public Types(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    } 
   
    /**
     * Generated: Initalize with Partition and Clustering Keys
     * @param keyspaceName Partition Key Named keyspaceName
     * @param typeName Clustering Key Named typeName
     */ 
    public Types(String keyspaceName,String typeName) {
        this.keyspaceName = keyspaceName;
        this.typeName = typeName;
    } 

    /**
     * Generated: Implement getID function from AbstractCassandraTable
     * @return Array of keys
     */ 
    @Transient
    public Object[] getID() {
        return new Object[]{this.getKeyspaceName(),this.getTypeName()};
    }
    
    /**
     * Generated: Get procedure for FieldNames
     * @return Value of FieldNames
     */ 
    public List<String> getFieldNames() {
        return this.fieldNames;
    }
    
    /**
     * Generated: Set procedure for FieldNames
     * @param fieldNames value to set
     */ 
    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }
    /**
     * Generated: Get procedure for FieldTypes
     * @return Value of FieldTypes
     */ 
    public List<String> getFieldTypes() {
        return this.fieldTypes;
    }
    
    /**
     * Generated: Set procedure for FieldTypes
     * @param fieldTypes value to set
     */ 
    public void setFieldTypes(List<String> fieldTypes) {
        this.fieldTypes = fieldTypes;
    }
    /**
     * Generated: Get procedure for KeyspaceName
     * @return Value of KeyspaceName
     */ 
    public String getKeyspaceName() {
        return this.keyspaceName;
    }
    
    /**
     * Generated: Set procedure for KeyspaceName
     * @param keyspaceName value to set
     */ 
    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }
    /**
     * Generated: Get procedure for TypeName
     * @return Value of TypeName
     */ 
    public String getTypeName() {
        return this.typeName;
    }
    
    /**
     * Generated: Set procedure for TypeName
     * @param typeName value to set
     */ 
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

}
