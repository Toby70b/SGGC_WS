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
&& aws configure set region "eu-west-2" --profile localuser

# Set the default profile to the one just created to prevent having to provide it for subsequent AWS commands
export AWS_PROFILE=localuser

echo "########## Starting dynamodb local... ##########"
java -Djava.library.path=./DynamoDBLocal_lib -jar ./DynamoDBLocal.jar -sharedDb &
DYNAMO_PID=$!

echo "########## Waiting for dynamodb local... ##########"
while ! timeout 1 bash -c "echo > /dev/tcp/localhost/$DYNAMO_PORT" 2> /dev/null; do
  sleep 1
done

echo "########## Initializing tables ##########"
for file in sggc-setup/tables/*.json; do
    # Assumes all JSON files are named after the tables they create. Currently, this is a very safe assumption.
    FILE_WITHOUT_PATH=${file##*/}
    TABLE_NAME=${FILE_WITHOUT_PATH%%.*}
    if aws dynamodb describe-table --table-name "$TABLE_NAME" --endpoint-url $DYNAMO_ENDPOINT_URL 2> /dev/null; then
        echo "Table [$TABLE_NAME] already exists."
    else
        echo "Table [$TABLE_NAME] does not already exist. Will be created now."
        echo "$0: Creating table [$TABLE_NAME] from [$file]";
        aws dynamodb create-table --cli-input-json file://"$file" --endpoint-url $DYNAMO_ENDPOINT_URL
    fi
done
echo "########## Tables initialized ##########"
echo "########## DB boostrap completed! ##########"
wait $DYNAMO_PID