# Hostfully Connector

A queue driven integration with the Hostfully PMS API <https://dev.hostfully.com/reference>. The service will long poll an SQS queue and process messages. Upon completion of a job, the service will send a message to the inbound queue for the `Legacy Sync Connector` in order to trigger a sync between the new data and the legacy portal.

This application has a Terraform dependency on the `Legacy Sync Connector`. It must use `Legacy SYnc Connector`'s remote state in order to get access to the connector's inbound queue so that it can send it messages to trigger runs.

## Terraform / AWS

| path            | description                                |
| --------------- | ------------------------------------------ |
| ./src/terraform | root directory for terraform configuration |

## Application Code

| path       | description                  |
| ---------- | ---------------------------- |
| ./src/main | root directory for java code |

## Tools

| path               | description                                                         |
| ------------------ | ------------------------------------------------------------------- |
| ./tools/deploy.ps1 | calls CircleCI pipeline to deploy the app `help ./tools/deploy.ps1` |
| ./mvnw             | Maven wrapper                                                       |

## IntelliJ Run/Debug Configuration

1. Active Profiles: `local`
2. Environment Variables: `ISLOCAL=true;SSM_PREFIX=<placeholder>`

    | name       | value                 |
    | ---------- | --------------------- |
    | IS_LOCAL   | true                  |
    | SSM_PREFIX | from terraform output |

3. Override parameters:

    | name          | value |
    | ------------- | ----- |
    | user.timezone | UTC   |

## Local Development Workflow

1. Create feature branch in git `git checkout -b <branchname>`
2. Run Terraform `plan`/`apply` in `./src/terraform/src` to create branch resources in the development account with the branch name as the prefix
3. Code
4. Run or debug the application
5. Push branch and create PR
6. Clean up branch resources by running Terraform `destroy` in `./src/terraform/src`
