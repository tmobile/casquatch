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
package com.tmobile.opensource.casquatch.models.system_schema;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

@Table(
        keyspace = "system_schema",
        name="columns"
)

public class ColumnsExtended extends Columns {

    //Returns a formatted string such that underscores are removed and string becomes source_string to sourceString
    @Transient
    public static String format(String name, boolean capFirst) {
        String returnName = name;

        //Convert _ to space
        returnName = returnName.replaceAll("_"," ");

        //Cap first words
        char[] chars = returnName.toCharArray();
        for(int x=2; x<chars.length;x++){
            if(chars[x-1] == ' ') {
                chars[x] = Character.toUpperCase(chars[x]);
            }
        }
        returnName = new String(chars);

        //Remove whitespace
        returnName = returnName.replaceAll("\\s","");
        if(capFirst) {
            returnName = returnName.substring(0, 1).toUpperCase() + returnName.substring(1);
        }
        return returnName;
    }

    //Returns format for var name
    @Transient
    public String getVarName() {
        return this.format(this.getColumnName(),false);
    }

    //Returns format for proc name
    @Transient
    public String getProcName() {
        return this.format(this.getColumnName(),true);
    }

    //Returns CQL types converted to java
    @Transient
    public String getJavaType() {
        return this.convertCQLtoJava(this.getType(),false);
    }

    @Transient
    public String convertCQLtoJava(String type, boolean forcePrimitive) {
        String rtnType = "";
        
        Pattern ptnFrozen = Pattern.compile("^frozen<(.+)>$");
        Matcher frozen = ptnFrozen.matcher(type); 

        Pattern ptnMap = Pattern.compile("^map<(.+), (.+)>$");
        Matcher map = ptnMap.matcher(type);
        
        Pattern ptnSetList = Pattern.compile("^(set|list)<(.+)>$");
        Matcher setList = ptnSetList.matcher(type);
        
        if(frozen.matches()) {
        	rtnType = this.convertCQLtoJava(frozen.group(1),false);
        }
        else if (map.matches()) {
        	return "Map<"+this.convertCQLtoJava(map.group(1),false)+", "+this.convertCQLtoJava(map.group(2),false)+">";
        }
        else if (setList.matches()) {
        	return format(setList.group(1),true)+"<"+this.convertCQLtoJava(setList.group(2),false)+">";
        }
        else {
            switch (type) {
                case "ascii":
                case "text":
                case "varchar":
                    rtnType = "String";
                    break;
                case "bigint":
                case "counter":
                case "time":
                	if(!forcePrimitive) {
                		rtnType = "Long";
                	}
                	else {
                		rtnType = "long";
                	}
                    break;
                case "blob":
                    rtnType = "ByteBuffer";
                    break;
                case "boolean":
                    rtnType = "boolean";
                    break;
                case "date":
                	rtnType = "LocalDate";
                	break;
                case "decimal":
                    rtnType = "BigDecimal";
                    break;
                case "double":
                	if(!forcePrimitive) {
                		rtnType = "Double";
                	}
                	else {
                		rtnType = "double";
                	}
                    break;
                case "float":
                	if(!forcePrimitive) {
                		rtnType = "Float";
                	}
                	else {
                		rtnType = "float";
                	}
                    break;
                case "inet":
                    rtnType = "InetAddress";
                    break;
                case "int":
                	if(!forcePrimitive) {
                		rtnType = "Integer";
                	}
                	else {
                		rtnType = "int";
                	}
                    break;
                case "smallint":
                	if(!forcePrimitive) {
                		rtnType = "Short";
                	}
                	else {
                		rtnType = "short";
                	}
                	break;
                case "timestamp":
                    rtnType = "Date";
                    break;
                case "tinyint":
                	if(!forcePrimitive) {
                		rtnType = "Byte";
                	}
                	else {
                		rtnType = "byte";
                	}
                	break;
                case "tuple":
                    rtnType = "TupleValue";
                    break;
                case "timeuuid":
                case "uuid":
                    rtnType = "UUID";
                    break;
                case "varint":
                    rtnType = "BigInteger";
                    break;
                default:
                    rtnType = format(type,true);
            }
        }
        return rtnType;
    }
    
    @Transient
    public String getAnnotations() {
    	List<String> annotations = new ArrayList<>();
    	if (this.getKind().equals("partition_key")) {
    		annotations.add("@PartitionKey("+this.getPosition()+")");    		
    	}
    	else if (this.getKind().equals("clustering")) {
    		annotations.add("@ClusteringColumn("+this.getPosition()+")");    		
    	}
    	
    	if (this.hasFrozen(this.getType())) {
    		annotations.add("@Frozen");
    	}
    	if (this.hasFrozenKey(this.getType())) {
    		annotations.add("@FrozenKey");
    	}
    	if (this.hasFrozenValue(this.getType())) {
    		annotations.add("@FrozenValue");
    	}
        annotations.add("@Column(name=\""+this.getColumnName()+"\")");
        annotations.add("//Cassandra Type: "+this.getType());
    	return String.join("\n\t\t", annotations);
    }
    
    @Transient
    private boolean hasFrozen(String type) {
    	if (type.matches("^frozen.+")) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    @Transient
    private boolean hasFrozenKey(String type) {
    	if (type.matches(".+<frozen<.+")) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    @Transient
    private boolean hasFrozenValue(String type) {
    	if (type.matches(".+, frozen.+")) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    @Transient
    public List<String> convertCQLtoJavaImport(String type, boolean forcePrimitive) {
        List<String> importList = new ArrayList<String>();
         
        if (this.hasFrozen(this.getType())) {
    		importList.add("com.datastax.driver.mapping.annotations.Frozen");
    	}
    	if (this.hasFrozenKey(this.getType())) {
    		importList.add("com.datastax.driver.mapping.annotations.FrozenKey");
    	}
    	if (this.hasFrozenValue(this.getType())) {
    		importList.add("com.datastax.driver.mapping.annotations.FrozenValue");
    	}
    	        
        Pattern ptnFrozen = Pattern.compile("^frozen<(.+)>$");
        Matcher frozen = ptnFrozen.matcher(type); 

        Pattern ptnMap = Pattern.compile("^map<(.+), (.+)>$");
        Matcher map = ptnMap.matcher(type);
        
        Pattern ptnSetList = Pattern.compile("^(set|list)<(.+)>$");
        Matcher setList = ptnSetList.matcher(type);
        
        if(frozen.matches()) {
        	importList.addAll(this.convertCQLtoJavaImport(frozen.group(1),false));
        }
        else if (map.matches()) {
        	importList.add("java.util.Map");
        	importList.addAll(this.convertCQLtoJavaImport(map.group(1),false));
        	importList.addAll(this.convertCQLtoJavaImport(map.group(2),false));
        }
        else if (setList.matches()) {
        	importList.add("java.util."+format(setList.group(1),true));
        	importList.addAll(this.convertCQLtoJavaImport(setList.group(2),false));
        }
        else {
            switch (type) {
            	case "varchar":
                case "ascii":
                case "text":
                	importList.add("java.lang.String");
                    break;
                case "blob":
                	importList.add("java.nio.ByteBuffer");
                    break;
                case "date":
                	importList.add("com.datastax.driver.core.LocalDate");
                	break;
                case "decimal":
                	importList.add("java.math.BigDecimal");
                    break;
                case "inet":
                	importList.add("java.net.InetAddress");
                    break;
                case "timestamp":
                	importList.add("java.util.Date");
                    break;
                case "uuid":
                case "timeuuid":
                	importList.add("java.util.UUID");
                    break;
                case "tuple":
                	importList.add("com.datastax.driver.core.TupleType");
                    break;
                case "varint":
                	importList.add("java.math.BigInteger");
                    break;
                case "bigint":
                case "counter":
                case "time":
                	if(!forcePrimitive) {
                		importList.add("java.lang.Long");
                	}
                    break;
                case "tinyint":
                	if(!forcePrimitive) {
                		importList.add("java.lang.Byte");
                	}
                case "smallint":
                	if(!forcePrimitive) {
                		importList.add("java.lang.Short");
                	}
                case "int":
                	if(!forcePrimitive) {
                		importList.add("java.lang.Integer");
                	}
                case "double":
                	if(!forcePrimitive) {
                		importList.add("java.lang.Double");
                	}
                    break;
                case "float":
                	if(!forcePrimitive) {
                		importList.add("java.lang.Float");
                	}
                default:
                	importList.add("com.tmobile.opensource.casquatch.models."+this.getKeyspaceName()+"."+format(type,true));
            }
        }
        return importList;
    }
}
