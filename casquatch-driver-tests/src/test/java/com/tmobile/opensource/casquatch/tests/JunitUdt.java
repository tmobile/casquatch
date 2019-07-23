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

package com.tmobile.opensource.casquatch.tests;

import com.tmobile.opensource.casquatch.AbstractCasquatchType;
import com.tmobile.opensource.casquatch.annotation.CasquatchType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CasquatchType
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class JunitUdt extends AbstractCasquatchType {
    private String val2;
    private String val1;

    /**
    * Generated: Returns DDL
    * @return DDL for table
    */
    public static String getDDL() {
        return "CREATE TYPE \"junittest\".\"junit_udt\" ( \"val1\" text, \"val2\" text );";
    }
}

