#! /bin/bash
set -e
export AWS_PAGER=""
DYNAMO_PORT=8000

export DYNAMO_ENDPOINT_URL=http://localhost:$DYNAMO_PORT

aws configure \
set aws_access_key_id "DUMMY_ACCESS_KEY" --profile localuser \
&& aws configure set aws_secret_access_key "DUMMY_SECRET_ACCESS_KEY" --profile localuser \
&& aws configure set region "local" --profile localuser

export AWS_PROFILE=localuser

echo "Starting dynamodb local..."
java -Djava.library.path=./DynamoDBLocal_lib -jar ./DynamoDBLocal.jar -sharedDb &
DYNAMO_PID=$!

echo "Waiting for dynamodb local..."
while ! timeout 1 bash -c "echo > /dev/tcp/localhost/$DYNAMO_PORT" 2> /dev/null; do
  sleep 1
done


echo "Initializing tables"
for f in setup/tables/*.json; do
    echo "$0: Creating table from $f"; aws dynamodb create-table --cli-input-json file://$f --endpoint-url $DYNAMO_ENDPOINT_URL
done
echo "Tables initialized"
wait $DYNAMO_PID