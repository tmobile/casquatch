package com.tmobile.opensource.casquatch.junit.runner;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tmobile.opensource.casquatch.junit.CassandraDriverDockerFilterTests;

public class FilterTests {

	private static final Logger logger = LoggerFactory.getLogger(FilterTests.class);
	
	public static void main(String[] args) throws Exception {       
		JUnitCore junitCore = new JUnitCore();
		Result result = junitCore.run(CassandraDriverDockerFilterTests.class);
	        for (Failure failure : result.getFailures()) {
	            logger.error(failure.toString());	            
	        }
	        System.exit(0);	  
     
	}

}
