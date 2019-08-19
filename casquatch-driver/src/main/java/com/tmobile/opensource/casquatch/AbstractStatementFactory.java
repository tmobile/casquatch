/*
 * Copyright 2018 T-Mobile US, Inc.
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

package com.tmobile.opensource.casquatch;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.data.GettableByName;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.querybuilder.BuildableQuery;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.delete.DeleteSelection;
import com.datastax.oss.driver.api.querybuilder.insert.InsertInto;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;

/**
 * Abstract Statement Factory to generate statements
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public abstract class AbstractStatementFactory<E extends AbstractCasquatchEntity> {

    /**
     * Internal class to represent an entity with only a SolrQuery column
     */
    @AllArgsConstructor
    private class SolrQueryEntity {
        @Getter String solrQuery;
    }

    protected final DeleteSelection deleteStart;
    protected final InsertInto insertStart;
    protected final Select selectAllStart;
    protected final Select selectCountStart;
    protected final Select selectSolrStart;
    protected final Select selectSolrCountStart;
    protected final CqlSession session;
    protected final Class<E> entityClass;

    /**
     * Query Factory Constructor
     * @param entityClass Class for table object
     * @param session bind to session
     */
    public AbstractStatementFactory(Class<E> entityClass, CqlSession session) {
        this.deleteStart = QueryBuilder.deleteFrom(this.getTableName());
        this.insertStart=QueryBuilder.insertInto(this.getTableName());
        this.selectCountStart=QueryBuilder.selectFrom(this.getTableName()).countAll();
        this.selectAllStart=QueryBuilder.selectFrom(this.getTableName()).all();
        this.selectSolrStart=selectAllStart.whereColumn("solr_query").isEqualTo(bindMarker());
        this.selectSolrCountStart=selectCountStart.whereColumn("solr_query").isEqualTo(bindMarker());
        this.session=session;
        this.entityClass=entityClass;
    }

    /**
     * Populate bind values of query using the provided object
     * @param boundStatementBuilder bound statement to bind to
     * @param obj partially populated object
     * @param queryOptions query options to apply
     * @return bound statement builder containing bound values
     */
    protected abstract BoundStatementBuilder bindObject(BoundStatementBuilder boundStatementBuilder, E obj, QueryOptions queryOptions);

    /**
     * Create delete statement for object
     * @param obj populated object
     * @param queryOptions query options to apply
     * @return delete statement containing object
     */
    protected abstract Delete deleteObject(E obj, QueryOptions queryOptions);

    /**
     * Get the table name
     * @return table name reference
     */
    public abstract CqlIdentifier getTableName();

    /**
     * Create insert statement for object
     * @param obj populated object
     * @param queryOptions query options to apply
     * @return insert statement containing object
     */
    protected abstract RegularInsert insertObject(E obj, QueryOptions queryOptions);

    /**
     * Map a source to an object
     * @param source source, generally a row from a resultset
     * @return populated object
     */
    protected abstract E map(GettableByName source);

    /**
     * Append a where clause to a select query using non-null fields found in the provided object
     * @param select select to append to
     * @param obj partially populated object
     * @param queryOptions query options to apply
     * @return select object containing where clause
     */
    protected abstract Select selectWhereObject(Select select, E obj, QueryOptions queryOptions);

    /**
     * Wrapper to filter out classes which cannot be queried
     * @param clazz class to check
     * @return boolean indicating if class can be mapped
     */
    protected Boolean allowQueryByType(Class clazz) {
        if(clazz.equals(List.class) || clazz.equals(Set.class) || clazz.equals(Map.class) || clazz.equals(ByteBuffer.class)) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Wrapper to bind a variable if it is marked
     * @param boundStatementBuilder bound statement builder reference
     * @param field field to bind
     * @param value value to bind
     * @param fieldClass class of field
     * @param <T> generic for field
     * @return bound statement builder with object bound
     */
    protected <T> BoundStatementBuilder bindIfMarked(BoundStatementBuilder boundStatementBuilder, String field, T value, Class<T> fieldClass) {
        try {
            if (fieldClass.equals(UdtValue.class)) {
                return boundStatementBuilder.setUdtValue(field, (UdtValue) value);
            } else {
                return boundStatementBuilder.set(field, value, fieldClass);
            }
        }
        catch (java.lang.IllegalArgumentException e) {
            log.trace("Failed to bind {}: {}",field,e.getMessage());
            return boundStatementBuilder;
        }
    }

    /**
     * Populate bind values of query using the provided object
     * @param boundStatementBuilder bound statement to bind to
     * @param obj partially populated object
     * @param queryOptions query options to apply
     * @return bound statement builder containing bound values
     */
    protected BoundStatementBuilder bindObject(BoundStatementBuilder boundStatementBuilder, SolrQueryEntity obj, QueryOptions queryOptions) {
        return boundStatementBuilder.setString("solr_query",obj.getSolrQuery());
    }

    /**
     * Produce a bound statement while applying object and query options
     * @param buildableQuery buildable query
     * @param obj populated object
     * @param queryOptions query options to apply
     * @param bindToSession override session
     * @return bound statement
     */
    protected BoundStatement buildBoundStatement(BuildableQuery buildableQuery, Object obj, QueryOptions queryOptions, CqlSession bindToSession) {
        if(buildableQuery instanceof Select) {
            if(queryOptions!=null) {
                if(queryOptions.getLimit()!=null) {
                    buildableQuery = ((Select) buildableQuery).limit(queryOptions.getLimit());
                }
            }
        }
        else if (buildableQuery instanceof RegularInsert) {
            if(queryOptions.getTtl()!=null) {
                buildableQuery = ((RegularInsert) buildableQuery).usingTtl(queryOptions.getTtl());
            }
        }
        SimpleStatement simpleStatement = buildableQuery.build();

        if(log.isTraceEnabled()) log.trace("Preparing Statement {}",simpleStatement.getQuery());
        BoundStatementBuilder boundStatementBuilder = bindToSession.prepare(simpleStatement).boundStatementBuilder();
        if(obj.getClass().equals(this.entityClass)) {
            //noinspection unchecked
            boundStatementBuilder=bindObject(boundStatementBuilder,(E) obj,queryOptions);
        }
        else if (obj.getClass().equals(SolrQueryEntity.class)) {
            //noinspection unchecked
            boundStatementBuilder=bindObject(boundStatementBuilder,(SolrQueryEntity) obj,queryOptions);
        }
        else {
            throw new DriverException(DriverException.CATEGORIES.CASQUATCH_MISSING_GENERATED_CLASS, "Unknown class");
        }
        if(queryOptions!=null) {
            if (queryOptions.getConsistencyLevel() != null) {
                boundStatementBuilder = boundStatementBuilder.setConsistencyLevel(queryOptions.getConsistencyLevel());
            }
            if (queryOptions.getProfile() != null) {
                boundStatementBuilder = boundStatementBuilder.setExecutionProfileName(queryOptions.getProfile());
            }
        }
        return boundStatementBuilder.build();
    }

    /**
     * Create a count statement for an object
     *
     * Example: select count(*) from TABLE where KEY=?
     *
     * @param obj partially populated object
     * @param queryOptions query options to apply
     * @return bound statement for the query
     */
    public BoundStatement count(E obj, QueryOptions queryOptions) {
        return buildBoundStatement(selectWhereObject(selectCountStart, obj, queryOptions),obj, queryOptions, this.session);
    }

    /**
     * Create a count statement for a solr query
     *
     * Example: select count(*) from TABLE where solr_query=?
     *
     * @param solrQuery solrQuery to search
     * @param queryOptions query options to apply
     * @return bound statement for the query
     */
    public BoundStatement countSolr(String solrQuery, QueryOptions queryOptions) {
        return buildBoundStatement(selectSolrCountStart,new SolrQueryEntity(solrQuery), queryOptions, this.session);
    }

    /**
     * Create a delete statement for an object
     *
     * Example: delete from TABLE where KEY=?
     *
     * @param obj partially populated object
     * @param queryOptions query options to apply
     * @return bound statement for the query
     */
    public BoundStatement delete(E obj, QueryOptions queryOptions) {
        return buildBoundStatement(deleteObject(obj, queryOptions), obj,queryOptions,this.session);
    }

    /**
     * Create a get statement for an object
     *
     * Example: select [COL1...COLN] from TABLE where KEY=?
     *
     * @param obj partially populated object
     * @param queryOptions query options to apply
     * @return bound statement for the query
     */
    public BoundStatement get(E obj, QueryOptions queryOptions) {
        return buildBoundStatement(selectWhereObject(selectAllStart, obj, queryOptions), obj,queryOptions,this.session);
    }

    /**
     * Create a get statement for a solr query
     *
     * Example: select [COL1...COLN] from TABLE where solr_query=?
     *
     * @param solrQuery solr query
     * @param queryOptions query options to apply
     * @return bound statement for the query
     */
    public BoundStatement getSolr(String solrQuery, QueryOptions queryOptions) {
        return buildBoundStatement(selectSolrStart,new SolrQueryEntity(solrQuery),queryOptions,this.session);
    }

    /**
     * Create a save statement for an object
     *
     * Example: INSERE INTO TABLE ([COL1..COLN]) VALUES([?..?])
     *
     * @param obj partially populated object
     * @param queryOptions query options to apply
     * @return simple statement for the query
     */
    public BoundStatement save(E obj, QueryOptions queryOptions) {
        return buildBoundStatement(insertObject(obj,queryOptions),obj, queryOptions,this.session);
    }



}
