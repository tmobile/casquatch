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
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


/**
 * Builder for {@link CasquatchDao}
 */
@SuppressWarnings({"WeakerAccess", "SpellCheckingInspection"})
@Slf4j
public class CasquatchDaoBuilder {

    protected final Map<String,Object> configMap = new HashMap<>();
    protected String prefix=null;
    protected String path=null;
    protected Config config;

    /**
     * Clear cached configuration
     */
    private void clearConfigCache() {
        this.config=null;
    }

    /**
     * Create CasquatchDao from configuration
     * @return configured CasquatchDao
     */
    public CasquatchDao build() {
        return new CasquatchDao(this);
    }

    /**
     * End the current profile
     * @return builder profile ended
     */
    public CasquatchDaoBuilder endProfile() {
        this.path=null;
        return this;
    }

    /**
     * Generate configuration from files as well as runtime settings
     * @return typesafe config object
     */
    public Config getConfig() {
        if(this.config == null) {
            ConfigLoader.clear();
            if (this.prefix == null) {
                this.config = ConfigLoader.casquatch();
            } else {
                this.config = ConfigLoader.casquatch(this.prefix);
            }
            if (!this.configMap.isEmpty()) {
                for (Map.Entry<String, Object> entry : this.configMap.entrySet()) {
                    if (entry.getValue() != null && !(entry.getValue() instanceof String && ((String) entry.getValue()).isEmpty())) {
                        if (log.isTraceEnabled())
                            log.trace("Runtime Property: {} -> {}", entry.getKey(), entry.getValue());
                        this.config = this.config.withValue(entry.getKey(), ConfigValueFactory.fromAnyRef(entry.getValue()));
                    }
                }
            }
        }
        return this.config;
    }

    /**
     * Get prefix of properties
     * @return prefix
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Providers access to a raw session based on Casquatch config
     * @return CqlSession object
     */
    public CqlSession session() {
        return this.sessionBuilder().build();
    }

    /**
     * Provides access to a raw session based on Casquatch config
     * @param keyspace override the keyspace for this session
     * @return CqlSession object
     */
    public CqlSession session(String keyspace) {
        return this.sessionBuilder().withKeyspace(keyspace).build();
    }

    /**
     * Provides access to the underlying session builder based on Casquatch config
     * @return CqlSessionBuilder object
     */
    public CqlSessionBuilder sessionBuilder() {
        return CqlSession.builder().withConfigLoader(new DefaultDriverConfigLoader(this::getConfig));
    }

    /**
     * Set the prefix for properties
     * @param prefix property prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Start a profile
     * @param profile name of profile
     * @return builder with profile started
     */
    public CasquatchDaoBuilder startProfile(String profile) {
        this.path=String.format("profiles.%s.",profile);
        return this;
    }

    /**
     * Prints out the config in JSON format
     * #return config in json format
     */
    public String toString() {
        Map<String,String> configString = new HashMap<>();
        for (Map.Entry<String, ConfigValue> entry : this.getConfig().entrySet()) {
            configString.put(entry.getKey(),entry.getValue().render());
        }
        try {
            return new ObjectMapper().writeValueAsString(configString);
        } catch (JsonProcessingException e) {
            return "Unable to convert to JSON";
        }
    }

    /**
     * Add a single value to the config
     * @param key key for value
     * @param value value (object)
     * @return builder with value set
     */
    public CasquatchDaoBuilder with(String key, Object value) {
        this.clearConfigCache();
        if(this.path !=null) {
            key = this.path + key;
        }
        this.configMap.put(key,value);
        return this;
    }

    /**
     * Add a list to the config
     * @param key key for value
     * @param valueList list of values
     * @return builder with value set
     */
    public CasquatchDaoBuilder with(String key, List<String> valueList) {
        if(this.path !=null) {
            key = this.path + key;
        }

        List<String> list;
        if(this.configMap.containsKey(key)) {
            if(this.configMap.get(key) instanceof List) {
                //noinspection unchecked
                list = (List<String>) this.configMap.get(key);
                list.addAll(valueList);
            }
            else if(this.configMap.get(key) instanceof String) {
                list = new ArrayList<>();
                list.add((String) this.configMap.get(key));
            }
            else {
                throw new DriverException(DriverException.CATEGORIES.CASQUATCH_INVALID_CONFIGURATION, "Attempted to set %s to a list but it already contained another class");
            }
        }
        else {
            list = new ArrayList<>(valueList);
        }
        this.configMap.put(key,list);
        return this;
    }


    /**
     * Add value to property list mapped to advanced.ssl-engine-factory.truststore-path
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSslEngineFactoryTruststorePath(String value) {
        return this.with("advanced.ssl-engine-factory.truststore-path",value);
    }

    /**
     * Add value to property list mapped to basic.load-balancing-policy.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicLoadBalancingPolicyClass(String value) {
        return this.with("basic.load-balancing-policy.class",value);
    }

    /**
     * Add value to property list mapped to basic.load-balancing-policy.local-datacenter
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicLoadBalancingPolicyLocalDatacenter(String value) {
        return this.with("basic.load-balancing-policy.local-datacenter",value);
    }

    /**
     * Add value to property list mapped to advanced.ssl-engine-factory.keystore-path
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSslEngineFactoryKeystorePath(String value) {
        return this.with("advanced.ssl-engine-factory.keystore-path",value);
    }

    /**
     * Add value to property list mapped to advanced.connection.pool.remote.size
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedConnectionPoolRemoteSize(Integer value) {
        return this.with("advanced.connection.pool.remote.size",value);
    }

    /**
     * Add value to property list mapped to basic.request.serial-consistency
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicRequestSerialConsistency(String value) {
        return this.with("basic.request.serial-consistency",value);
    }

    /**
     * Add value to property list mapped to advanced.metadata.topology-event-debouncer.window
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetadataTopologyEventDebouncerWindow(String value) {
        return this.with("advanced.metadata.topology-event-debouncer.window",value);
    }

    /**
     * Add value to property list mapped to advanced.address-translator.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedAddressTranslatorClass(String value) {
        return this.with("advanced.address-translator.class",value);
    }

    /**
     * Add value to property list mapped to solr-query-options.null-saving-strategy
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withSolrQueryOptionsNullSavingStrategy(String value) {
        return this.with("solr-query-options.null-saving-strategy",value);
    }

    /**
     * Add value to property list mapped to advanced.control-connection.schema-agreement.timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedControlConnectionSchemaAgreementTimeout(String value) {
        return this.with("advanced.control-connection.schema-agreement.timeout",value);
    }

    /**
     * Add value to property list mapped to advanced.connection.warn-on-init-error
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedConnectionWarnOnInitError(Boolean value) {
        return this.with("advanced.connection.warn-on-init-error",value);
    }

    /**
     * Add value to property list mapped to basic.request.default-idempotence
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicRequestDefaultIdempotence(Boolean value) {
        return this.with("basic.request.default-idempotence",value);
    }

    /**
     * Add value to property list mapped to advanced.heartbeat.interval
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedHeartbeatInterval(String value) {
        return this.with("advanced.heartbeat.interval",value);
    }

    /**
     * Add value to property list mapped to advanced.metadata.topology-event-debouncer.max-events
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetadataTopologyEventDebouncerMaxEvents(Integer value) {
        return this.with("advanced.metadata.topology-event-debouncer.max-events",value);
    }

    /**
     * Add value to property list mapped to advanced.protocol.max-frame-length
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedProtocolMaxFrameLength(String value) {
        return this.with("advanced.protocol.max-frame-length",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.session.throttling.delay.highest-latency
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsSessionThrottlingDelayHighestLatency(String value) {
        return this.with("advanced.metrics.session.throttling.delay.highest-latency",value);
    }

    /**
     * Add value to property list mapped to advanced.metadata.schema.request-timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetadataSchemaRequestTimeout(String value) {
        return this.with("advanced.metadata.schema.request-timeout",value);
    }

    /**
     * Add value to property list mapped to advanced.resolve-contact-points
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedResolveContactPoints(Boolean value) {
        return this.with("advanced.resolve-contact-points",value);
    }

    /**
     * Add value to property list mapped to advanced.netty.io-group.shutdown.quiet-period
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNettyIoGroupShutdownQuietPeriod(Integer value) {
        return this.with("advanced.netty.io-group.shutdown.quiet-period",value);
    }

    /**
     * Add value to property list mapped to advanced.netty.timer.ticks-per-wheel
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNettyTimerTicksPerWheel(Integer value) {
        return this.with("advanced.netty.timer.ticks-per-wheel",value);
    }

    /**
     * Add value to property list mapped to advanced.request.log-warnings
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedRequestLogWarnings(Boolean value) {
        return this.with("advanced.request.log-warnings",value);
    }

    /**
     * Add value to property list mapped to advanced.auth-provider.password
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedAuthProviderPassword(String value) {
        return this.with("advanced.auth-provider.password",value);
    }

    /**
     * Add value to property list mapped to query-options.profile
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withQueryOptionsProfile(String value) {
        return this.with("query-options.profile",value);
    }

    /**
     * Add value to property list mapped to advanced.netty.admin-group.shutdown.timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNettyAdminGroupShutdownTimeout(Integer value) {
        return this.with("advanced.netty.admin-group.shutdown.timeout",value);
    }

    /**
     * Add value to property list mapped to advanced.netty.admin-group.shutdown.unit
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNettyAdminGroupShutdownUnit(String value) {
        return this.with("advanced.netty.admin-group.shutdown.unit",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.node.cql-messages.highest-latency
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsNodeCqlMessagesHighestLatency(String value) {
        return this.with("advanced.metrics.node.cql-messages.highest-latency",value);
    }

    /**
     * Add value to property list mapped to solr-query-options.limit
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withSolrQueryOptionsLimit(Integer value) {
        return this.with("solr-query-options.limit",value);
    }

    /**
     * Add value to property list mapped to advanced.netty.admin-group.size
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNettyAdminGroupSize(Integer value) {
        return this.with("advanced.netty.admin-group.size",value);
    }

    /**
     * Add value to property list mapped to advanced.retry-policy.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedRetryPolicyClass(String value) {
        return this.with("advanced.retry-policy.class",value);
    }

    /**
     * Add value to property list mapped to advanced.metadata.schema.enabled
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetadataSchemaEnabled(Boolean value) {
        return this.with("advanced.metadata.schema.enabled",value);
    }

    /**
     * Add value to property list mapped to advanced.prepared-statements.reprepare-on-up.check-system-table
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedPreparedStatementsReprepareOnUpCheckSystemTable(Boolean value) {
        return this.with("advanced.prepared-statements.reprepare-on-up.check-system-table",value);
    }

    /**
     * Add value to property list mapped to advanced.connection.max-requests-per-connection
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedConnectionMaxRequestsPerConnection(Integer value) {
        return this.with("advanced.connection.max-requests-per-connection",value);
    }

    /**
     * Add value to property list mapped to advanced.request.trace.interval
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedRequestTraceInterval(String value) {
        return this.with("advanced.request.trace.interval",value);
    }

    /**
     * Add value to property list mapped to advanced.control-connection.schema-agreement.interval
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedControlConnectionSchemaAgreementInterval(String value) {
        return this.with("advanced.control-connection.schema-agreement.interval",value);
    }

    /**
     * Add value to property list mapped to advanced.control-connection.schema-agreement.warn-on-failure
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedControlConnectionSchemaAgreementWarnOnFailure(Boolean value) {
        return this.with("advanced.control-connection.schema-agreement.warn-on-failure",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.node.enabled
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsNodeEnabled(String value) {
        if(value.contains(",")) {
            return this.with("advanced.metrics.node.enabled", Collections.singletonList(value.split(",")));
        }
        else {
            return this.with("advanced.metrics.node.enabled", Collections.singletonList(value));
        }
    }
    /**
     * Add value to property list mapped to advanced.metrics.node.enabled
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value list of values for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsNodeEnabled(List<String> value) {
        return this.with("advanced.metrics.node.enabled",value);
    }

    /**
     * Add value to property list mapped to advanced.timestamp-generator.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedTimestampGeneratorClass(String value) {
        return this.with("advanced.timestamp-generator.class",value);
    }

    /**
     * Add value to property list mapped to failover-policy.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withFailoverPolicyClass(String value) {
        return this.with("failover-policy.class",value);
    }

    /**
     * Add value to property list mapped to advanced.netty.admin-group.shutdown.quiet-period
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNettyAdminGroupShutdownQuietPeriod(Integer value) {
        return this.with("advanced.netty.admin-group.shutdown.quiet-period",value);
    }

    /**
     * Add value to property list mapped to failover-policy.profile
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withFailoverPolicyProfile(String value) {
        return this.with("failover-policy.profile",value);
    }

    /**
     * Add value to property list mapped to basic.load-balancing-policy.filter.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicLoadBalancingPolicyFilterClass(String value) {
        return this.with("basic.load-balancing-policy.filter.class",value);
    }

    /**
     * Add value to property list mapped to advanced.ssl-engine-factory.truststore-password
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSslEngineFactoryTruststorePassword(String value) {
        return this.with("advanced.ssl-engine-factory.truststore-password",value);
    }

    /**
     * Add value to property list mapped to query-options.ignore-non-primary-keys
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withQueryOptionsIgnoreNonPrimaryKeys(Boolean value) {
        return this.with("query-options.ignore-non-primary-keys",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.session.throttling.delay.significant-digits
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsSessionThrottlingDelaySignificantDigits(Integer value) {
        return this.with("advanced.metrics.session.throttling.delay.significant-digits",value);
    }

    /**
     * Add value to property list mapped to basic.request.consistency
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicRequestConsistency(String value) {
        return this.with("basic.request.consistency",value);
    }

    /**
     * Add value to property list mapped to query-options.persist-nulls
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withQueryOptionsPersistNulls(Boolean value) {
        return this.with("query-options.persist-nulls",value);
    }

    /**
     * Add value to property list mapped to advanced.coalescer.reschedule-interval
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedCoalescerRescheduleInterval(String value) {
        return this.with("advanced.coalescer.reschedule-interval",value);
    }

    /**
     * Add value to property list mapped to query-options.allow-non-primary-keys
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withQueryOptionsAllowNonPrimaryKeys(Boolean value) {
        return this.with("query-options.allow-non-primary-keys",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.session.throttling.delay.refresh-interval
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsSessionThrottlingDelayRefreshInterval(String value) {
        return this.with("advanced.metrics.session.throttling.delay.refresh-interval",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.session.enabled
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsSessionEnabled(String value) {
        if(value.contains(",")) {
            return this.with("advanced.metrics.session.enabled", Collections.singletonList(value.split(",")));
        }
        else {
            return this.with("advanced.metrics.session.enabled", Collections.singletonList(value));
        }
    }
    /**
     * Add value to property list mapped to advanced.metrics.session.enabled
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value list of values for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsSessionEnabled(List<String> value) {
        return this.with("advanced.metrics.session.enabled",value);
    }

    /**
     * Add value to property list mapped to basic.config-reload-interval
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicConfigReloadInterval(String value) {
        return this.with("basic.config-reload-interval",value);
    }

    /**
     * Add value to property list mapped to query-options.limit
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withQueryOptionsLimit(Integer value) {
        return this.with("query-options.limit",value);
    }

    /**
     * Add value to property list mapped to advanced.netty.io-group.shutdown.timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNettyIoGroupShutdownTimeout(Integer value) {
        return this.with("advanced.netty.io-group.shutdown.timeout",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.session.cql-requests.refresh-interval
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsSessionCqlRequestsRefreshInterval(String value) {
        return this.with("advanced.metrics.session.cql-requests.refresh-interval",value);
    }

    /**
     * Add value to property list mapped to solr-query-options.consistency
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withSolrQueryOptionsConsistency(String value) {
        return this.with("solr-query-options.consistency",value);
    }

    /**
     * Add value to property list mapped to advanced.request-tracker.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedRequestTrackerClass(String value) {
        return this.with("advanced.request-tracker.class",value);
    }

    /**
     * Add value to property list mapped to basic.request.timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicRequestTimeout(String value) {
        return this.with("basic.request.timeout",value);
    }

    /**
     * Add value to property list mapped to advanced.coalescer.max-runs-with-no-work
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedCoalescerMaxRunsWithNoWork(Integer value) {
        return this.with("advanced.coalescer.max-runs-with-no-work",value);
    }

    /**
     * Add value to property list mapped to advanced.auth-provider.username
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedAuthProviderUsername(String value) {
        return this.with("advanced.auth-provider.username",value);
    }

    /**
     * Add value to property list mapped to advanced.throttler.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedThrottlerClass(String value) {
        return this.with("advanced.throttler.class",value);
    }

    /**
     * Add value to property list mapped to advanced.reconnection-policy.max-delay
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedReconnectionPolicyMaxDelay(String value) {
        return this.with("advanced.reconnection-policy.max-delay",value);
    }

    /**
     * Add value to property list mapped to advanced.metadata.schema.request-page-size
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetadataSchemaRequestPageSize(Integer value) {
        return this.with("advanced.metadata.schema.request-page-size",value);
    }

    /**
     * Add value to property list mapped to advanced.reconnection-policy.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedReconnectionPolicyClass(String value) {
        return this.with("advanced.reconnection-policy.class",value);
    }

    /**
     * Add value to property list mapped to advanced.request.warn-if-set-keyspace
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedRequestWarnIfSetKeyspace(Boolean value) {
        return this.with("advanced.request.warn-if-set-keyspace",value);
    }

    /**
     * Add value to property list mapped to advanced.netty.io-group.shutdown.unit
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNettyIoGroupShutdownUnit(String value) {
        return this.with("advanced.netty.io-group.shutdown.unit",value);
    }

    /**
     * Add value to property list mapped to advanced.schema-change-listener.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSchemaChangeListenerClass(String value) {
        return this.with("advanced.schema-change-listener.class",value);
    }

    /**
     * Add value to property list mapped to basic.contact-points
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicContactPoints(String value) {
        if(value.contains(",")) {
            return this.with("basic.contact-points", Collections.singletonList(value.split(",")));
        }
        else {
            return this.with("basic.contact-points", Collections.singletonList(value));
        }
    }
    /**
     * Add value to property list mapped to basic.contact-points
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value list of values for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicContactPoints(List<String> value) {
        return this.with("basic.contact-points",value);
    }

    /**
     * Add value to property list mapped to advanced.speculative-execution-policy.delay
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSpeculativeExecutionPolicyDelay(String value) {
        return this.with("advanced.speculative-execution-policy.delay",value);
    }

    /**
     * Add value to property list mapped to advanced.heartbeat.timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedHeartbeatTimeout(String value) {
        return this.with("advanced.heartbeat.timeout",value);
    }

    /**
     * Add value to property list mapped to basic.request.page-size
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicRequestPageSize(Integer value) {
        return this.with("basic.request.page-size",value);
    }

    /**
     * Add value to property list mapped to advanced.speculative-execution-policy.max-executions
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSpeculativeExecutionPolicyMaxExecutions(Integer value) {
        return this.with("advanced.speculative-execution-policy.max-executions",value);
    }

    /**
     * Add value to property list mapped to solr-query-options.allow-non-primary-keys
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withSolrQueryOptionsAllowNonPrimaryKeys(Boolean value) {
        return this.with("solr-query-options.allow-non-primary-keys",value);
    }

    /**
     * Add value to property list mapped to advanced.timestamp-generator.drift-warning.interval
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedTimestampGeneratorDriftWarningInterval(String value) {
        return this.with("advanced.timestamp-generator.drift-warning.interval",value);
    }

    /**
     * Add value to property list mapped to advanced.ssl-engine-factory.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSslEngineFactoryClass(String value) {
        return this.with("advanced.ssl-engine-factory.class",value);
    }

    /**
     * Add value to property list mapped to advanced.connection.init-query-timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedConnectionInitQueryTimeout(String value) {
        return this.with("advanced.connection.init-query-timeout",value);
    }

    /**
     * Add value to property list mapped to advanced.request.trace.attempts
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedRequestTraceAttempts(Integer value) {
        return this.with("advanced.request.trace.attempts",value);
    }

    /**
     * Add value to property list mapped to advanced.ssl-engine-factory.keystore-password
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSslEngineFactoryKeystorePassword(String value) {
        return this.with("advanced.ssl-engine-factory.keystore-password",value);
    }

    /**
     * Add value to property list mapped to advanced.request.trace.consistency
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedRequestTraceConsistency(String value) {
        return this.with("advanced.request.trace.consistency",value);
    }

    /**
     * Add value to property list mapped to advanced.connection.pool.local.size
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedConnectionPoolLocalSize(Integer value) {
        return this.with("advanced.connection.pool.local.size",value);
    }

    /**
     * Add value to property list mapped to solr-query-options.ignore-non-primary-keys
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withSolrQueryOptionsIgnoreNonPrimaryKeys(Boolean value) {
        return this.with("solr-query-options.ignore-non-primary-keys",value);
    }

    /**
     * Add value to property list mapped to advanced.socket.tcp-no-delay
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSocketTcpNoDelay(Boolean value) {
        return this.with("advanced.socket.tcp-no-delay",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.node.cql-messages.significant-digits
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsNodeCqlMessagesSignificantDigits(Integer value) {
        return this.with("advanced.metrics.node.cql-messages.significant-digits",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.session.cql-requests.highest-latency
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsSessionCqlRequestsHighestLatency(String value) {
        return this.with("advanced.metrics.session.cql-requests.highest-latency",value);
    }

    /**
     * Add value to property list mapped to advanced.metadata.schema.debouncer.window
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetadataSchemaDebouncerWindow(String value) {
        return this.with("advanced.metadata.schema.debouncer.window",value);
    }

    /**
     * Add value to property list mapped to query-options.null-saving-strategy
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withQueryOptionsNullSavingStrategy(String value) {
        return this.with("query-options.null-saving-strategy",value);
    }

    /**
     * Add value to property list mapped to advanced.reconnection-policy.base-delay
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedReconnectionPolicyBaseDelay(String value) {
        return this.with("advanced.reconnection-policy.base-delay",value);
    }

    /**
     * Add value to property list mapped to advanced.reconnect-on-init
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedReconnectOnInit(Boolean value) {
        return this.with("advanced.reconnect-on-init",value);
    }

    /**
     * Add value to property list mapped to advanced.node-state-listener.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNodeStateListenerClass(String value) {
        return this.with("advanced.node-state-listener.class",value);
    }

    /**
     * Add value to property list mapped to advanced.prepared-statements.reprepare-on-up.enabled
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedPreparedStatementsReprepareOnUpEnabled(Boolean value) {
        return this.with("advanced.prepared-statements.reprepare-on-up.enabled",value);
    }

    /**
     * Add value to property list mapped to advanced.timestamp-generator.drift-warning.threshold
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedTimestampGeneratorDriftWarningThreshold(String value) {
        return this.with("advanced.timestamp-generator.drift-warning.threshold",value);
    }

    /**
     * Add value to property list mapped to advanced.auth-provider.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedAuthProviderClass(String value) {
        return this.with("advanced.auth-provider.class",value);
    }

    /**
     * Add value to property list mapped to basic.session-keyspace
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withBasicSessionKeyspace(String value) {
        return this.with("basic.session-keyspace",value);
    }

    /**
     * Add value to property list mapped to advanced.timestamp-generator.force-java-clock
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedTimestampGeneratorForceJavaClock(Boolean value) {
        return this.with("advanced.timestamp-generator.force-java-clock",value);
    }

    /**
     * Add value to property list mapped to advanced.prepared-statements.reprepare-on-up.timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedPreparedStatementsReprepareOnUpTimeout(String value) {
        return this.with("advanced.prepared-statements.reprepare-on-up.timeout",value);
    }

    /**
     * Add value to property list mapped to advanced.metadata.schema.debouncer.max-events
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetadataSchemaDebouncerMaxEvents(Integer value) {
        return this.with("advanced.metadata.schema.debouncer.max-events",value);
    }

    /**
     * Add value to property list mapped to advanced.prepared-statements.reprepare-on-up.max-parallelism
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedPreparedStatementsReprepareOnUpMaxParallelism(Integer value) {
        return this.with("advanced.prepared-statements.reprepare-on-up.max-parallelism",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.node.cql-messages.refresh-interval
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsNodeCqlMessagesRefreshInterval(String value) {
        return this.with("advanced.metrics.node.cql-messages.refresh-interval",value);
    }

    /**
     * Add value to property list mapped to advanced.netty.io-group.size
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNettyIoGroupSize(Integer value) {
        return this.with("advanced.netty.io-group.size",value);
    }

    /**
     * Add value to property list mapped to advanced.connection.set-keyspace-timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedConnectionSetKeyspaceTimeout(Integer value) {
        return this.with("advanced.connection.set-keyspace-timeout",value);
    }

    /**
     * Add value to property list mapped to advanced.speculative-execution-policy.class
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSpeculativeExecutionPolicyClass(String value) {
        return this.with("advanced.speculative-execution-policy.class",value);
    }

    /**
     * Add value to property list mapped to advanced.metadata.token-map.enabled
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetadataTokenMapEnabled(Boolean value) {
        return this.with("advanced.metadata.token-map.enabled",value);
    }

    /**
     * Add value to property list mapped to advanced.prepared-statements.prepare-on-all-nodes
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedPreparedStatementsPrepareOnAllNodes(Boolean value) {
        return this.with("advanced.prepared-statements.prepare-on-all-nodes",value);
    }

    /**
     * Add value to property list mapped to solr-query-options.profile
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withSolrQueryOptionsProfile(String value) {
        return this.with("solr-query-options.profile",value);
    }

    /**
     * Add value to property list mapped to advanced.ssl-engine-factory.hostname-validation
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSslEngineFactoryHostnameValidation(Boolean value) {
        return this.with("advanced.ssl-engine-factory.hostname-validation",value);
    }

    /**
     * Add value to property list mapped to advanced.control-connection.timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedControlConnectionTimeout(String value) {
        return this.with("advanced.control-connection.timeout",value);
    }

    /**
     * Add value to property list mapped to advanced.prepared-statements.reprepare-on-up.max-statements
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedPreparedStatementsReprepareOnUpMaxStatements(Integer value) {
        return this.with("advanced.prepared-statements.reprepare-on-up.max-statements",value);
    }

    /**
     * Add value to property list mapped to max-requests-per-connection
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withMaxRequestsPerConnection(Integer value) {
        return this.with("max-requests-per-connection",value);
    }

    /**
     * Add value to property list mapped to profiles.ddl.basic.request.timeout
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withProfilesDdlBasicRequestTimeout(String value) {
        return this.with("profiles.ddl.basic.request.timeout",value);
    }

    /**
     * Add value to property list mapped to advanced.ssl-engine-factory.cipher-suites
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSslEngineFactoryCipherSuites(String value) {
        if(value.contains(",")) {
            return this.with("advanced.ssl-engine-factory.cipher-suites", Collections.singletonList(value.split(",")));
        }
        else {
            return this.with("advanced.ssl-engine-factory.cipher-suites", Collections.singletonList(value));
        }
    }
    /**
     * Add value to property list mapped to advanced.ssl-engine-factory.cipher-suites
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value list of values for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedSslEngineFactoryCipherSuites(List<String> value) {
        return this.with("advanced.ssl-engine-factory.cipher-suites",value);
    }

    /**
     * Add value to property list mapped to advanced.metrics.session.cql-requests.significant-digits
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedMetricsSessionCqlRequestsSignificantDigits(Integer value) {
        return this.with("advanced.metrics.session.cql-requests.significant-digits",value);
    }

    /**
     * Add value to property list mapped to advanced.netty.timer.tick-duration
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedNettyTimerTickDuration(String value) {
        return this.with("advanced.netty.timer.tick-duration",value);
    }

    /**
     * Add value to property list mapped to advanced.connection.max-orphan-requests
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder withAdvancedConnectionMaxOrphanRequests(Integer value) {
        return this.with("advanced.connection.max-orphan-requests",value);
    }


}