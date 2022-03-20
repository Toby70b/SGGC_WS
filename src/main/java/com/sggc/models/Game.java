package com.sggc.models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor
@DynamoDBTable(tableName = "Game")
public class Game {
    @Id
    @JsonIgnore
    private String id;
    @JsonProperty("appid")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "appid-index")
    private String appid;
    private String name;
    @JsonIgnore
    private Boolean multiplayer;

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    public String getId() {
        return id;
    }

    @DynamoDBAttribute
    public String getAppid() {
        return appid;
    }

    @DynamoDBAttribute
    public String getName() {
        return name;
    }

    @DynamoDBAttribute
    public Boolean getMultiplayer() {
        return multiplayer;
    }
}

