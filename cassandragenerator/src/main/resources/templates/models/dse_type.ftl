package com.tmobile.opensource.casquatch.models.${type.keyspaceName};

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

<#list imports as import>
import ${import};
</#list> 

/**
 * Generated: Generated UDT class for ${type.keyspaceName}.${type.typeName}
 */ 
@UDT(
    keyspace = "${type.keyspaceName}",
    name="${type.typeName}"
)
public class ${className} {

    <#list columns as col> 
    @Field(name="${col.columnName}")
    private ${col.javaType} ${col.varName};
    </#list>
    
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
    
    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Unable to convert to JSON";
        }
    }

}
