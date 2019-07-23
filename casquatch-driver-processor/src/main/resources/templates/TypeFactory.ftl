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

<#if package?has_content>
package ${package};
</#if>

import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.tmobile.opensource.casquatch.AbstractTypeFactory;

public class ${naming.classToTypeFactory(naming.classToSimpleClass(class))} extends AbstractTypeFactory<${naming.classToSimpleClass(class)}> {

    public ${naming.classToTypeFactory(naming.classToSimpleClass(class))}(UserDefinedType userDefinedType) {
        super(userDefinedType);
    }

    @Override
    protected ${naming.classToSimpleClass(class)} fromUdtValue(UdtValue udtValue) {
        return new ${naming.classToSimpleClass(class)}(<#list fields as field,type>udtValue.get(${field?index},${type}.class)<#sep>,</#sep></#list>);
    }

    @Override
    protected UdtValue toUdtValue( ${naming.classToSimpleClass(class)} ${naming.classToVar(naming.classToSimpleClass(class))}) {
        return this.userDefinedType.newValue(<#list fields as field,type>${naming.classToVar(naming.classToSimpleClass(class))}.${naming.cqlToJavaGet(field)}()<#sep>,</#sep></#list>);
    }
}
