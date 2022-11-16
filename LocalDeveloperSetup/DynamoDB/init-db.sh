#! /bin/bash
set -e

# The AWS pager will page the result of any command which can freeze the script as it awaits user input, this removes the default pager
# keeps the script running
export AWS_PAGER=""
DYNAMO_PORT=8000

export DYNAMO_ENDPOINT_URL=http://localhost:$DYNAMO_PORT

aws configure \
set aws_access_key_id "DUMMY_ACCESS_KEY" --profile localuser \
&& aws configure set aws_secret_access_key "DUMMY_SECRET_ACCESS_KEY" --profile localuser \
&& aws configure set region "local" --profile localuser

# Set the default profile to the one just created to prevent having to provide it for subsequent AWS commands
export AWS_PROFILE=localuser

echo "Starting dynamodb local..."
java -Djava.library.path=./DynamoDBLocal_lib -jar ./DynamoDBLocal.jar -sharedDb &
DYNAMO_PID=$!

echo "Waiting for dynamodb local..."
while ! timeout 1 bash -c "echo > /dev/tcp/localhost/$DYNAMO_PORT" 2> /dev/null; do
  sleep 1
done


echo "Initializing tables"
for f in sggc-setup/tables/*.json; do
    echo "$0: Creating table from $f"; aws dynamodb create-table --cli-input-json file://$f --endpoint-url $DYNAMO_ENDPOINT_URL
done
echo "Tables initialized"
echo "DB boostrap completed!"
wait $DYNAMO_PID