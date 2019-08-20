package com.tmobile.opensource.casquatch.examples.springrest;


import com.tmobile.opensource.casquatch.AbstractCasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.CasquatchEntity;
import com.tmobile.opensource.casquatch.annotation.ClusteringColumn;
import com.tmobile.opensource.casquatch.annotation.PartitionKey;
import java.lang.Integer;
import java.lang.String;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CasquatchEntity
@Getter @Setter @NoArgsConstructor
public class TableName extends AbstractCasquatchEntity {
    @PartitionKey
    private Integer keyOne;

    @ClusteringColumn(1)
    private Integer keyTwo;

    private String colOne;
    private String colTwo;
}

