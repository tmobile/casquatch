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

package com.tmobile.opensource.casquatch.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.datastax.driver.core.TableMetadata;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.opensource.casquatch.CassandraAdminDriver;
import com.tmobile.opensource.casquatch.CassandraDriver;
import com.tmobile.opensource.casquatch.generator.models.system_schema.ColumnsExtended;
import com.tmobile.opensource.casquatch.generator.models.system_schema.TypesExtended;
import com.tmobile.opensource.casquatch.generator.models.system_schema.TablesExtended;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class CassandraGenerator {
	
	private final static Logger logger = LoggerFactory.getLogger(CassandraGenerator.class);
	
	public static int MODE_CONSOLE = 0;
	public static int MODE_FILE = 1;
	
	private CassandraDriver db;
	private CassandraAdminDriver adminDB;
	private Configuration fmConfig;
	private Builder.Configuration config;
	
	public static class Builder {
		private class Configuration {	
			
			class Database {
				String username;
				String password;
				String datacenter;
				String keyspace;
				int port;
				String contactPoints;
			}
			
			class Output {
				String folder;
				Boolean overwrite;
				int mode;
			}
			
			Database db = new Database();
			Output output = new Output();
			Boolean pkg;
			List<String> tables;
			List<String> cacheableTables;
			List<String> types;
			String packageName;
			
			
			public Configuration() {
				this.db.port=9042;
				this.db.username="cassandra";
				this.db.password="cassandra";
				this.db.contactPoints="localhost";		
				this.output.overwrite=false;
				this.output.mode = MODE_CONSOLE;
				this.output.folder="./";
				this.pkg=false;
				this.packageName="com.tmobile.opensource.casquatch.models.$KEYSPACE$";
			}
			
			
			public String toString() {
				try {
					ObjectMapper mapper = new ObjectMapper();
					mapper.setVisibility(PropertyAccessor.FIELD,Visibility.ANY);
					return mapper.writeValueAsString(this).replace(this.db.password, "MASKED");
				} catch (JsonProcessingException e) {
					logger.debug("Failed to serialize config",e);
					return "Failed to serialize";
				}
			}
			
			public boolean validate() throws Exception {
		    	logger.debug("Configuration Validation: "+this.toString());		    	
		    	
		    	if(this.output.mode == MODE_FILE && this.output.folder.isEmpty()) {
		    		logger.error("Output folder is required");
		    		return false;
		    	}
		    	
		    	if(this.db.keyspace.isEmpty()) {
		    		logger.error("Keyspace is required");
		    		return false;
		    	}
		    	return true;
				
			}
		}
		
		private Builder.Configuration config;
		
	    /**
	     * CassandraDriver Builder constructor. Configures default settings.
	     */
		public Builder() {
			config = new Builder.Configuration();		
		}
		
		/**
	     * Build with properties file
	     * @param propertiesFile path to file
	     * @return Reference to Builder object
	     * @throws Exception passed exception
	     */
		public Builder withProperties(String propertiesFile) throws Exception {
			logger.info("Loading properties from "+propertiesFile);
			Properties properties = new Properties();
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(propertiesFile);
			    properties.load(inputStream);
			    
				
			}
			catch(IOException e) {
				logger.error("Unable to load properties file:"+propertiesFile,e);
			}
			finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					}
					catch (IOException e) {
						logger.error("Unable to close properties file: "+propertiesFile,e);
					}
				}
			}
						
			if(properties.containsKey("cassandraDriver.username")) {
				this.withUsername(properties.getProperty("cassandraDriver.username"));
			}
			
			if(properties.containsKey("cassandraDriver.username")) {
				this.withPassword(properties.getProperty("cassandraDriver.password"));
			}
			if(properties.containsKey("cassandraDriver.contactPoints")) {
				this.withContactPoints(properties.getProperty("cassandraDriver.contactPoints"));
			}
			
			if(properties.containsKey("cassandraDriver.keyspace")) {
				this.withKeyspace(properties.getProperty("cassandraDriver.keyspace"));
			}
			
			if(properties.containsKey("cassandraDriver.localDC")) {
				this.withDatacenter(properties.getProperty("cassandraDriver.localDC"));
			}
			
			if(properties.containsKey("cassandraDriver.port")) {
				this.withPort(Integer.parseInt(properties.getProperty("cassandraDriver.port")));
			}			
			
			if(properties.containsKey("cassandraGenerator.db.username")) {
				this.withUsername(properties.getProperty("cassandraGenerator.db.username"));
			}
			
			if(properties.containsKey("cassandraGenerator.db.password")) {
				this.withPassword(properties.getProperty("cassandraGenerator.db.password"));
			}
			
			if(properties.containsKey("cassandraGenerator.db.contactPoints")) {
				this.withContactPoints(properties.getProperty("cassandraGenerator.db.contactPoints"));
			}
						
			if(properties.containsKey("cassandraGenerator.db.port")) {
				this.withPort(Integer.parseInt(properties.getProperty("cassandraGenerator.db.port")));
			}
			
			if(properties.containsKey("cassandraGenerator.db.datacenter")) {
				this.withDatacenter(properties.getProperty("cassandraGenerator.db.datacenter"));
			}
			
			if(properties.containsKey("cassandraGenerator.db.keyspace")) {
				this.withKeyspace(properties.getProperty("cassandraGenerator.db.keyspace"));
			}
			
			if(properties.containsKey("cassandraGenerator.output.folder")) {
				this.withOutputFolder(properties.getProperty("cassandraGenerator.output.folder"));
			}
			
			if(properties.containsKey("cassandraGenerator.output.overwrite")) {
				if(properties.getProperty("cassandraGenerator.output.overwrite").toLowerCase().equals("true")) {
					this.withOverwriteOutput();
				}
				else if(properties.getProperty("cassandraGenerator.output.overwrite").toLowerCase().equals("false")) {
					this.withoutOverwriteOutput();
				}
				else {
					throw new Exception ("invalid cassandraGenerator.output.overwrite");
				}
			}
			
			if(properties.containsKey("cassandraGenerator.output.mode")) {
				if(properties.getProperty("cassandraGenerator.output.mode").toLowerCase().equals("file")) {
					this.withFileOutput();
				}
				else if(properties.getProperty("cassandraGenerator.output.mode").toLowerCase().equals("console")) {
					this.withConsoleOutput();
				}
				else {
					throw new Exception ("invalid cassandraGenerator.output.mode");
				}
			}
			
			if(properties.containsKey("cassandraGenerator.package")) {
				if(properties.getProperty("cassandraGenerator.package").toLowerCase().equals("true")) {
					this.withPackage();
				}
				else if(properties.getProperty("cassandraGenerator.package").toLowerCase().equals("false")) {
					this.withoutPackage();
				}
				else {
					throw new Exception ("invalid cassandraGenerator.package");
				}
			}
			
			if(properties.containsKey("cassandraGenerator.tables")) {
				this.withTables(Arrays.asList(properties.getProperty("cassandraGenerator.tables").split(",")));
			}
			
			if(properties.containsKey("cassandraGenerator.cacheableTables")) {
				this.withCacheableTables(Arrays.asList(properties.getProperty("cassandraGenerator.cacheableTables").split(",")));
			}
			
			if(properties.containsKey("cassandraGenerator.types")) {
				this.withTypes(Arrays.asList(properties.getProperty("cassandraGenerator.types").split(",")));
			}
			
			if(properties.containsKey("cassandraGenerator.packageName")) {
				this.withPackageName(properties.getProperty("cassandraGenerator.packageName"));
			}
			
			return this;
		}
		
		/**
	     * Build from command line arguments
	     * @param args argument object
	     * @return Reference to Builder object
	     * @throws Exception passed exception
	     */
		public Builder withArgs(ApplicationArguments args) throws Exception {
			
			logger.info(args.getOptionNames().toString());
			
			if(args.containsOption("properties")) {
				this.withProperties(args.getOptionValues("properties").get(0));
			}
			
			if(args.containsOption("output")) {
				this.withOutputFolder(args.getOptionValues("output").get(0));
			}
			
			if(args.containsOption("overwrite")) {
				this.withOverwriteOutput();
			}
			
			if(args.containsOption("contactPoints")) {
				this.withContactPoints(args.getOptionValues("contactPoints").get(0));
			}
			
			if(args.containsOption("port")) {
				this.withPort(Integer.parseInt(args.getOptionValues("port").get(0)));
			}
			
			if(args.containsOption("keyspace")) {
				this.withKeyspace(args.getOptionValues("keyspace").get(0));
			}
			
			if(args.containsOption("datacenter")) {
				this.withDatacenter(args.getOptionValues("datacenter").get(0));
			}
			
			if(args.containsOption("user")) {
				this.withUsername(args.getOptionValues("user").get(0));
			}
			
			if(args.containsOption("password")) {
				this.withPassword(args.getOptionValues("password").get(0));
			}
			
			if(args.containsOption("package")) {
				this.withPackage();
			}
			
			if(args.containsOption("packageName")) {
				this.withPackageName(args.getOptionValues("packageName").get(0));
			}
			
			if(args.containsOption("table")) {
				this.withTables(args.getOptionValues("table"));
			}
			
			if(args.containsOption("cacheableTable")) {
				this.withCacheableTables(args.getOptionValues("cacheableTable"));
			}
			
			if(args.containsOption("type")) {
				this.withTypes(args.getOptionValues("type"));
			}
			
			if(args.containsOption("console")) {
				this.withConsoleOutput();
			}
			
			return this;
		}
		
	    /**
	     * Build with username
	     * @param username database username
	     * @return Reference to Builder object
	     */
		public Builder withUsername(String username) {
			config.db.username = username;
			return this;
		}
		
	    /**
	     * Build with password
	     * @param password database password
	     * @return Reference to Builder object
	     */
		public Builder withPassword(String password) {
			config.db.password = password;
			return this;
		}
		
	    /**
	     * Build with contactPoints
	     * @param contactPoints database contactPoints
	     * @return Reference to Builder object
	     */
		public Builder withContactPoints(String contactPoints) {
			config.db.contactPoints = contactPoints;
			return this;
		}
		
	    /**
	     * Build with port
	     * @param port database port
	     * @return Reference to Builder object
	     */
		public Builder withPort(int port) {
			config.db.port = port;
			return this;
		}
		
	    /**
	     * Build with datacenter
	     * @param datacenter datacenter name
	     * @return Reference to Builder object
	     */
		public Builder withDatacenter(String datacenter) {
			config.db.datacenter = datacenter;
			return this;
		}
		
	    /**
	     * Build with keyspace
	     * @param keyspace database keyspace
	     * @return Reference to Builder object
	     */
		public Builder withKeyspace(String keyspace) {
			config.db.keyspace = keyspace;
			return this;
		}
		
	    /**
	     * Build with defined output folder
	     * @param folder folder for output
	     * @return Reference to Builder object
	     */
		public Builder withOutputFolder(String folder) {
			this.withFileOutput();
			config.output.folder = folder;
			return this;
		}
		
	    /**
	     * Build with overwriting output
	     * @return Reference to Builder object
	     */
		public Builder withOverwriteOutput() {
			config.output.overwrite = true;
			return this;
		}
		
	    /**
	     * Build without overwriting output
	     * @return Reference to Builder object
	     */
		public Builder withoutOverwriteOutput() {
			config.output.overwrite = false;
			return this;
		}
		
	    /**
	     * Build with console output
	     * @return Reference to Builder object
	     */
		public Builder withConsoleOutput() {
			config.output.mode = MODE_CONSOLE;
			return this;
		}
		
	    /**
	     * Build with file output
	     * @return Reference to Builder object
	     */
		public Builder withFileOutput() {
			config.output.mode = MODE_FILE;
			return this;
		}
		
	    /**
	     * Build as package
	     * @return Reference to Builder object
	     */
		public Builder withPackage() {
			config.pkg=true;
			return this;
		}
		
	    /**
	     * Build not as package
	     * @return Reference to Builder object
	     */
		public Builder withoutPackage() {
			config.pkg=false;
			return this;
		}
		
	    /**
	     * Build with the defined list of tables
	     * @param tables list of tables
	     * @return Reference to Builder object
	     */
		public Builder withTables(List<String> tables) {
			config.tables=tables;
			return this;
		}
		
	    /**
	     * Build with the list of cacheable tables
	     * @param cacheableTables list of tables
	     * @return Reference to Builder object
	     */
		public Builder withCacheableTables(List<String> cacheableTables) {
			config.cacheableTables=cacheableTables;
			return this;
		}
		
	    /**
	     * Build with the defined list of types
	     * @param types list of types
	     * @return Reference to Builder object
	     */
		public Builder withTypes(List<String> types) {
			config.types=types;
			return this;
		}
		
	    /**
	     * Build with package name
	     * @param packageName name of generated package
	     * @return Reference to Builder object
	     */
		public Builder withPackageName(String packageName) {
			config.packageName=packageName;
			return this;
		}
		
		
	    /**
	     * Get configuration object
	     * @return config Configuration instance
	     */	
		private Builder.Configuration getConfiguration() {
			return config;
		}
		
	    /**
	     * Build the defined CassandraDriver
	     * @return CassandraDriver Configured driver object
	     * @throws Exception passed exception
	     */
		public CassandraGenerator build() throws Exception {
			return CassandraGenerator.buildFrom(getConfiguration());
		}
	}
	
    /**
     * Validates a Builder configuration and returns the configured generator. Tied to .build() procedure
     * @param config generator configuration
     */
    private static CassandraGenerator buildFrom(Builder.Configuration config) throws Exception {
    	if(config == null)
    		throw new Exception("Configuration is required");
    	
    	return new CassandraGenerator(config);    	
    }
    
    /**
     * Cassandra Generator Builder. Please refer to builder docs for details
     * @return builder Instance of CassandraGenerator.Builder
     */    
    public static CassandraGenerator.Builder builder() {
    	return new CassandraGenerator.Builder();
    }
    
    /**
     * Initializes the Generator with configuration object
     * @param config generator configuration
     * @throws Exception passed exception
     */
    protected CassandraGenerator(Builder.Configuration config) throws Exception {
    	logger.info("Building with "+config.toString());
    	config.validate();
    	
    	config.packageName = config.packageName.replace("$KEYSPACE$", config.db.keyspace);
    	
    	this.config = config;	
    	this.fmConfig = new Configuration(Configuration.VERSION_2_3_27);
		this.fmConfig.setClassForTemplateLoading(this.getClass(), "/templates/");
		this.fmConfig.setDefaultEncoding("UTF-8");
		this.fmConfig.setLocale(Locale.US);
		this.fmConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		this.fmConfig.setLogTemplateExceptions(false);
		this.fmConfig.setWrapUncheckedExceptions(true);
		
		if(config.output.mode==MODE_FILE) {
			File dir = new File(config.output.folder);
			if(dir.exists() && config.output.overwrite==false) {
				throw new Exception("Directory already exists and overwrite is false");
			}
			else {
				try {
					dir.mkdir();
				}
				catch(Exception e) {
					logger.error("Failed to create directory "+config.output.folder);
					throw new Exception("Unable to create directory");
				}
			}
		}
		
		this.db = new CassandraDriver.Builder()
				      .withContactPoints(config.db.contactPoints)
				      .withKeyspace(config.db.keyspace)
				      .withUsername(config.db.username)
				      .withPassword(config.db.password)
				      .withPort(config.db.port)
				      .withLocalDC(config.db.datacenter)
				      .withoutDriverConfig()
				      .build();
		this.adminDB = new CassandraAdminDriver(db);
    }
	
    /**
     * Default constructor
     * @throws Exception passed exception
     */   
	public CassandraGenerator() throws Exception {
		this(new Builder().getConfiguration());		
	}
	
	/**
     * Generates code based on the provided configuration
     * @throws Exception passed exception
     */   
	public void run() throws Exception {
		if(config.pkg) {
			generatePom();
		}
		generateTables();
		generatecacheableTables();
		generateTypes();
	}
	
   /**
    * Generates pom file
    * @throws Exception passed exception
    */  
	public void generatePom() throws Exception {
		generatePom(config.db.keyspace);
	}
	
   /**
    * Generates pom file
    * @param keyspace provided keyspace
    * @throws Exception passed exception
    */  
	public void generatePom(String keyspace) throws Exception {
		
		Map<String, Object> input = new HashMap<String, Object>();
		input.put("keyspace", keyspace);
        input.put("package", config.packageName);
		
		generate("pom.ftl",input,"pom.xml");
	
	}
	
   /**
    * Generates all tables
    * @throws Exception passed exception
    */ 
	public void generateTables() throws Exception {
		generateTables(config.db.keyspace, config.tables, false);
	}
	
   /**
    * Generates all defined cache tables
    * @throws Exception passed exception
    */ 
	public void generatecacheableTables() throws Exception {
		if(config.cacheableTables!=null && config.cacheableTables.size() > 0) {
			generateTables(config.db.keyspace, config.cacheableTables, false);
		}
	}
	
   /**
    * Generates all tables
    * @param keyspace provided keyspace
    * @param tables list of tables
    * @param cacheable defines if table is cacheable
    * @throws Exception passed exception
    */ 	
	public void generateTables(String keyspace, List<String> tables, Boolean cacheable) throws Exception {
		if(tables != null && tables.size() > 0) {
			for (int i=0;i < tables.size();i++) {
				this.table(keyspace, tables.get(i),cacheable);
			}
		}
		else {
			List<TablesExtended> tableList = db.executeAll(TablesExtended.class, "select keyspace_name, table_name from system_schema.tables where keyspace_name = '"+keyspace.toLowerCase()+"'");
			for (int i=0;i < tableList.size();i++) {
				this.table(keyspace, tableList.get(i).getTableName(),cacheable);
			}
		}
	}
	
   /**
    * Generates a single table
    * @param keyspace provided keyspace
    * @param table name of table
    * @param cacheable flag if table should be cacheable
    * @throws Exception passed exception
    */ 
	public void table(String keyspace, String table, Boolean cacheable) throws Exception {
		
		logger.info("Creating models for table "+keyspace+"."+table);
		
		String cql = adminDB.getDatastaxSession().getCluster().getMetadata().getKeyspace(keyspace).getTable(table).asCQLQuery();
		
		List<ColumnsExtended> columns = db.executeAll(ColumnsExtended.class, "select keyspace_name, table_name, column_name, clustering_order, kind, position, type from system_schema.columns where keyspace_name = '"+keyspace.toLowerCase()+"' and table_name = '"+table.toLowerCase()+"'");
       
		// Remove if column is solr_query
		columns.removeIf(col -> col.getColumnName().equals("solr_query"));
       
		Collections.sort(columns, Comparator.comparing(ColumnsExtended::getPosition));
        

        List<ColumnsExtended> partitionKeys = new ArrayList<>();
        columns.stream().filter((ColumnsExtended col) -> col.getKind().equals("partition_key")).forEach((ColumnsExtended col) -> partitionKeys.add(col));
        Collections.sort(partitionKeys, Comparator.comparing(ColumnsExtended::getPosition));

        List<ColumnsExtended> clusteringKeys = new ArrayList<>();
        columns.stream().filter((ColumnsExtended col) -> col.getKind().equals("clustering")).forEach((ColumnsExtended col) -> clusteringKeys.add(col));
        Collections.sort(clusteringKeys, Comparator.comparing(ColumnsExtended::getPosition));
        
        List<String> imports = new ArrayList<String>();        
        for(ColumnsExtended col : columns) {
        	List<String> tmpImportList = col.convertCQLtoJavaImport(col.getType(),false);
        	for(String tmpImport : tmpImportList) {
        		if(!imports.contains(tmpImport)) {
        			imports.add(tmpImport);
        		}
        	}
        }      
		
		Map<String, Object> input = new HashMap<String, Object>();

        
        input.put("imports",imports);

        input.put("keyspace",keyspace);
        input.put("package", config.packageName);
        input.put("table",table);
        input.put("className",ColumnsExtended.format(table,true));
        input.put("partitionKeys", partitionKeys);
        input.put("clusteringKeys", clusteringKeys);
        input.put("columns", columns);
        input.put("cql", cql);
        
        String fileName = TablesExtended.format(table,true)+".java"; 
        if(config.pkg) {
        	fileName = "src/main/java/"+(config.packageName.replace(".", "/"))+"/"+fileName;   	
        }
		
        if(cacheable) {
    		generate("models/dse_cacheable_table.ftl",input,fileName);	
        	
        } else {
    		generate("models/dse_table.ftl",input,fileName);		        	
        }	
	}
	
   /**
    * Generates all types
    * @throws Exception passed exception
    */ 	
	public void generateTypes() throws Exception {
		generateTypes(config.db.keyspace, config.types);
	}	

	
   /**
    * Generates all types
    * @param keyspace provided keyspace
    * @param types list of types
    * @throws Exception passed exception
    */ 
	public void generateTypes(String keyspace, List<String> types) throws Exception {
		if(types != null && types.size() > 0) {
			for (int i=0;i < types.size();i++) {
				this.type(keyspace, types.get(i));
			}
		}
		else {
			List<TypesExtended> typeList = db.executeAll(TypesExtended.class, "select keyspace_name, type_name, field_names, field_types from system_schema.types where keyspace_name = '"+keyspace.toLowerCase()+"'");
			for (int i=0;i < typeList.size();i++) {
				this.type(keyspace, typeList.get(i).getTypeName());
			}
		}
	}
	
   /**
    * Generates a single type
    * @param keyspace provided keyspace
    * @param type name of type
    * @throws Exception passed exception
    */		
	public void type(String keyspace, String type) throws Exception {
		 TypesExtended typeData = db.executeOne(TypesExtended.class, "select keyspace_name, type_name, field_names, field_types from system_schema.types where keyspace_name = '"+keyspace.toLowerCase()+"' and type_name='"+type.toLowerCase()+"'");

        List<String> imports = new ArrayList<String>();        
        for(ColumnsExtended col : typeData.getCols()) {
        	List<String> tmpImportList = col.convertCQLtoJavaImport(col.getType(),false);
        	for(String tmpImport : tmpImportList) {
        		if(!imports.contains(tmpImport)) {
        			imports.add(tmpImport);
        		}
        	}
        }
        
        Map<String, Object> input = new HashMap<String, Object>();
        
        input.put("imports",imports);
        input.put("package", config.packageName);        
        input.put("type",typeData);
        input.put("columns", typeData.getCols());
        input.put("className",ColumnsExtended.format(type,true));
        
        String fileName = TypesExtended.format(type,true)+".java";
        if(config.pkg) {
        	fileName = "src/main/java/"+(config.packageName.replace(".", "/"))+"/"+fileName;   	
        }
        
        generate("models/dse_type.ftl",input,fileName);	
	}
	
   /**
    * Wrapper for generate commands to generate based on mode
    * @param templateName freemarker template to use
    * @param input input parameters
    * @param output name of output file
    * @throws Exception passed exception
    */ 
	private void generate(String templateName, Map<String, Object> input, String output ) throws Exception {
		if(this.config.output.mode==MODE_FILE) {
			generateFile(templateName,input,output);	
		}
		else {
			generateConsole(templateName,input);	
			
		}
	}
	
   /**
    * Generates a template output and writes to  console
    * @param templateName freemarker template to use
    * @param input input parameters
    * @throws Exception passed exception
    */ 
	private void generateConsole(String templateName, Map<String, Object> input) throws Exception {
		Template template = fmConfig.getTemplate(templateName);
		Writer consoleWriter = new OutputStreamWriter(System.out);
        template.process(input, consoleWriter);		
	}

	
   /**
    * Generates a template output and writes to file
    * @param templateName freemarker template to use
    * @param input input parameters
    * @param output name of output file
    * @throws Exception passed exception
    */ 
	private void generateFile(String templateName, Map<String, Object> input, String output ) throws Exception {
		
		output = config.output.folder+"/"+output;
		
		logger.info("writing to file : "+output);
		
		if(output.contains("/")) {
		
	        String path = output.substring(0,output.lastIndexOf("/"));
			
			File dir = new File(path);
			if(!dir.exists()) {
				try {
					dir.mkdirs();
				}
				catch(Exception e) {
					logger.error("Failed to create directory "+path,e);
					throw new Exception("Unable to create directory:"+path);
				}
			}
		}
		
		Template template = fmConfig.getTemplate(templateName);
		
		File file = new File(output);
		
		if(file.exists() && !config.output.overwrite) {
			throw new Exception("File ("+output+") already exists and overwrite is not enabled");
		}
		
		Writer fileWriter = new FileWriter(file);
        try {
            template.process(input, fileWriter);
        } finally {
            fileWriter.close();
        }
	}
	
	public static void help() {
		System.out.print(
		  "Command Line Arguments \n" +
		  "  --properties=<filename>     Load from properties file (View README.md for details) \n" +
		  "  --output=<path>             Path for output. Defaults to current directory\n" +
		  "  --overwrite                 Allow overwriting existing files\n" +
		  "  --contactPoints             Cassandra contact contactPoints. Defaults to localhost\n" +
		  "  --port=<port>               Cassandra Port. Defaults to 9042\n" +
		  "  --keyspace=<keyspace>       Cassandra keyspace\n" +
		  "  --datacenter=<datacenter>   Cassandra data center\n" +
		  "  --user=<username>           Cassandra user. Defaults to cassandra\n" +
		  "  --password=<password>       Cassandra password. Defaults to cassandra\n" +
		  "  --package                   Specifies if the results should be formatted as a maven project to be packaged\n" +
		  "  --packageName=<name>        Java package name. Defaults to com.tmobile.opensource.casquatch.models.<keyspace>\n" +
		  "  --table=<table>             Repeating parameter for each table to include. Generates all tables if omitted.\n" +
		  "  --cachableTable=<table>     Repeating parameter for any cachable table\n" +
		  "  --type=<type>               Repeating parameter for each type to include. Generates all types if omitted.\n" +
		  "  --console                   Display results to console instead of file\n" +
		  "  \n" +
		  "Example\n" +
		  "  Generate code based on a properties file\n" +
		  "    java -jar CassandraGenerator.jar --properties=application.properties \n" +
		  "  \n" +
		  "  Generate all tables in a keyspace providing minimum information\n" +
		  "    java -jar CassandraGenerator.jar --output=tmp --keyspace=myKeyspace --datacenter=dkr\n" +
		  "  \n" +
		  "  Generate a package for all tables in a keyspace. Specifying full information\n" +
		  "    java -jar CassandraGenerator.jar --output=tmp --contactPoints=localhost --port=9042 --keyspace=myKeyspace --datacenter=dkr --user=cassandra --password=cassandra --package --packageName=com.test.myapp --overwrite);\n"
		  );					  
	}
}
