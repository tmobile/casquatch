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
package com.tmobile.opensource.casquatch.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.tmobile.opensource.casquatch.CassandraDriver;
import com.tmobile.opensource.casquatch.models.system_schema.ColumnsExtended;
import com.tmobile.opensource.casquatch.models.system_schema.TablesExtended;
import com.tmobile.opensource.casquatch.models.system_schema.TypesExtended;

@Controller
public class GeneratorController {

    private CassandraDriver db;    

    private final static Logger logger = LoggerFactory.getLogger(GeneratorController.class);

    @Autowired
    public GeneratorController(CassandraDriver db) {
        this.db = db;
    }


    @GetMapping(value = "/generator/template/udtmodels/{schema}/{type}/{file_name}")
    public String generateUDTModelDSE(Model model, @PathVariable(value="schema") String schema, @PathVariable(value="type") String type,@PathVariable(value="file_name") String file_name) {   	
        TypesExtended typeData = db.executeOne(TypesExtended.class, "select keyspace_name, type_name, field_names, field_types from system_schema.types where keyspace_name = '"+schema+"' and type_name='"+type+"'");

        List<String> imports = new ArrayList<String>();        
        for(ColumnsExtended col : typeData.getCols()) {
        	List<String> tmpImportList = col.convertCQLtoJavaImport(col.getType(),false);
        	for(String tmpImport : tmpImportList) {
        		if(!imports.contains(tmpImport)) {
        			imports.add(tmpImport);
        		}
        	}
        }        
        model.addAttribute("imports",imports);
        
        model.addAttribute("type",typeData);
        model.addAttribute("columns", typeData.getCols());
        model.addAttribute("className",ColumnsExtended.format(type,true));
        return "models/dse_type";
    }

    @GetMapping(value = "/generator/template/models/{schema}/{table}/{file_name}")
    public String generateModelDSE(Model model, @PathVariable(value="schema") String schema, @PathVariable(value="table") String table,@PathVariable(value="file_name") String file_name) {
        List<ColumnsExtended> columns = db.executeAll(ColumnsExtended.class, "select keyspace_name, table_name, column_name, clustering_order, kind, position, type from system_schema.columns where keyspace_name = '"+schema.toLowerCase()+"' and table_name = '"+table.toLowerCase()+"'");
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
        model.addAttribute("imports",imports);

        model.addAttribute("keyspace",schema);
        model.addAttribute("table",table);
        model.addAttribute("className",ColumnsExtended.format(table,true));
        model.addAttribute("partitionKeys", partitionKeys);
        model.addAttribute("clusteringKeys", clusteringKeys);
        model.addAttribute("columns", columns);
        return "models/dse_table";
    }

    @GetMapping(value = "/generator/template/models/{schema}/{table}/cachable/{file_name}")
    public String generateModelDSECachable(Model model, @PathVariable(value="schema") String schema, @PathVariable(value="table") String table,@PathVariable(value="file_name") String file_name) {
        List<ColumnsExtended> columns = db.executeAll(ColumnsExtended.class, "select keyspace_name, table_name, column_name, clustering_order, kind, position, type from system_schema.columns where keyspace_name = '"+schema.toLowerCase()+"' and table_name = '"+table.toLowerCase()+"'");
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
        model.addAttribute("imports",imports);

        model.addAttribute("keyspace",schema);
        model.addAttribute("table",table);
        model.addAttribute("className",ColumnsExtended.format(table,true));
        model.addAttribute("partitionKeys", partitionKeys);
        model.addAttribute("clusteringKeys", clusteringKeys);
        model.addAttribute("columns", columns);
        return "models/dse_table_cachable";
    }

    @GetMapping(value = "/generator/{schema}/download/powershell")
    public String downloadPackagePowerShell(Model model, @PathVariable(value="schema") String schema) {
        List<TablesExtended> tableList = db.executeAll(TablesExtended.class, "select keyspace_name, table_name from system_schema.tables where keyspace_name = '"+schema.toLowerCase()+"'");

        model.addAttribute("schema",schema);
        model.addAttribute("tableList",tableList);
        
        List<TypesExtended> typeList = db.executeAll(TypesExtended.class, "select keyspace_name, type_name, field_names, field_types from system_schema.types where keyspace_name = '"+schema.toLowerCase()+"'");
        model.addAttribute("typeList",typeList);
        
        return "download_powershell";
    }

    @GetMapping(value = "/generator/{schema}/download/bash")
    public String downloadPackageBash(Model model, @PathVariable(value="schema") String schema) {
        List<TablesExtended> tableList = db.executeAll(TablesExtended.class, "select keyspace_name, table_name from system_schema.tables where keyspace_name = '"+schema.toLowerCase()+"'");

        model.addAttribute("schema",schema);
        model.addAttribute("tableList",tableList);
        
        List<TypesExtended> typeList = db.executeAll(TypesExtended.class, "select keyspace_name, type_name, field_names, field_types from system_schema.types where keyspace_name = '"+schema.toLowerCase()+"'");
        model.addAttribute("typeList",typeList);
        
        return "download_bash";
    }

    @GetMapping(value = "/generator/template/{schema}/pom.xml")
    public String genetatePackagePom(Model model, @PathVariable(value="schema") String schema) {
        model.addAttribute("schema",schema);
        return "pom_models";
    }
    
}