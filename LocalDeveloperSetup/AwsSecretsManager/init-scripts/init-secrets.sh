#! /bin/bash
set -e

LOCALSTACK_PORT=4566

export LOCALSTACK_ENDPOINT_URL=http://localhost:$LOCALSTACK_PORT

#Local stack throws an error when using a unsupported region e.g. "local" so just use eu-west-2
aws configure \
set aws_access_key_id "DUMMY_ACCESS_KEY" --profile localuser \
&& aws configure set aws_secret_access_key "DUMMY_SECRET_ACCESS_KEY" --profile localuser \
&& aws configure set region "eu-west-2" --profile localuser

# Set the default profile to the one just created to prevent having to provide it for subsequent AWS commands
export AWS_PROFILE=localuser

echo "########## Initializing secrets ##########"

aws secretsmanager create-secret \
    --name SteamAPIKey \
    --secret-string "DUMMY_STEAM_API_KEY" \
	  --endpoint-url $LOCALSTACK_ENDPOINT_URL

echo "########## Secrets Initialized ##########"
