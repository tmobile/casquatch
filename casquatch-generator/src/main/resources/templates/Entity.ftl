<#if package?has_content>
package ${package};
</#if>


import com.tmobile.opensource.casquatch.AbstractCasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchIgnore;
<#if clusteringColumns?has_content>
import com.tmobile.opensource.casquatch.annotation.ClusteringColumn;
</#if>
import com.tmobile.opensource.casquatch.annotation.PartitionKey;
<#if udtColumns?has_content>
import com.tmobile.opensource.casquatch.annotation.UDT;
</#if>
<#list imports as clazz>
import ${clazz};
</#list>

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.commons.text.TextStringBuilder;

@CasquatchEntity
@Getter @Setter @NoArgsConstructor
public class ${CasquatchNamingConvention.classToSimpleClass(class)} extends AbstractCasquatchEntity {
<#list partitionKeys as cql,col>
    @PartitionKey
    private ${CasquatchNamingConvention.cqlDataTypeToJavaType(col.type)} ${CasquatchNamingConvention.cqlToJavaVariable(col.name)};
</#list>

<#list clusteringColumns as cql,col>
    @ClusteringColumn(${col?counter})
    private ${CasquatchNamingConvention.cqlDataTypeToJavaType(col.type)} ${CasquatchNamingConvention.cqlToJavaVariable(col.name)};
</#list>

<#list nonKeyColumns as cql,col>
    private ${CasquatchNamingConvention.cqlDataTypeToJavaType(col.type)} ${CasquatchNamingConvention.cqlToJavaVariable(col.name)};
</#list>
<#if udtColumns?has_content>
    <#list udtColumns as col,type>
    @UDT
    private ${CasquatchNamingConvention.cqlToJavaClass(type)} ${CasquatchNamingConvention.cqlToJavaVariable(col)};
    </#list>
</#if>

    /**
    * Generated: Initialize with Partition Keys
    <#list partitionKeys as cql,col>
        * @param ${CasquatchNamingConvention.cqlToJavaVariable(col.name)} Partition Key Named ${col.name}
    </#list>
    */
    public ${CasquatchNamingConvention.classToSimpleClass(class)}(<#list partitionKeys as cql,col>${CasquatchNamingConvention.cqlDataTypeToJavaType(col.type)} ${CasquatchNamingConvention.cqlToJavaVariable(col.name)}<#sep>,</#sep></#list>) {
    <#list partitionKeys as cql,col>
        this.${CasquatchNamingConvention.cqlToJavaSet(col.name)}(${CasquatchNamingConvention.cqlToJavaVariable(col.name)});
    </#list>
    }

    <#if clusteringColumns?has_content>
    /**
    * Generated: Initialize with Partition and Clustering Keys
    <#list partitionKeys as cql,col>
        * @param ${CasquatchNamingConvention.cqlToJavaVariable(col.name)} Partition Key Named ${col.name}
    </#list>
    <#list clusteringColumns as cql,col>
        * @param ${CasquatchNamingConvention.cqlToJavaVariable(col.name)} Clustering Key Named ${col.name}
    </#list>
    */
    public ${CasquatchNamingConvention.classToSimpleClass(class)}(<#list partitionKeys as cql,col>${CasquatchNamingConvention.cqlDataTypeToJavaType(col.type)} ${CasquatchNamingConvention.cqlToJavaVariable(col.name)}<#sep>,</#sep></#list>,<#list clusteringColumns as cql,col>${CasquatchNamingConvention.cqlDataTypeToJavaType(col.type)} ${CasquatchNamingConvention.cqlToJavaVariable(col.name)}<#sep>,</#sep></#list>) {
    <#list partitionKeys as cql,col>
        this.${CasquatchNamingConvention.cqlToJavaSet(col.name)}(${CasquatchNamingConvention.cqlToJavaVariable(col.name)});
    </#list>
    <#list clusteringColumns as cql,col>
        this.${CasquatchNamingConvention.cqlToJavaSet(col.name)}(${CasquatchNamingConvention.cqlToJavaVariable(col.name)});
    </#list>
    }
    </#if>

    /**
     * Generated: Instance of object containing primary keys only
     */
    @CasquatchIgnore
    public ${CasquatchNamingConvention.classToSimpleClass(class)} keys() {
        ${CasquatchNamingConvention.classToSimpleClass(class)} ${CasquatchNamingConvention.classToVar(name)} = new ${CasquatchNamingConvention.classToSimpleClass(class)}();
<#list partitionKeys as cql,col>
        ${CasquatchNamingConvention.classToVar(name)}.${CasquatchNamingConvention.cqlToJavaSet(col.name)}(this.${CasquatchNamingConvention.cqlToJavaGet(col.name)}());
</#list>
<#list clusteringColumns as cql,col>
        ${CasquatchNamingConvention.classToVar(name)}.${CasquatchNamingConvention.cqlToJavaSet(col.name)}(this.${CasquatchNamingConvention.cqlToJavaGet(col.name)}());
</#list>
        return ${CasquatchNamingConvention.classToVar(name)};
    }

    /**
    * Generated: Returns DDL
    * @return DDL for table
    */
    public static String getDDL() {
        TextStringBuilder ddl = new TextStringBuilder();
        <#if udtColumns?has_content>
            <#list udtColumns as col,type>
        ddl.appendln(${CasquatchNamingConvention.cqlToJavaClass(type)}.getDDL());
            </#list>
        </#if>
        ddl.appendln("${ddl}");
        return ddl.toString();
    }
}

