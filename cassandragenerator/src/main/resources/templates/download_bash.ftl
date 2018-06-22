rm -rf src/main/java/com/tmobile/opensource/casquatch/models
mkdir -p src/main/java/com/tmobile/opensource/casquatch/models/${schema}
cd src/main/java/com/tmobile/opensource/casquatch/models/${schema}
<#list tableList as table>
wget http://localhost:8080/generator/template/models/${table.keyspaceName}/${table.tableName}/${table.procName}.java
</#list>
<#list typeList as type>
wget http://localhost:8080/generator/template/udtmodels/${type.keyspaceName}/${type.typeName}/${type.procName}.java
</#list>
cd ../../../../../../../../../
wget http://localhost:8080/generator/template/${schema}/pom.xml