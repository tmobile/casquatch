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
<#if package?has_content>
package ${package};
</#if>

import com.tmobile.opensource.casquatch.rest.Request;
import com.tmobile.opensource.casquatch.rest.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.tmobile.opensource.casquatch.CasquatchDao;

import java.util.List;

@RestController
@RequestMapping(value="${restApi}/${naming.classToSimpleClass(class)}", produces = MediaType.APPLICATION_JSON_VALUE)
public class ${naming.classToRestDao(naming.classToSimpleClass(class))} {
    @Autowired
    CasquatchDao casquatchDao;

<#list restMethods as api,method>
    @RequestMapping(value = "${api}", method= RequestMethod.POST)
    <#if method.returnType.simpleName=="AbstractCasquatchEntity">
    public Response<${naming.classToSimpleClass(class)}> ${naming.apiToRestMethod(api)}(@RequestBody Request<${naming.classToSimpleClass(class)}> request) {
        return new Response<${naming.classToSimpleClass(class)}>(casquatchDao.${method.name}(${naming.classToSimpleClass(class)}.class,request.getPayload(),request.getQueryOptions()));
    }
    <#elseif method.returnType.simpleName=="Void">
    public Response<Void> ${naming.apiToRestMethod(api)}(@RequestBody Request<${naming.classToSimpleClass(class)}> request) {
        return new Response<Void>(casquatchDao.${method.name}(${naming.classToSimpleClass(class)}.class,request.getPayload(),request.getQueryOptions()), Response.Status.SUCCESS);
    }
    <#else>
    public Response<${method.returnType.simpleName}> ${naming.apiToRestMethod(api)}(@RequestBody Request<${naming.classToSimpleClass(class)}> request) {
        return new Response<${method.returnType.simpleName}>(casquatchDao.${method.name}(${naming.classToSimpleClass(class)}.class,request.getPayload(),request.getQueryOptions()));
    }
    </#if>
</#list>
}
