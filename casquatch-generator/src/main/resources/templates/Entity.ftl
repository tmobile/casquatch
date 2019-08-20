<#if package?has_content>
package ${package};
</#if>


import com.tmobile.opensource.casquatch.AbstractCasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchEntity;
<#if !minify>
import com.tmobile.opensource.casquatch.annotation.CasquatchIgnore;
</#if>
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

<#if !minify || createTests>
import org.apache.commons.text.TextStringBuilder;
</#if>

@CasquatchEntity
@Getter @Setter @NoArgsConstructor
public class ${naming.classToSimpleClass(class)} extends AbstractCasquatchEntity {
<#list partitionKeys as cql,col>
    @PartitionKey
    private ${naming.cqlDataTypeToJavaType(col.type)} ${naming.cqlToJavaVariable(col.name)};
</#list>

<#list clusteringColumns as cql,col>
    @ClusteringColumn(${col?counter})
    private ${naming.cqlDataTypeToJavaType(col.type)} ${naming.cqlToJavaVariable(col.name)};
</#list>

<#list nonKeyColumns as cql,col>
    private ${naming.cqlDataTypeToJavaType(col.type)} ${naming.cqlToJavaVariable(col.name)};
</#list>
<#if udtColumns?has_content>
    <#list udtColumns as col,type>
    @UDT
    private ${naming.cqlToJavaClass(type)} ${naming.cqlToJavaVariable(col)};
    </#list>
</#if>

<#if !minify>
    /**
    * Generated: Initialize with Partition Keys
    <#list partitionKeys as cql,col>
        * @param ${naming.cqlToJavaVariable(col.name)} Partition Key Named ${col.name}
    </#list>
    */
    public ${naming.classToSimpleClass(class)}(<#list partitionKeys as cql,col>${naming.cqlDataTypeToJavaType(col.type)} ${naming.cqlToJavaVariable(col.name)}<#sep>,</#sep></#list>) {
    <#list partitionKeys as cql,col>
        this.${naming.cqlToJavaSet(col.name)}(${naming.cqlToJavaVariable(col.name)});
    </#list>
    }

    <#if clusteringColumns?has_content>
    /**
    * Generated: Initialize with Partition and Clustering Keys
    <#list partitionKeys as cql,col>
        * @param ${naming.cqlToJavaVariable(col.name)} Partition Key Named ${col.name}
    </#list>
    <#list clusteringColumns as cql,col>
        * @param ${naming.cqlToJavaVariable(col.name)} Clustering Key Named ${col.name}
    </#list>
    */
    public ${naming.classToSimpleClass(class)}(<#list partitionKeys as cql,col>${naming.cqlDataTypeToJavaType(col.type)} ${naming.cqlToJavaVariable(col.name)}<#sep>,</#sep></#list>,<#list clusteringColumns as cql,col>${naming.cqlDataTypeToJavaType(col.type)} ${naming.cqlToJavaVariable(col.name)}<#sep>,</#sep></#list>) {
    <#list partitionKeys as cql,col>
        this.${naming.cqlToJavaSet(col.name)}(${naming.cqlToJavaVariable(col.name)});
    </#list>
    <#list clusteringColumns as cql,col>
        this.${naming.cqlToJavaSet(col.name)}(${naming.cqlToJavaVariable(col.name)});
    </#list>
    }
    </#if>

    /**
     * Generated: Instance of object containing primary keys only
     */
    @CasquatchIgnore
    public ${naming.classToSimpleClass(class)} keys() {
        ${naming.classToSimpleClass(class)} ${naming.classToVar(naming.classToSimpleClass(class))} = new ${naming.classToSimpleClass(class)}();
<#list partitionKeys as cql,col>
        ${naming.classToVar(naming.classToSimpleClass(class))}.${naming.cqlToJavaSet(col.name)}(this.${naming.cqlToJavaGet(col.name)}());
</#list>
<#list clusteringColumns as cql,col>
        ${naming.classToVar(naming.classToSimpleClass(class))}.${naming.cqlToJavaSet(col.name)}(this.${naming.cqlToJavaGet(col.name)}());
</#list>
        return ${naming.classToVar(naming.classToSimpleClass(class))};
    }
</#if>
<#if !minify || createTests>
    /**
    * Generated: Returns DDL
    * @return DDL for table
    */
    public static String getDDL() {
        TextStringBuilder ddl = new TextStringBuilder();
        <#if udtColumns?has_content>
            <#list udtColumns as col,type>
        ddl.appendln(${naming.cqlToJavaClass(type)}.getDDL());
            </#list>
        </#if>
        ddl.appendln("${ddl}");
        return ddl.toString();
    }
</#if>
}

