package com.tmobile.opensource.casquatch.models.springconfig;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;

import java.lang.String;

/**
 * Generated: Generated class for springconfig.configuration
 */ 
@Table(
    keyspace = "springconfig",
    name="configuration"
)
public class Configuration extends AbstractCassandraTable {

    @Column(name="value")
		//Cassandra Type: text
    private String value;
    @PartitionKey(0)
		@Column(name="application")
		//Cassandra Type: text
    private String application;
    @ClusteringColumn(0)
		@Column(name="label")
		//Cassandra Type: text
    private String label;
    @ClusteringColumn(1)
		@Column(name="key")
		//Cassandra Type: text
    private String key;
    @PartitionKey(1)
		@Column(name="profile")
		//Cassandra Type: text
    private String profile;

    /**
     * Generated: Empty Initializer
     */     
    public Configuration() {}    

    /**
     * Generated: Initalize with Partition Keys
     * @param application Partition Key Named application
     * @param profile Partition Key Named profile
     */    
    public Configuration(String application,String profile) {
        this.application = application;
        this.profile = profile;
    } 
   
    /**
     * Generated: Initalize with Partition and Clustering Keys
     * @param application Partition Key Named application
     * @param profile Partition Key Named profile
     * @param label Clustering Key Named label
     * @param key Clustering Key Named key
     */ 
    public Configuration(String application,String profile,String label,String key) {
        this.application = application;
        this.profile = profile;
        this.label = label;
        this.key = key;
    } 

    /**
     * Generated: Implement getID function from AbstractCassandraTable
     * @return Array of keys
     */ 
    @Transient
    public Object[] getID() {
        return new Object[]{this.getApplication(),this.getProfile(),this.getLabel(),this.getKey()};
    }
    
    /**
     * Generated: Get procedure for Value
     * @return Value of Value
     */ 
    public String getValue() {
        return this.value;
    }
    
    /**
     * Generated: Set procedure for Value
     * @param value value to set
     */ 
    public void setValue(String value) {
        this.value = value;
    }
    /**
     * Generated: Get procedure for Application
     * @return Value of Application
     */ 
    public String getApplication() {
        return this.application;
    }
    
    /**
     * Generated: Set procedure for Application
     * @param application value to set
     */ 
    public void setApplication(String application) {
        this.application = application;
    }
    /**
     * Generated: Get procedure for Label
     * @return Value of Label
     */ 
    public String getLabel() {
        return this.label;
    }
    
    /**
     * Generated: Set procedure for Label
     * @param label value to set
     */ 
    public void setLabel(String label) {
        this.label = label;
    }
    /**
     * Generated: Get procedure for Key
     * @return Value of Key
     */ 
    public String getKey() {
        return this.key;
    }
    
    /**
     * Generated: Set procedure for Key
     * @param key value to set
     */ 
    public void setKey(String key) {
        this.key = key;
    }
    /**
     * Generated: Get procedure for Profile
     * @return Value of Profile
     */ 
    public String getProfile() {
        return this.profile;
    }
    
    /**
     * Generated: Set procedure for Profile
     * @param profile value to set
     */ 
    public void setProfile(String profile) {
        this.profile = profile;
    }

}
