package com.tmobile.opensource.casquatch.models.junittest;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.tmobile.opensource.casquatch.models.AbstractCassandraTable;

import java.lang.String;
import java.lang.Integer;
import java.lang.Double;

/**
 * Generated: Generated class for junittest.table_name
 */ 
@Table(
    keyspace = "junittest",
    name="table_name"
)
public class TableName extends AbstractCassandraTable {

    @Column(name="col_one")
		//Cassandra Type: text
    private String colOne;
    @Column(name="col_two")
		//Cassandra Type: text
    private String colTwo;
    @PartitionKey(0)
		@Column(name="key_one")
		//Cassandra Type: int
    private Integer keyOne;
    @ClusteringColumn(0)
		@Column(name="key_two")
		//Cassandra Type: int
    private Integer keyTwo;

    /**
     * Generated: Empty Initializer
     */     
    public TableName() {}    

    /**
     * Generated: Initalize with Partition Keys
     * @param keyOne Partition Key Named keyOne
     */    
    public TableName(Integer keyOne) {
        this.keyOne = keyOne;
    } 
   
    /**
     * Generated: Initalize with Partition and Clustering Keys
     * @param keyOne Partition Key Named keyOne
     * @param keyTwo Clustering Key Named keyTwo
     */ 
    public TableName(Integer keyOne,Integer keyTwo) {
        this.keyOne = keyOne;
        this.keyTwo = keyTwo;
    } 

    /**
     * Generated: Implement getID function from AbstractCassandraTable
     * @return Array of keys
     */ 
    @Transient
    public Object[] getID() {
        return new Object[]{this.getKeyOne(),this.getKeyTwo()};
    }
    
    /**
     * Generated: Get procedure for ColOne
     * @return Value of ColOne
     */ 
    public String getColOne() {
        return this.colOne;
    }
    
    /**
     * Generated: Set procedure for ColOne
     * @param colOne value to set
     */ 
    public void setColOne(String colOne) {
        this.colOne = colOne;
    }
    /**
     * Generated: Get procedure for ColTwo
     * @return Value of ColTwo
     */ 
    public String getColTwo() {
        return this.colTwo;
    }
    
    /**
     * Generated: Set procedure for ColTwo
     * @param colTwo value to set
     */ 
    public void setColTwo(String colTwo) {
        this.colTwo = colTwo;
    }
    /**
     * Generated: Get procedure for KeyOne
     * @return Value of KeyOne
     */ 
    public Integer getKeyOne() {
        return this.keyOne;
    }
    
    /**
     * Generated: Set procedure for KeyOne
     * @param keyOne value to set
     */ 
    public void setKeyOne(Integer keyOne) {
        this.keyOne = keyOne;
    }
    /**
     * Generated: Get procedure for KeyTwo
     * @return Value of KeyTwo
     */ 
    public Integer getKeyTwo() {
        return this.keyTwo;
    }
    
    /**
     * Generated: Set procedure for KeyTwo
     * @param keyTwo value to set
     */ 
    public void setKeyTwo(Integer keyTwo) {
        this.keyTwo = keyTwo;
    }

}
