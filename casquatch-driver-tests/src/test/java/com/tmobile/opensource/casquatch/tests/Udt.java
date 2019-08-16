package com.tmobile.opensource.casquatch.tests;

import com.tmobile.opensource.casquatch.AbstractCasquatchType;
import com.tmobile.opensource.casquatch.annotation.CasquatchType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CasquatchType
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Udt extends AbstractCasquatchType {
    private String val2;
    private String val1;

    /**
    * Generated: Returns DDL
    * @return DDL for table
    */
    public static String getDDL() {
        return "CREATE TYPE \"udt\" ( \"val1\" text, \"val2\" text );";
    }
}

