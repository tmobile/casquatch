Remove-Item src/main/java/com/tmobile/opensource/casquatch/models -Force -Recurse
new-item -type directory -path src\main\java\com\tmobile\opensource\casquatch\models\${schema} -Force
cd src\main\java\com\tmobile\opensource\casquatch\models\${schema}
<#list tableList as table>
Invoke-WebRequest -OutFile ${table.procName}.java http://localhost:8080/generator/template/models/${table.keyspaceName}/${table.tableName}/${table.procName}.java
</#list>
cd ../../../../../../../../../
Invoke-WebRequest -OutFile pom.xml http://localhost:8080/generator/template/${schema}/pom.xml
