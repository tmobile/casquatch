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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.session.Session;
import com.tmobile.opensource.casquatch.annotation.Rest;
import com.tmobile.opensource.casquatch.policies.FailoverPolicy;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Primary entry point for Project - Casquatch to provide object based API for entities.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public class CasquatchDao {

    private final CqlSession session;
    @Getter private final String keyspace;
    private CasquatchDaoBuilder casquatchDaoBuilder;
    private final Map<Class,Object> statementFactoryCache;
    private final QueryOptions defaultQueryOptions;
    private final QueryOptions defaultSolrQueryOptions;
    private final Config config;
    private FailoverPolicy failoverPolicy;

    /**
     * Initialize CasquatchDao via a builder
     * @param casquatchDaoBuilder reference to a builder
     */
    protected CasquatchDao(CasquatchDaoBuilder casquatchDaoBuilder) {
        this.config = casquatchDaoBuilder.getConfig();
        this.keyspace=this.config.getString("basic.session-keyspace");
        this.session=casquatchDaoBuilder.session();
        this.statementFactoryCache = new HashMap<>();
        if(this.config.hasPath("query-options")) {
            this.defaultQueryOptions=new QueryOptions(this.config.getConfig("query-options"));
        }
        else {
            this.defaultQueryOptions=new QueryOptions();
        }
        if(log.isTraceEnabled()) log.trace("Default Query Options: {}",defaultQueryOptions);
        if(this.config.hasPath("solr-query-options")) {
            this.defaultSolrQueryOptions=new QueryOptions(this.config.getConfig("solr-query-options").withFallback(this.config.getConfig("query-options")));
        }
        else {
            defaultSolrQueryOptions=defaultQueryOptions.withAllColumns();
        }
        if(log.isTraceEnabled()) log.trace("Default SolrQuery Options: {}",defaultSolrQueryOptions);
        if(this.config.hasPath("failover-policy.class")) {
            String failoverClassName="";
            try {
                failoverClassName = this.config.getString("failover-policy.class");
                if(!failoverClassName.contains(".")) {
                    failoverClassName = String.format("com.tmobile.opensource.casquatch.policies.%s",failoverClassName);
                }
                log.trace("Loading FailoverPolicy: {}",failoverClassName);
                failoverPolicy = (FailoverPolicy) Class.forName(failoverClassName).newInstance();
            } catch (Exception e) {
                throw new DriverException(DriverException.CATEGORIES.CASQUATCH_INVALID_CONFIGURATION,String.format("Unable to instantiate FailoverPolicy class %s",failoverClassName));
            }
        }
        log.info(CasquatchDao.getVersion());
    }

    /**
     * Get a new builder
     * @return new builder reference
     */
    public static CasquatchDaoBuilder builder() {
        return new CasquatchDaoBuilder();
    }

    /**
     * Returns the CassandraDriver version information-
     * @return version string
     */
    public static String getVersion() {
        InputStream resourceAsStream = CasquatchDao.class.getResourceAsStream("/maven.properties");
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);

            return "Casquatch Driver "+properties.get("version")+". Datastax Driver Java Driver "+Session.OSS_DRIVER_COORDINATES.getVersion();
        }
        catch (Exception e) {
            throw new DriverException(e);
        }
    }

    /**
     * Execute a statement and provide resultset. This wraps {@link CqlSession#execute(Statement)} with additional logic
     *
     * @param statement statement to execute
     * @return statement results
     * @throws DriverException - Driver exception mapped to error code
     */
    public ResultSet execute(Statement statement) throws DriverException {
        if(log.isTraceEnabled()) log.trace("Executing Statement with profile {}: {}", statement.getExecutionProfileName(), this.getStatementQuery(statement));
        try {
            return this.session.execute(statement);
        }
        catch (Exception e) {
            if(failoverPolicy !=null && getProfileConfig("failover-policy.profile",statement.getExecutionProfileName()) != null && failoverPolicy.shouldFailover(e,statement)) {
                log.warn("Statement Failed With Exception. Retrying on failover profile: {}", getProfileConfig("failover-policy.profile",statement.getExecutionProfileName()), new DriverException(e));
                return this.execute(statement.setExecutionProfileName(getProfileConfig("failover-policy.profile",statement.getExecutionProfileName())));
            }
            throw new DriverException(e);
        }
    }

    /**
     * Execute a statement asynchronously and provide resultset. This wraps {@link CqlSession#executeAsync(Statement)} with additional logic
     *
     * @param statement statement to execute
     * @return CompletableFuture with statement results
     * @throws DriverException - Driver exception mapped to error code
     */
    private CompletableFuture<AsyncResultSet> executeASync(Statement statement) throws DriverException  {
        if(log.isTraceEnabled()) log.trace("Executing Statement Asynchronously with profile {}: {}", statement.getExecutionProfileName(), this.getStatementQuery(statement));
        try {
            return this.session.executeAsync(statement).toCompletableFuture().handle(
                    (result, e) -> {
                        if (e instanceof Exception) {
                            if(failoverPolicy !=null && getProfileConfig("failover-policy.profile",statement.getExecutionProfileName()) != null && failoverPolicy.shouldFailover((Exception) e,statement)) {
                                log.warn("Statement Failed With Exception. Retrying on failover profile: {}", getProfileConfig("failover-policy.profile",statement.getExecutionProfileName()), new DriverException((Exception) e));
                                try {
                                    return this.executeASync(statement.setExecutionProfileName(getProfileConfig("failover-policy.profile",statement.getExecutionProfileName()))).get();
                                } catch (Exception ex) {
                                    throw new DriverException(ex);
                                }
                            }
                        }
                        return result;
                    }
            );
        }
        catch (Exception e) {
            throw new DriverException(e);
        }
    }


    /**
     * Get a config string based on a profile
     * @param path config path
     * @param profile profile name
     * @return string
     */
    private String getProfileConfig(@SuppressWarnings("SameParameterValue") String path, String profile) {
        if(profile!=null && this.config.hasPath(String.format("profiles.%s.%s",profile,path))) {
            return this.config.getString(String.format("profiles.%s.%s",profile,path));
        }
        else if(profile==null && this.config.hasPath(path)) {
            return this.config.getString(path);
        }
        return null;
    }

    /**
     * Gets a dao mapper for a given object
     * @param c Entity class name
     * @param <E> Generic entity class
     * @return dao object
     */
    private <E extends AbstractCasquatchEntity, Q extends AbstractStatementFactory<E>> Q getStatementFactory(Class<E> c) {
        if(!statementFactoryCache.containsKey(c)) {
            try {
                statementFactoryCache.put(c, Class.forName(CasquatchNamingConvention.classToStatementFactory(c.getName())).getConstructor(CqlSession.class).newInstance(this.session));
            } catch (ClassNotFoundException e) {
                throw new DriverException(DriverException.CATEGORIES.CASQUATCH_MISSING_GENERATED_CLASS, String.format("Cannot find %s", CasquatchNamingConvention.classToStatementFactory(c.getName())));
            } catch (Exception e) {
                log.error("Failed to create Statement Factory",e);
                throw new DriverException(DriverException.CATEGORIES.CASQUATCH_MISSING_GENERATED_CLASS, String.format("Failed to create %s", CasquatchNamingConvention.classToStatementFactory(c.getName())));
            }
        }
        //noinspection unchecked
        return (Q) statementFactoryCache.get(c);
    }

    /**
     * Extracts a query from a statement object
     * @param statement statement object
     * @return query depending on type
     */
    private String getStatementQuery(Statement statement) {
        if(statement instanceof SimpleStatement) {
            return ((SimpleStatement) statement).getQuery();
        }
        else if(statement instanceof BoundStatement) {
            return ((BoundStatement) statement).getPreparedStatement().getQuery();
        }
        else {
            return "Unknown";
        }
    }

    /**
     * Delete an object by passing an instance of the given object.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> Void delete(Class<E> c, E o) throws DriverException {
        return this.delete(c,o,defaultQueryOptions);
    }

    /**
     * Delete an object by passing an instance of the given object.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param queryOptions Query Options to include
     * @throws DriverException - Driver exception mapped to error code
     */
    @SuppressWarnings("SameReturnValue")
    @Rest("/delete")
    public <E extends AbstractCasquatchEntity> Void delete(Class<E> c, E o, QueryOptions queryOptions) throws DriverException {
        this.execute(this.getStatementFactory(c).delete(o,queryOptions.withPrimaryKeysOnly()));
        return null;
    }

    /**
     * Delete asynchronously an object by passing an instance of the given object.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @return CompletableFuture to process ASync request
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> CompletableFuture<Void> deleteAsync(Class<E> c, E o) throws DriverException {
        return this.deleteAsync(c,o,defaultQueryOptions);
    }

    /**
     * Delete asynchronously an object by passing an instance of the given object.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param queryOptions Query Options to include
     * @return CompletableFuture to process ASync request
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> CompletableFuture<Void> deleteAsync(Class<E> c, E o,QueryOptions queryOptions) throws DriverException {
        return this.executeASync(this.getStatementFactory(c).delete(o,queryOptions.withPrimaryKeysOnly())).thenApply(rs -> null);
    }

    /**
     * Check if an object exists. Non key columns are ignored.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @return boolean indicating existence
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> Boolean existsById(Class<E> c, E o) throws DriverException {
        return this.existsById(c,o,defaultQueryOptions);
    }

    /**
     * Check if an object exists. Non key columns are ignored.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param queryOptions Query Options to include
     * @return boolean indicating existence
     * @throws DriverException - Driver exception mapped to error code
     */
    @Rest("/exists")
    public <E extends AbstractCasquatchEntity> Boolean existsById(Class<E> c, E o, QueryOptions queryOptions) throws DriverException {
        ResultSet resultSet = this.execute(this.getStatementFactory(c).get(o,queryOptions.withPrimaryKeysOnly()));
        return resultSet.one() != null;
    }

    /**
     * Gets a database cache for the given entity class
     * @param c Entity class name
     * @param expirationTime Time in milliseconds to expire cache
     * @param <E> Generic entity class
     * @return database cache object
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> DatabaseCache<E> getCache(Class<E> c, Long expirationTime) throws DriverException {
        return new DatabaseCache<>(c,this,expirationTime);
    }

    /**
     * Gets a database cache for the given entity class
     * @param c Entity class name
     * @param <E> Generic entity class
     * @return database cache object
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> DatabaseCache<E> getCache(Class<E> c) throws DriverException {
        return new DatabaseCache<>(c,this);
    }

    /**
     * Get an object by passing an instance of the given object with all keys populated. Non-Key columns are ignored.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @return populated object
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> E getById(Class<E> c, E o) throws DriverException {
        return this.getById(c,o,defaultQueryOptions);
    }

    /**
     * Get an object by passing an instance of the given object with all keys populated. Non-Key columns are ignored.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param queryOptions Query Options to include
     * @return populated object
     * @throws DriverException - Driver exception mapped to error code
     */
    @Rest("/get")
    public <E extends AbstractCasquatchEntity> E getById(Class<E> c, E o, QueryOptions queryOptions) throws DriverException {
        return this.execute(this.getStatementFactory(c).get(o,queryOptions.withPrimaryKeysOnly().withLimit(1))).map(this.getStatementFactory(c)::map).one();
    }

    /**
     * Get all object by passing a partially populated instance of the given object. Non-Key columns are ignored.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> List<E> getAllById(Class<E> c, E o) throws DriverException {
        return this.getAllById(c,o,defaultQueryOptions);
    }

    /**
     * Get all object by passing a partially populated instance of the given object. Non-Key columns are ignored.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param limit limit number of returned objects
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> List<E> getAllById(Class<E> c, E o, Integer limit) throws DriverException {
        return this.getAllById(c,o,defaultQueryOptions.withLimit(limit));
    }

    /**
     * Get all object by passing a partially populated instance of the given object. Non-Key columns are ignored.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param queryOptions Query Options to include
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    @Rest("/get/all")
    public <E extends AbstractCasquatchEntity> List<E> getAllById(Class<E> c, E o, QueryOptions queryOptions) throws DriverException {
        return this.execute(this.getStatementFactory(c).get(o,queryOptions.withPrimaryKeysOnly())).map(this.getStatementFactory(c)::map).all();
    }

    /**
     * Get all objects by passing a partially populated object. Non-Key columns are allowed.
     *
     * Note: Defaults to 10 rows
     *
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> List<E> getAllBySolr(Class<E> c, E o) throws DriverException {
        return this.getAllBySolr(c,o,defaultSolrQueryOptions);
    }

    /**
     * Get all objects by passing a partially populated object. Non-Key columns are allowed.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param limit limit number of returned objects
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> List<E> getAllBySolr(Class<E> c, E o, int limit) throws DriverException {
        return this.getAllBySolr(c,o,defaultSolrQueryOptions.withLimit(limit));
    }

    /**
     * Get all objects by passing a partially populated object. Non-Key columns are allowed.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param queryOptions Query Options to include
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    @Rest("/solr/object/get")
    public <E extends AbstractCasquatchEntity> List<E> getAllBySolr(Class<E> c, E o, QueryOptions queryOptions) throws DriverException {
        return this.execute(this.getStatementFactory(c).get(o,queryOptions.withAllColumns())).map(this.getStatementFactory(c)::map).all();
    }

    /**
     * Get all objects by passing a solr_query.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param solrQueryString string representing the solr query (See https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/siQuerySyntax.html#siQuerySyntax)
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> List<E> getAllBySolr(Class<E> c, String solrQueryString) throws DriverException {
        return getAllBySolr(c,solrQueryString,defaultSolrQueryOptions);
    }

    /**
     * Get all objects by passing a solr_query.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param solrQueryString string representing the solr query (See https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/siQuerySyntax.html#siQuerySyntax)
     * @param limit limit number of returned objects
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> List<E> getAllBySolr(Class<E> c, String solrQueryString, int limit) throws DriverException {
        return this.getAllBySolr(c,solrQueryString,defaultSolrQueryOptions.withLimit(limit));
    }

    /**
     * Get all objects by passing a solr_query.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param solrQueryString string representing the solr query (See https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/siQuerySyntax.html#siQuerySyntax)
     * @param queryOptions Query Options to include
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    @Rest("/solr/query/get")
    public <E extends AbstractCasquatchEntity> List<E> getAllBySolr(Class<E> c, String solrQueryString, QueryOptions queryOptions) throws DriverException {
        return this.execute(this.getStatementFactory(c).getSolr(solrQueryString,queryOptions.withAllColumns())).map(this.getStatementFactory(c)::map).all();
    }

    /**
     * Get a count of objects by passing a solr_query.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> Long getCountBySolr(Class<E> c, E o) throws DriverException {
        return this.getCountBySolr(c,o,defaultSolrQueryOptions);
    }

    /**
     * Get a count of objects by passing a solr_query.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param queryOptions Query Options to include
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    @Rest("/solr/object/count")
    public <E extends AbstractCasquatchEntity> Long getCountBySolr(Class<E> c, E o, QueryOptions queryOptions) throws DriverException {
        Row row = this.execute(this.getStatementFactory(c).count(o,queryOptions.withAllColumns())).one();
        return Objects.requireNonNull(row).getLong("count");
    }

    /**
     * Get a count of objects by passing a solr_query.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param solrQueryString string representing the solr query (See https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/siQuerySyntax.html#siQuerySyntax)
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> Long getCountBySolr(Class<E> c, String solrQueryString) throws DriverException {
        return this.getCountBySolr(c,solrQueryString,defaultSolrQueryOptions);
    }

    /**
     * Get a count of objects by passing a solr_query.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param solrQueryString string representing the solr query (See https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/siQuerySyntax.html#siQuerySyntax)
     * @param queryOptions Query Options to include
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    @Rest("/solr/query/count")
    public <E extends AbstractCasquatchEntity> Long getCountBySolr(Class<E> c, String solrQueryString, QueryOptions queryOptions) throws DriverException {
        Row row = this.execute(this.getStatementFactory(c).countSolr(solrQueryString,queryOptions.withAllColumns())).one();
        return Objects.requireNonNull(row).getLong("count");
    }

    /**
     * Provides a raw session with no additional wrapping. {@link CasquatchDao#execute(Statement)} is preferred whenever possible.
     * @return raw CqlSession
     */
    public CqlSession getSession() {
        log.warn("CasquatchDao.getSession() is a raw session without any additional wrapping. CasquatchDao.execute() is preferred whenever possible.");
        return this.session;
    }

    /**
     * Save an object by passing a populated instance of the given object.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> Void save(Class<E> c, E o) throws DriverException{
        return this.save(c,o,defaultQueryOptions);
    }

    /**
     * Save an object by passing a populated instance of the given object.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param queryOptions Query Options to include
     * @throws DriverException - Driver exception mapped to error code
     */
    @SuppressWarnings("SameReturnValue")
    @Rest("/save")
    public <E extends AbstractCasquatchEntity> Void save(Class<E> c, E o, QueryOptions queryOptions) throws DriverException{
        this.execute(this.getStatementFactory(c).save(o,queryOptions.withAllColumns()));
        return null;
    }

    /**
     * Save asynchronously an object by passing a populated instance of the given object.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @return CompletableFuture to process ASync request
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> CompletableFuture<Void> saveAsync(Class<E> c, E o) throws DriverException{
        return this.saveAsync(c,o,defaultQueryOptions);
    }

    /**
     * Save asynchronously an object by passing a populated instance of the given object.
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @param queryOptions Query Options to include
     * @return CompletableFuture to process ASync request
     * @throws DriverException - Driver exception mapped to error code
     */
    public <E extends AbstractCasquatchEntity> CompletableFuture<Void> saveAsync(Class<E> c, E o,QueryOptions queryOptions) throws DriverException{
        return this.executeASync(this.getStatementFactory(c).save(o,queryOptions.withPrimaryKeysOnly())).thenApply(rs -> null);
    }

    /**
     * Close cluster connections
     */
    @PreDestroy
    public void close() {
        this.session.close();
        log.info("Closed cluster connection");
    }

    /**
     * Execute cql statement
     *
     * Deprecated: Please use Object API or {@link CasquatchDao#execute(Statement)}
     *
     * @param cql to execute
     */
    @Deprecated
    public void execute(String cql) {
        try {
            this.execute(SimpleStatement.builder(cql).build());
        }
        catch (Exception e) {
            throw new DriverException(e);
        }
    }

    /**
     * Get all object by passing a cql query
     *
     * Deprecated: Please use Object API or {@link CasquatchDao#execute(Statement)}
     *
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param cql cql to run
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    @Deprecated
    public <E extends AbstractCasquatchEntity> List<E> executeAll(Class<E> c, String cql) throws DriverException {
        try {
            return this.execute(SimpleStatement.builder(cql).build()).map(this.getStatementFactory(c)::map).all();
        }
        catch (Exception e) {
            throw new DriverException(e);
        }
    }

    /**
     * Get all object by passing a cql query
     *
     * Deprecated: Please use Object API or {@link CasquatchDao#execute(Statement)}
     *
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param cql cql to run
     * @return list of populated objects
     * @throws DriverException - Driver exception mapped to error code
     */
    @Deprecated
    public <E extends AbstractCasquatchEntity> E executeOne(Class<E> c, String cql) throws DriverException {
        try {
            return this.execute(SimpleStatement.builder(cql).build()).map(this.getStatementFactory(c)::map).one();
        }
        catch (Exception e) {
            throw new DriverException(e);
        }
    }

    /**
     * Get an object by passing a partially populated instance of the given object and returning the first result. Non-Key columns are ignored.
     *
     * Deprecated: please use {@link com.tmobile.opensource.casquatch.CasquatchDao#getById(Class, AbstractCasquatchEntity)}
     *
     * @param <E> Entity Object for results
     * @param c Class of object
     * @param o partially populated object
     * @return populated object
     * @throws DriverException - Driver exception mapped to error code
     */
    @Deprecated
    public <E extends AbstractCasquatchEntity> E getOneById(Class<E> c, E o) throws DriverException {
        return this.getById(c,o);
    }

}