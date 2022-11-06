#Start DynamoDB service
java -Djava.library.path=../DynamoDBLocal_lib -jar ../DynamoDBLocal.jar -sharedDb &

#Create default profile
aws configure \
set aws_access_key_id "DUMMY_ACCESS_KEY" --profile localuser \
&& aws configure set aws_secret_access_key "DUMMY_SECRET_ACCESS_KEY" --profile localuser \
&& aws configure set region "local" --profile localuser

export AWS_PROFILE=localuser

#Create table
aws dynamodb create-table \
   --table-name UnifiedTable \
   --attribute-definitions AttributeName=pk,AttributeType=S AttributeName=sk,AttributeType=S \
   --key-schema AttributeName=pk,KeyType=HASH AttributeName=sk,KeyType=RANGE \
   --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
   --endpoint-url http://localhost:8000 || echo 'Table already exists'


