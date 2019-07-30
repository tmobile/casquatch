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

import com.tmobile.opensource.casquatch.AbstractEntityRestDAOTests;
import com.tmobile.opensource.casquatch.CasquatchDao;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@Getter
public class ${naming.classToRestDaoTests(naming.classToSimpleClass(class))} extends AbstractEntityRestDAOTests<${naming.classToSimpleClass(class)},${naming.classToRestDao(naming.classToSimpleClass(class))}> {
	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	@Spy
	protected ${naming.classToRestDao(naming.classToSimpleClass(class))} service;

	@Autowired
	protected CasquatchDao dao;

	public ${naming.classToRestDaoTests(naming.classToSimpleClass(class))}() {
		super(${naming.classToSimpleClass(class)}.class, ${naming.classToRestDao(naming.classToSimpleClass(class))}.class);
	}
}