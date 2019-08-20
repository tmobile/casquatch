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

package com.tmobile.opensource.casquatch.generator;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.TupleType;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.tmobile.opensource.casquatch.CasquatchDao;
import com.tmobile.opensource.casquatch.CasquatchDaoBuilder;
import com.tmobile.opensource.casquatch.CasquatchNamingConvention;
import com.tmobile.opensource.casquatch.ConfigLoader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.TextStringBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 * Generates entity objects which extend {@link com.tmobile.opensource.casquatch.AbstractCasquatchEntity} for use in Casquatch
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public class CasquatchGenerator {

    private CqlSession session;
    private final Configuration fmConfig;
    private final CasquatchGeneratorConfiguration casquatchGeneratorConfiguration;
    private KeyspaceMetadata keyspaceMetadata = null;

    /**
     * Main method to trigger run
     * @param args main arguments
     */
    public static void main(String[] args) {
        try {
            if(args.length==1 && args[0].equals("-h")) {
                System.out.println(help());
            }
            else {
                new CasquatchGenerator().run();
            }
        }
        catch (Exception e) {
            log.error("Error",e);
            System.exit(1);
        }

        System.exit(0);
    }

    /**
     * Initialize CasquatchGenerator
     * @throws Exception relays any exception found
     */
    public CasquatchGenerator() throws Exception {
        this(ConfigBeanFactory.create(ConfigLoader.generator(),CasquatchGeneratorConfiguration.class));
    }

    /**
     * Initialize with custom configuration
     * @param configuration configuration to use
     * @throws Exception exception during processing
     */
    public CasquatchGenerator(CasquatchGeneratorConfiguration configuration) throws Exception{
        fmConfig = new Configuration(Configuration.VERSION_2_3_28);
        fmConfig.setClassForTemplateLoading(CasquatchGenerator.class, "/templates/");
        fmConfig.setDefaultEncoding("UTF-8");
        fmConfig.setLocale(Locale.US);
        fmConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        fmConfig.setLogTemplateExceptions(false);
        fmConfig.setWrapUncheckedExceptions(true);

        Config casquatchConfig = ConfigLoader.casquatch();
        if(configuration.getKeyspace()==null && casquatchConfig.hasPath("basic.session-keyspace")) {
            configuration.setKeyspace(casquatchConfig.getString("basic.session-keyspace"));
        }

        try {
            configuration.validate();
        }
        catch (Exception e) {
            log.error("Configuration is invalid. Please run with -h or read README.md for required parameters.",e);
            System.exit(1);
        }
        casquatchGeneratorConfiguration = configuration;

        CasquatchDaoBuilder casquatchDaoBuilder = CasquatchDao.builder();
        casquatchDaoBuilder = casquatchDaoBuilder.withAdvancedAuthProviderUsername(casquatchGeneratorConfiguration.getUsername());
        casquatchDaoBuilder = casquatchDaoBuilder.withAdvancedAuthProviderPassword(casquatchGeneratorConfiguration.getPassword());
        casquatchDaoBuilder = casquatchDaoBuilder.withBasicLoadBalancingPolicyLocalDatacenter(casquatchGeneratorConfiguration.getDatacenter());
        casquatchDaoBuilder = casquatchDaoBuilder.withBasicSessionKeyspace(casquatchGeneratorConfiguration.getKeyspace());
        if(casquatchGeneratorConfiguration.getContactPoints()!=null && casquatchGeneratorConfiguration.getContactPoints().size()>0) {
            casquatchDaoBuilder = casquatchDaoBuilder.withBasicContactPoints(casquatchGeneratorConfiguration.getContactPoints());
        }

        if(casquatchGeneratorConfiguration.getFile()) {
            File dir = new File(casquatchGeneratorConfiguration.getOutputFolder());
            if(dir.exists() && !casquatchGeneratorConfiguration.getOverwrite()) {
                throw new Exception("Directory already exists and overwrite is false");
            }
            else {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdir();
                }
                catch(Exception e) {
                    log.error("Failed to create directory {}",casquatchGeneratorConfiguration.getOutputFolder());
                    throw new Exception(String.format("Failed to create directory %s",casquatchGeneratorConfiguration.getOutputFolder()));
                }
            }

        }
        if(casquatchGeneratorConfiguration.getPackageName()==null) {
            casquatchGeneratorConfiguration.setPackageName("com.tmobile.opensource.casquatch.models."+casquatchGeneratorConfiguration.getKeyspace());
        }
        session=casquatchDaoBuilder.session();
    }

    /**
     * Syntax helper
     * @return string containing help message
     */
    public static String help() {
        TextStringBuilder output = new TextStringBuilder();
        output.appendln("Casquatch Code Generated");
        output.appendln("");
        output.appendln("Properties can be provided via the command line via -D parameters");
        output.appendln("Properties:");
        output.appendln("casquatch.generator.username=<string> -- Authentication Username");
        output.appendln("casquatch.generator.password=<string> -- Authentication Password");
        output.appendln("casquatch.generator.keyspace=<string> -- Keyspace to parse");
        output.appendln("casquatch.generator.datacenter=<string> -- LocalDC for connection");
        output.appendln("casquatch.generator.contactPoints.0=<ip0:port>...casquatch.generator.contactPoints.n=<ipn:port> -- List of contact points");
        output.appendln("casquatch.generator.tables.0=<table0>...casquatch.generator.tables.n=<tablen> -- Provide a list of tables. All tables are processed if not supplied");
        output.appendln("casquatch.generator.console=<boolean> -- Indicate if generated code should be printed to console");
        output.appendln("casquatch.generator.file=<boolean> -- Indicate if generated code should be printed to a file");
        output.appendln("casquatch.generator.outputFolder=<path> -- Required if file=true. Defines location to write generated files");
        output.appendln("casquatch.generator.overwrite=<boolean> -- Toggle overwriting of files in output folder");
        output.appendln("casquatch.generator.createPackage=<boolean> -- If createPackage=true then pom.xml and src folder structure will be added |");
        output.appendln("casquatch.generator.packageName=<string> -- Package name for source files");
        output.appendln("config.file=<path> -- Specify a path  to a config file to place parameters");
        output.appendln("");
        output.appendln("Examples: ");
        output.appendln("");
        output.appendln("Generate using properties file");
        output.appendln("java -Dconfig.file=/path/to/config.properties -jar CassandraGenerator.jar");
        output.appendln("");
        output.appendln("Generate all tables in a keyspace providing minimum information");
        output.appendln("java -Dcasquatch.generator.outputFolder=tmp -Dcasquatch.generator.keyspace=myKeyspace -Dcasquatch.generator.datacenter=datacenter1 -jar CassandraGenerator.jar");
        output.appendln("");
        output.appendln("Generate a package for all tables in a keyspace providing additional information");
        output.appendln("java -Dcasquatch.generator.outputFolder=tmp -Dcasquatch.generator.keyspace=myKeyspace -Dcasquatch.generator.datacenter=datacenter1 -Dcasquatch.generator.username=cassandra -Dcasquatch.generator.password=cassandra -Dcasquatch.generator.createPackage=true -Dcasquatch.generator.packageName=com.demo.mykeyspace -jar CassandraGenerator.jar");
        return output.toString();
    }

    /**
     * Runs generation
     * @throws Exception relays any found exception
     */
    public void run() throws Exception {
        if(casquatchGeneratorConfiguration.getCreatePackage()) {
            generatePom();
        }

        for (Map.Entry<CqlIdentifier, KeyspaceMetadata> entry : session.getMetadata().getKeyspaces().entrySet()) {
            if (entry.getKey().toString().equals(casquatchGeneratorConfiguration.getKeyspace())) {
                keyspaceMetadata = entry.getValue();
                break;
            }
        }
        if (keyspaceMetadata == null) {
            log.error("Cannot find keyspace: {}", casquatchGeneratorConfiguration.getKeyspace());
        }

        generateTypes();
        generateEntities();

    }

    /**
     * Generate source for all UDTs
     * @throws Exception relay any found exception
     */
    private void generateTypes() throws Exception {
        if (casquatchGeneratorConfiguration.getTypes() != null && casquatchGeneratorConfiguration.getTypes().size()>0) {
            for(String type : casquatchGeneratorConfiguration.getTypes()) {
                if(keyspaceMetadata.getUserDefinedType(type).isPresent()) {
                    generateType(keyspaceMetadata.getUserDefinedType(type).get());
                }
            }
        }
        else if(!keyspaceMetadata.getTables().isEmpty()) {
            for (Map.Entry<CqlIdentifier, UserDefinedType> type : keyspaceMetadata.getUserDefinedTypes().entrySet()) {
                generateType(type.getValue());
            }
        }
    }

    /**
     * Generate source for UDT
     * @param userDefinedType UDT metadata
     * @throws Exception relay any found exception
     */
    private void generateType(UserDefinedType userDefinedType) throws Exception{
        List<String> imports = new ArrayList<>();
        Map<CqlIdentifier,DataType> fields = new HashMap<>();

        List<CqlIdentifier> fieldNames = userDefinedType.getFieldNames();
        List<DataType> fieldTypes = userDefinedType.getFieldTypes();

        for(int i = 0;i<fieldNames.size();i++) {
            fields.put(fieldNames.get(i),fieldTypes.get(i));
            for(Class clazz : CasquatchNamingConvention.cqlDataTypeToJavaClasses(fieldTypes.get(i))) {
                if(!imports.contains(clazz.getName())) {
                    imports.add(clazz.getName());
                }
            }
        }
        Map<String, Object> input = inputStart(userDefinedType.getName().toString());
        input.put("ddl",userDefinedType.describe(false).replace("CREATE TYPE \""+casquatchGeneratorConfiguration.getKeyspace()+"\".","CREATE TYPE ").replace("\"","\\\""));
        input.put("fields",fields);
        input.put("imports",imports);
        String output;
        if(casquatchGeneratorConfiguration.getCreatePackage()) {
            output = "src/main/java/" + (casquatchGeneratorConfiguration.getPackageName().replace(".", "/")) + "/" + CasquatchNamingConvention.cqlToJavaClass(userDefinedType.getName().toString()) + ".java";
        }
        else {
            output = CasquatchNamingConvention.cqlToJavaClass(userDefinedType.getName().toString()) + ".java";
        }
        generate("Type.ftl", input, output);
    }

    /**
     * Generate all entities for configuration
     * @throws Exception relays any found exception
     */
    private void generateEntities() throws Exception {

        if (casquatchGeneratorConfiguration.getTables() != null && casquatchGeneratorConfiguration.getTables().size()>0) {
            for(String table : casquatchGeneratorConfiguration.getTables()) {
                if(keyspaceMetadata.getTable(table).isPresent()) {
                    generateEntity(keyspaceMetadata.getTable(table).get());
                }
            }
        }
        else if(!keyspaceMetadata.getTables().isEmpty()) {
            for (Map.Entry<CqlIdentifier, TableMetadata> table : keyspaceMetadata.getTables().entrySet()) {
                generateEntity(table.getValue());
            }
        }
    }

    /**
     * Generate a single entity
     * @param entity metadata of entity
     * @throws Exception relays any found exception
     */
    private void generateEntity(TableMetadata entity) throws Exception{
        List<String> imports = new ArrayList<>();
        Map<CqlIdentifier,ColumnMetadata> partitionKeys = new HashMap<>();
        Map<CqlIdentifier,ColumnMetadata> clusteringColumns = new HashMap<>();
        Map<CqlIdentifier,ColumnMetadata> nonKeyColumns = new HashMap<>();
        Map<String,String> udtColumns = new HashMap<>();

        for(ColumnMetadata partitionKey : entity.getPartitionKey()) {
            partitionKeys.put(partitionKey.getName(),partitionKey);
        }

        for(Map.Entry<ColumnMetadata, ClusteringOrder> clusteringKey : entity.getClusteringColumns().entrySet()) {
            clusteringColumns.put(clusteringKey.getKey().getName(),clusteringKey.getKey());
        }

        for(Map.Entry<CqlIdentifier, ColumnMetadata> column : entity.getColumns().entrySet()) {
            if(column.getKey().toString().equals("solr_query")) {
                continue;
            }
            else if(column.getValue().getType()== DataTypes.COUNTER) {
                log.error("Counters are not supported. {} will not be generated.",entity.getName());
                return;
            }
            else if(column.getValue().getType() instanceof TupleType) {
                log.error("Tuples are not supported. {} will not be generated.",entity.getName());
                return;
            }
            else if(column.getValue().getType().getProtocolCode()==48) {
                udtColumns.put(column.getKey().toString(),((UserDefinedType) column.getValue().getType()).getName().toString());
            }
            else {
                if(!partitionKeys.containsKey(column.getKey()) && !clusteringColumns.containsKey(column.getKey())) {
                    nonKeyColumns.put(column.getKey(),column.getValue());
                }
                for (Class clazz : CasquatchNamingConvention.cqlDataTypeToJavaClasses(column.getValue().getType())) {
                    if (!imports.contains(clazz.getName())) {
                        imports.add(clazz.getName());
                    }
                }
            }
        }

        Map<String, Object> input = inputStart(entity.getName().toString());
        input.put("ddl",entity.describe(false).replace("CREATE TABLE \""+casquatchGeneratorConfiguration.getKeyspace()+"\".","CREATE TABLE ").replace("\"","\\\""));
        input.put("partitionKeys",partitionKeys);
        input.put("clusteringColumns",clusteringColumns);
        input.put("nonKeyColumns",nonKeyColumns);
        input.put("udtColumns",udtColumns);
        input.put("imports",imports);
        input.put("naming",new CasquatchNamingConvention());

        String entityName;
        if(casquatchGeneratorConfiguration.getCreatePackage()) {
            entityName = "src/main/java/" + (casquatchGeneratorConfiguration.getPackageName().replace(".", "/")) + "/" + CasquatchNamingConvention.cqlToJavaClass(entity.getName().toString()) + ".java";
        }
        else {
            entityName = CasquatchNamingConvention.cqlToJavaClass(entity.getName().toString()) + ".java";
        }

        String entityTestName;
        if(casquatchGeneratorConfiguration.getCreatePackage()) {
            entityTestName = "src/test/java/" + (casquatchGeneratorConfiguration.getPackageName().replace(".", "/")) + "/" + CasquatchNamingConvention.classToEmbeddedTests(CasquatchNamingConvention.cqlToJavaClass(entity.getName().toString()))+".java";
        }
        else {
            entityTestName = CasquatchNamingConvention.classToEmbeddedTests(CasquatchNamingConvention.cqlToJavaClass(entity.getName().toString()))+".java";
        }

        generate("Entity.ftl", input, entityName);
        if(casquatchGeneratorConfiguration.getCreateTests()) {
            generate("EntityTest.ftl", input, entityTestName);
        }
    }

    /**
     * Generates the pom file for the package
     * @throws Exception relays any found exception
     */
    private void generatePom() throws Exception {

        Map<String, Object> input = new HashMap<>();
        input.put("keyspace", casquatchGeneratorConfiguration.getKeyspace());
        input.put("package", casquatchGeneratorConfiguration.getPackageName());

        generate("pom.ftl",input,"pom.xml");

    }

    /**
     * Wrapper for generate commands to generate based on mode
     * @param templateName freemarker template to use
     * @param input input parameters
     * @param output name of output file
     * @throws Exception passed exception
     */
    private void generate(String templateName, Map<String, Object> input, String output ) throws Exception {
        if(casquatchGeneratorConfiguration.getConsole()) {
            generateConsole(templateName,input);
        }
        if(casquatchGeneratorConfiguration.getFile()) {
            generateFile(templateName,input,output);
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

        output = casquatchGeneratorConfiguration.getOutputFolder()+"/"+output;

        log.info("writing to file : "+output);

        if(output.contains("/")) {

            String path = output.substring(0,output.lastIndexOf("/"));

            File dir = new File(path);
            if(!dir.exists()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdirs();
                }
                catch(Exception e) {
                    log.error("Failed to create directory "+path,e);
                    throw new Exception("Unable to create directory:"+path);
                }
            }
        }

        Template template = fmConfig.getTemplate(templateName);

        File file = new File(output);

        if(file.exists() && !casquatchGeneratorConfiguration.getOverwrite()) {
            throw new Exception("File ("+output+") already exists and overwrite is not enabled");
        }

        try (Writer fileWriter = new FileWriter(file)) {
            template.process(input, fileWriter);
        }
    }

    /**
     * Start the template input map for shared values
     * @param name name of entity
     * @return input map
     */
    protected Map<String,Object> inputStart(String name) {
        Map<String, Object> input = new HashMap<>();
        input.put("package", casquatchGeneratorConfiguration.getPackageName());
        input.put("class", casquatchGeneratorConfiguration.getPackageName()+"."+CasquatchNamingConvention.cqlToJavaClass(name));
        input.put("name", name);
        input.put("naming", new CasquatchNamingConvention());
        input.put("minify",casquatchGeneratorConfiguration.getMinify());
        input.put("createTests",casquatchGeneratorConfiguration.getCreateTests());
        return input;
    }
}
