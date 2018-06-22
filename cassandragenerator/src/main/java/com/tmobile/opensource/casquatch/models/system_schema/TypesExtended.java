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

import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

@Table(
        keyspace = "system_schema",
        name="types"
)

public class TypesExtended extends Types {

    
	@Transient
	public List<ColumnsExtended> getCols() {
		List<ColumnsExtended> cols = new ArrayList<ColumnsExtended>();		
		List<String> fields = this.getFieldNames();
		List<String> types = this.getFieldTypes();		
		
		for(int i=0;i<fields.size();i++) {
			ColumnsExtended newCol = new ColumnsExtended();
			newCol.setKeyspaceName(this.getKeyspaceName());
			newCol.setTableName(this.getTypeName());
			newCol.setColumnName(fields.get(i));
			newCol.setType(types.get(i));
			cols.add(newCol);
		}		
		return cols;
	}

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

    //Returns format for proc name
    @Transient
    public String getProcName() {
        return this.format(this.getTypeName(),true);
    }
}
