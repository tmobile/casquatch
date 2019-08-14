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
import com.tmobile.opensource.casquatch.AbstractEntityTests;
import com.tmobile.opensource.casquatch.CasquatchDao;
import com.tmobile.opensource.casquatch.CasquatchSpringBeans;
import com.tmobile.opensource.casquatch.CasquatchTestDaoBuilder;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(CasquatchSpringBeans.class)
public class ${naming.classToSpringTests(naming.classToSimpleClass(class))} extends AbstractEntityTests<${naming.classToSimpleClass(class)}> {
    @Autowired
    CasquatchDao casquatchDao;

    public ${naming.classToSpringTests(naming.classToSimpleClass(class))}() {
        super(${naming.classToSimpleClass(class)}.class);
        new CasquatchTestDaoBuilder().withEmbedded().withTestKeyspace("junittest").withDDL(new ${naming.classToSimpleClass(class)}().getDDL()).buildSpring();
    }

    public CasquatchDao getCasquatchDao() {
        return this.casquatchDao;
    }
}