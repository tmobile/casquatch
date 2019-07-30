<#if package?has_content>
package ${package};
</#if>

import com.tmobile.opensource.casquatch.AbstractCasquatchType;
import com.tmobile.opensource.casquatch.annotation.CasquatchType;
<#list imports as clazz>
import ${clazz};
</#list>

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CasquatchType
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ${naming.cqlToJavaClass(name)} extends AbstractCasquatchType {
<#list fields as col,type>
    private ${naming.cqlDataTypeToJavaType(type)} ${naming.cqlToJavaVariable(col)};
</#list>

    /**
    * Generated: Returns DDL
    * @return DDL for table
    */
    public static String getDDL() {
        return "${ddl}";
    }
}

