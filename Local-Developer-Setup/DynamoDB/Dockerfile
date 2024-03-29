# Get the unzipped AWS CLI
FROM alpine:3.16 as awscli
RUN apk update && apk add --no-cache \
    curl \
    unzip \
    && curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip

FROM amazon/dynamodb-local:latest
# Add the AWS CLI from the previous stage here so we can use it to prepopulate tables upon startup
COPY --from=awscli aws ./
USER root
RUN yum -y install sudo less \
    && yum -y clean all \
    && rm -rf /var/cache \
    && sudo ./install \
    && rm -rf ./install \
    && mkdir -p sggc-setup
# Run script to start local dynamodb and populate it with data
COPY init-db.sh sggc-setup/init-db.sh
# Mark copied file as executable to run on AWS CodeBuild
RUN  chmod +x ./sggc-setup/init-db.sh
COPY tables sggc-setup/tables
ENTRYPOINT ["/bin/bash" ,"-c", "sggc-setup/init-db.sh"]

