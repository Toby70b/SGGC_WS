package com.sggc.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.*;

import java.util.Set;
import org.springframework.data.annotation.Id;
@Data
@DynamoDBTable(tableName = "User")
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    private Set<String> ownedGameIds;

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    public String getId() {
        return id;
    }

    @DynamoDBAttribute
    public Set<String> getOwnedGameIds() {
        return ownedGameIds;
    }

    //TTL (time-to-live) field for DynamoDB
    @NonNull
    private double removalDate;
}
