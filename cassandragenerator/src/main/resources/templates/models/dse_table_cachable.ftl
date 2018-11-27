package ${package};

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.tmobile.opensource.casquatch.models.AbstractCachable;

//Data Types
import java.lang.String;
import java.nio.ByteBuffer;
import com.datastax.driver.core.LocalDate;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.UUID;
import java.math.BigInteger;

/**
 * Generated: Generated class for ${keyspace}.${table}
 */ 
@Table(
    keyspace = "${keyspace}",
    name="${table}"
)
public class ${className} extends AbstractCachable {

    <#list columns as col>
    <#switch col.kind>
        <#case "partition_key">
    @PartitionKey(${col.position})
        <#break>
        <#case "clustering">
    @ClusteringColumn(${col.position})
        <#break>
    </#switch>    
    @Column(name="${col.columnName}")
    private ${col.javaType} ${col.varName};
    <#sep>
    
    </#sep>
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
    } 
    </#if>   

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
        <#assign ordinal=0>
        <#list partitionKeys as col>
        this.set${col.procName}(keyVals[${ordinal}]);
        <#assign ordinal=ordinal+1>
        </#list>
        <#list clusteringKeys as col>
        this.set${col.procName}(keyVals[${ordinal}]);
        <#assign ordinal=ordinal+1>
        </#list>
    }
    
    /**
     * Generated: Get the cache key which is a dot separated list of primary keys
     * @return key dot separated primary key
     */
    @Transient
    public String getCacheKey() {
        return <#list partitionKeys as col>this.get${col.procName}()<#sep>+"."+</#sep></#list><#if clusteringKeys?has_content>+"."+<#list clusteringKeys as col>this.get${col.procName}()<#sep>,</#sep></#list></#if>;
    }  

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
