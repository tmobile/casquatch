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
<#if package?has_content>
package ${package};
</#if>

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.data.GettableByName;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.tmobile.opensource.casquatch.AbstractStatementFactory;
import com.tmobile.opensource.casquatch.CasquatchNamingConvention;
import com.tmobile.opensource.casquatch.QueryOptions;
import com.tmobile.opensource.casquatch.DriverException;
import lombok.extern.slf4j.Slf4j;

<#list imports as import>
import ${import};
</#list>

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;

@Slf4j
public class ${naming.classToStatementFactory(naming.classToSimpleClass(class))} extends AbstractStatementFactory<${naming.classToSimpleClass(class)}> {

<#if udtFields?has_content>
    <#list udtFields as field,type>
    protected ${naming.classToTypeFactory(naming.classToSimpleClass(type))} ${naming.classToVar(naming.classToTypeFactory(naming.classToSimpleClass(type)))};
    </#list>
</#if>

    public ${naming.classToStatementFactory(naming.classToSimpleClass(class))}(CqlSession session) {
        super(${naming.classToSimpleClass(class)}.class,session);
<#if udtFields?has_content>
    <#list udtFields as field,type>
        ${naming.classToVar(naming.classToTypeFactory(naming.classToSimpleClass(type)))} = new ${naming.classToTypeFactory(naming.classToSimpleClass(type))}(
                session.getMetadata()
                        .getKeyspace(session.getKeyspace().orElseThrow(() -> new DriverException(DriverException.CATEGORIES.CASQUATCH_INVALID_CONFIGURATION, "Keyspace not defined")))
                        .flatMap(ks -> ks.getUserDefinedType("${naming.javaClassToCql(naming.classToSimpleClass(type))}")).orElseThrow(() -> new DriverException(DriverException.CATEGORIES.CASQUATCH_MISSING_GENERATED_CLASS, "Missing UDT definition"))
        );
    </#list>
</#if>
    }

    @Override
    protected Select selectWhereObject(Select select, ${naming.classToSimpleClass(class)} ${naming.classToVar(naming.classToSimpleClass(class))}, QueryOptions options) {

<#list keyFields as field,type>
        if(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()!=null) {
            select=select.whereColumn("${naming.javaVariableToCql(field)}").isEqualTo(bindMarker());
        }
</#list>
        if(!options.getIgnoreNonPrimaryKeys()) {
<#list nonKeyFields as field,type>
            if(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()!=null) {
                if(!allowQueryByType(${type}.class)) {
                    log.warn("Ignorning column ${field}. ${type} is not a supported query column type");
                }
                else {
                    select=select.whereColumn("${naming.javaVariableToCql(field)}").isEqualTo(bindMarker());
                }
            }
</#list>
<#list udtFields as field,type>
            if(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()!=null) {
                select=select.whereColumn("${naming.javaVariableToCql(field)}").isEqualTo(bindMarker());
            }
</#list>
        }
        return select;
    }

    @Override
    protected RegularInsert insertObject(${naming.classToSimpleClass(class)} ${naming.classToVar(naming.classToSimpleClass(class))}, QueryOptions options) {
        RegularInsert insert=null;
<#list keyFields as field,type>
        if(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("${naming.javaVariableToCql(field)}",bindMarker());
        }
</#list>
<#list nonKeyFields as field,type>
        if(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("${naming.javaVariableToCql(field)}",bindMarker());
        }
</#list>
<#list udtFields as field,type>
        if(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()!=null || options.getPersistNulls()) {
            insert=(insert==null?insertStart:insert).value("${naming.javaVariableToCql(field)}",bindMarker());
        }
</#list>
        return insert;
    }

    @Override
    protected Delete deleteObject(${naming.classToSimpleClass(class)} ${naming.classToVar(naming.classToSimpleClass(class))}, QueryOptions options) {
        Delete delete=null;
<#list keyFields as field,type>
        if(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()!=null) {
            delete=(delete==null?deleteStart:delete).whereColumn("${naming.javaVariableToCql(field)}").isEqualTo(bindMarker());
       }
</#list>
        return delete;
    }

    @Override
    public CqlIdentifier getTableName() {
        return CqlIdentifier.fromCql("${table}");
    }

    @Override
    protected BoundStatementBuilder bindObject(BoundStatementBuilder boundStatementBuilder, ${naming.classToSimpleClass(class)} ${naming.classToVar(naming.classToSimpleClass(class))}, QueryOptions options) {
<#list keyFields as field,type>
        if(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()!=null || options.getPersistNulls()) {
            boundStatementBuilder = bindIfMarked(boundStatementBuilder,"${naming.javaVariableToCql(field)}", ${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}(), ${type}.class);
        }
</#list>
        if(!options.getIgnoreNonPrimaryKeys()) {
<#list nonKeyFields as field,type>
            if(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()!=null || options.getPersistNulls()) {
                boundStatementBuilder = bindIfMarked(boundStatementBuilder,"${naming.javaVariableToCql(field)}", ${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}(), ${type}.class);
            }
</#list>
<#list udtFields as field,type>
            if(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()!=null || options.getPersistNulls()) {
                boundStatementBuilder=bindIfMarked(boundStatementBuilder,"${naming.javaVariableToCql(field)}",${naming.classToVar(naming.classToTypeFactory(naming.classToSimpleClass(type)))}.toUdtValue(${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaGet(field)}()), UdtValue.class);
            }
</#list>
        }
        return boundStatementBuilder;
    }

    @Override
    public ${naming.classToSimpleClass(class)} map(GettableByName source) {
        ${naming.classToSimpleClass(class)} ${naming.classToVar(naming.classToSimpleClass(class))} = new ${naming.classToSimpleClass(class)}();
<#list keyFields as field,type>
        if(!source.isNull("${naming.javaVariableToCql(field)}")) {
            ${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaSet(field)}(source.get("${naming.javaVariableToCql(field)}",${naming.classToSimpleClass(type)}.class));
        }
</#list>
<#list nonKeyFields as field,type>
        if(!source.isNull("${naming.javaVariableToCql(field)}")) {
            ${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaSet(field)}(source.get("${naming.javaVariableToCql(field)}",${naming.classToSimpleClass(type)}.class));
        }
</#list>
<#list udtFields as field,type>
        if(!source.isNull("${naming.javaVariableToCql(field)}")) {
            ${naming.classToVar(naming.classToSimpleClass(class))}.${naming.javaVariableToJavaSet(field)}(${naming.classToVar(naming.classToTypeFactory(naming.classToSimpleClass(type)))}.fromUdtValue(source.getUdtValue("${naming.javaVariableToCql(field)}")));
        }
</#list>
        return ${naming.classToVar(naming.classToSimpleClass(class))};
    }

}
