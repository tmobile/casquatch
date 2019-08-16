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

import com.datastax.oss.driver.api.core.type.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for Casquatch Naming Conventions
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public class CasquatchNamingConvention {
    /**
     * Converts an api name to rest method
     * @param api name of api
     * @return name of rest method
     */
    public static String apiToRestMethod(String api) {
        return WordUtils.uncapitalize(WordUtils.capitalizeFully(api,'/','-').replaceAll("([^a-zA-Z0-9])",""));
    }

    /**
     * convert class to package name
     * @param className class name
     * @return package name
     */
    public static String classToPackageName(String className) { return className.substring(0,className.lastIndexOf("."));}

    /**
     * convert class to simple class name
     * @param className class name
     * @return simple class name
     */
    public static String classToSimpleClass(String className) { return className.substring(className.lastIndexOf(".")+1);}
    /**
     * convert class to DSE tests class
     * @param className class name
     * @return dse tests class name
     */
    public static String classToDSETests(String className) { return className+"_DSETests";}
    /**
     * convert class to embedded tests class
     * @param className class name
     * @return embedded tests class name
     */
    public static String classToEmbeddedTests(String className) { return className+"_EmbeddedTests";}
    /**
     * convert class to external tests class
     * @param className class name
     * @return external tests class name
     */
    public static String classToExternalTests(String className) { return className+"_ExternalTests";}
    /**
     * convert class to rest dao class
     * @param className class name
     * @return rest dao class name
     */
    public static String classToRestDao(String className) { return className+"_RestDao";}
    /**
     * convert class to rest dao test class
     * @param className class name
     * @return rest dao test class name
     */
    public static String classToRestDaoTests(String className) { return className+"_RestDaoTests";}

    /**
     * convert class to spring tests class
     * @param className class name
     * @return spring tests class name
     */
    public static String classToSpringTests(String className) { return className+"_SpringTests";}

    /**
     * convert class to statement factory class
     * @param className class name
     * @return statement factory class name
     */
    public static String classToStatementFactory(String className) { return className+"_StatementFactory";}

    /**
     * convert class to type factory class
     * @param className class name
     * @return type factory class name
     */
    public static String classToTypeFactory(String className) { return className+"_TypeFactory";}

    /**
     * Convert class name to var
     * @param className name of class
     * @return name of var for class
     */
    public static String classToVar(String className) { return WordUtils.uncapitalize(className);}


    /**
     * Converts a CQL data type to a list of Java classes
     * @param type CQL DataType
     * @return List of required classes for type
     */
    public static List<Class> cqlDataTypeToJavaClasses(DataType type) {
        List<Class> classList = new ArrayList<>();
        if(type.equals(DataTypes.ASCII)) {
            classList.add(java.lang.String.class);
        }
        else if(type.equals(DataTypes.BIGINT)) {
            classList.add(java.lang.Long.class);
        }
        else if(type.equals(DataTypes.BLOB)) {
            classList.add(java.nio.ByteBuffer.class);
        }
        else if(type.equals(DataTypes.BOOLEAN)) {
            classList.add(java.lang.Boolean.class);
        }
        else if(type.equals(DataTypes.COUNTER)) {
            classList.add(java.lang.Long.class);
        }
        else if(type.equals(DataTypes.DATE)) {
            classList.add(java.time.LocalDate.class);
        }
        else if(type.equals(DataTypes.DECIMAL)) {
            classList.add(java.math.BigDecimal.class);
        }
        else if(type.equals(DataTypes.DOUBLE)) {
            classList.add(java.lang.Double.class);
        }
        else if(type.equals(DataTypes.DURATION)) {
            classList.add(com.datastax.oss.driver.api.core.data.CqlDuration.class);
        }
        else if(type.equals(DataTypes.FLOAT)) {
            classList.add(java.lang.Float.class);
        }
        else if(type.equals(DataTypes.INET)) {
            classList.add(java.net.InetAddress.class);
        }
        else if(type.equals(DataTypes.INT)) {
            classList.add(java.lang.Integer.class);
        }
        else if(type.equals(DataTypes.SMALLINT)) {
            classList.add(java.lang.Short.class);
        }
        else if(type.equals(DataTypes.TEXT)) {
            classList.add(java.lang.String.class);
        }
        else if(type.equals(DataTypes.TIME)) {
            classList.add(java.time.LocalTime.class);
        }
        else if(type.equals(DataTypes.TIMESTAMP)) {
            classList.add(java.time.Instant.class);
        }
        else if(type.equals(DataTypes.TIMEUUID)) {
            classList.add(java.util.UUID.class);
        }
        else if(type.equals(DataTypes.TINYINT)) {
            classList.add(java.lang.Byte.class);
        }
        else if(type.equals(DataTypes.UUID)) {
            classList.add(java.util.UUID.class);
        }
        else if(type.equals(DataTypes.VARINT)) {
            classList.add(java.math.BigInteger.class);
        }
        else if(type instanceof ListType) {
            classList.add(java.util.List.class);
            classList.addAll(cqlDataTypeToJavaClasses(((ListType) type).getElementType()));
        }
        else if(type instanceof MapType) {
            classList.add(java.util.Map.class);
            classList.addAll(cqlDataTypeToJavaClasses(((MapType) type).getKeyType()));
            classList.addAll(cqlDataTypeToJavaClasses(((MapType) type).getValueType()));
        }
        else if(type instanceof SetType) {
            classList.add(java.util.Set.class);
            classList.addAll(cqlDataTypeToJavaClasses(((SetType) type).getElementType()));
        }
        else if(type instanceof TupleType) {
            classList.add(com.datastax.oss.driver.api.core.data.TupleValue.class);
        }

        if(classList.size()==0) {
            throw new DriverException(DriverException.CATEGORIES.UNHANDLED_CASQUATCH,String.format("Unknown Data Type: %s %s", type.asCql(false, false), type.getProtocolCode()));
        }
        return classList;
    }

    /**
     * Converts a CQL data type to Java data type
     * @param type CQL DataType
     * @return String representation in Java for type
     */
    public static String cqlDataTypeToJavaType(DataType type) {
        List<Class> classList = cqlDataTypeToJavaClasses(type);

        if(classList.isEmpty()) {
            return null;
        }
        else if(classList.size()==1) {
            return cqlDataTypeToJavaClasses(type).get(0).getSimpleName();
        }
        else {
            StringBuilder javaType = new StringBuilder();
            int counter = 0;
            for(Class clazz : classList) {
                if(
                        clazz.equals(java.util.List.class) ||
                                clazz.equals(java.util.Map.class) ||
                                clazz.equals(java.util.Set.class)
                ) {
                    counter++;
                    javaType.append(clazz.getSimpleName()).append("<");
                }
                else {
                    if((javaType.length() > 0) && !javaType.toString().endsWith("<")) {
                        javaType.append(",");
                    }
                    javaType.append(clazz.getSimpleName());
                }
            }
            for(int i = 0 ; i<counter;i++) {
                javaType.append(">");
            }
            return javaType.toString();
        }
    }

    /**
     * Converts a CQL column to Java Class name
     * @param cql cql column name
     * @return name of the Java Class
     */
    public static String cqlToJavaClass(String cql) {
        return WordUtils.capitalize(cqlToJavaVariable(cql));
    }

    /**
     * Converts a cql column to setter
     * @param cql name of cql column
     * @return name of the setter
     */
    public static String cqlToJavaSet(String cql) {
        return javaVariableToJavaSet(cqlToJavaVariable(cql));
    }

    /**
     * Converts a cql column to getter
     * @param cql name of cql column
     * @return name of the getter
     */
    public static String cqlToJavaGet(String cql) {
        return javaVariableToJavaGet(cqlToJavaVariable(cql));
    }

    /**
     * Converts a CQL column to Java Variable
     * @param cql name of cql column
     * @return java variable representation of cql
     */
    public static String cqlToJavaVariable(String cql) {
        return WordUtils.uncapitalize(WordUtils.capitalize(cql,'_').replace("_",""));
    }

    /**
     * Convert a config property name into a function name
     * @param configName name of config property
     * @return name of function
     */
    public static String configToFunction(String configName) {
        return "with"+WordUtils.capitalizeFully(configName,'.','-').replaceAll("([.\\-])","");
    }

    /**
     * Converts a Java Variable name to CQL
     * @param javaVariable name of java variable
     * @return cql representation of javaVariable
     */
    public static String javaVariableToCql(String javaVariable) {
        String[] words = javaVariable.split("(?=[A-Z])");
        return String.join("_",words).toLowerCase();
    }

    /**
     * Converts a Java Class name to CQL
     * @param className name of java class
     * @return cql representation of classname
     */
    public static String javaClassToCql(String className) {
        return javaVariableToCql(classToVar(className));
    }

    /**
     * Converts a java variable name to getter
     * @param javaVariable name of java variable
     * @return name of the getter
     */
    public static String javaVariableToJavaGet(String javaVariable) {
        return "get"+ WordUtils.capitalize(javaVariable);
    }

    /**
     * Converts a java variable name to setter
     * @param javaVariable name of java variable
     * @return name of the setter
     */
    public static String javaVariableToJavaSet(String javaVariable) {
        return "set"+WordUtils.capitalize(javaVariable);
    }

}
