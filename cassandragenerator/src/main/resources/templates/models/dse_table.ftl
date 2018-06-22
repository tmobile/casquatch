package com.tmobile.opensource.casquatch.models.${keyspace};

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;

<#list imports as import>
import ${import};
</#list> 

/**
 * Generated: Generated class for ${keyspace}.${table}
 */ 
@Table(
    keyspace = "${keyspace}",
    name="${table}"
)
public class ${className} extends AbstractCassandraTable {

    <#list columns as col>
    ${col.annotations}
    private ${col.javaType} ${col.varName};
    </#list>

    /**
     * Generated: Empty Initializer
     */     
    public ${className}() {}    

    /**
     * Generated: Initalize with Partition Keys
<#list partitionKeys as col>
     * @param ${col.varName} Partition Key Named ${col.varName}
</#list>
     */    
    public ${className}(<#list partitionKeys as col>${col.javaType} ${col.varName}<#sep>,</#sep></#list>) {
        <#list partitionKeys as col>
        this.${col.varName} = ${col.varName};
        </#list>
    } 
   
    <#if clusteringKeys?has_content>    
    /**
     * Generated: Initalize with Partition and Clustering Keys
<#list partitionKeys as col>
     * @param ${col.varName} Partition Key Named ${col.varName}
</#list>
<#list clusteringKeys as col>
     * @param ${col.varName} Clustering Key Named ${col.varName}
</#list>
     */ 
    public ${className}(<#list partitionKeys as col>${col.javaType} ${col.varName}<#sep>,</#sep></#list>,<#list clusteringKeys as col>${col.javaType} ${col.varName}<#sep>,</#sep></#list>) {
        <#list partitionKeys as col>
        this.${col.varName} = ${col.varName};
        </#list>       
        <#list clusteringKeys as col>
        this.${col.varName} = ${col.varName};
        </#list>
    } 
    </#if>   

    /**
     * Generated: Implement getID function from AbstractCassandraTable
     * @return Array of keys
     */ 
    @Transient
    public Object[] getID() {
        return new Object[]{<#list partitionKeys as col>this.get${col.procName}()<#sep>,</#sep></#list><#if clusteringKeys?has_content>,<#list clusteringKeys as col>this.get${col.procName}()<#sep>,</#sep></#list></#if>};
    }
    
    <#list columns as col> 
    /**
     * Generated: Get procedure for ${col.procName}
     * @return Value of ${col.procName}
     */ 
    public ${col.javaType} get${col.procName}() {
        return this.${col.varName};
    }
    
    /**
     * Generated: Set procedure for ${col.procName}
     * @param ${col.varName} value to set
     */ 
    public void set${col.procName}(${col.javaType} ${col.varName}) {
        this.${col.varName} = ${col.varName};
    }
    </#list>

}
