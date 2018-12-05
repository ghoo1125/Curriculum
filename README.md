# Curriculum
Curriculum is a web service providing teachers to open courses and students to select courses.
Besides, the web service is built into a docker image and deployed on AWS with ECS Fargate. The instructions of building the project and setting up AWS environment are documented below.


## Build
Curriculum is built using [Parsec](https://yahoo.github.io/parsec/) open source. On MacOS please follow the steps.


#### _MacOS_
Download java 1.8 and gradle 2.14 via Homebrew:
~~~~
$ brew cask install java8
$ brew install gradle@2.14
~~~~

Then paste following code to _**~/.gradle/init.gradle**_ to apply parsec gradle plugin:
~~~~
gradle.beforeProject { prj ->
   prj.apply from: 'https://raw.githubusercontent.com/yahoo/parsec/master/parsec-template-plugin/installation/apply.groovy'
}
~~~~

Create project with the plugin. Where __groupId__ refers to the namespace of your package, while the __artifactId__ is your project name:
~~~~
$ gradle createParsecProject -PgroupId='your.group.name' -PartifactId='your_project_name'
~~~~

Run the command to build the project into war file:
~~~~
$ gradle war
~~~~

#### _Note_
- There are some problems in building project with Parsec using latest gradle version,
  though the document says that it supports gradle version >2.4.
- To dynamically change gradle version, you can use [gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) instead.

## Docker
You should download [Docker](https://docs.docker.com/docker-for-mac/install/) before building project into Docker image.

#### _Build Docker Image_
Since the web service built with Parsec is deployed with jetty server. We build our image based on [jetty image](https://hub.docker.com/_/jetty/).
Add a _**Dockefile**_ under the root directory of your project with following content:
~~~~
FROM jetty
COPY ./build/libs/your_project_name_version.war $JETTY_BASE/webapps/ROOT.war
~~~~
Here we rename the file as ROOT.war so jetty will deploy the service with a [context path](https://www.eclipse.org/jetty/documentation/9.4.x/configuring-contexts.html) of /.

Then run the command to build the docker image with a specified tag name:
~~~~
$ docker build -t a_tag_name
~~~~

Run the command to check whether your image is built succesfully:
~~~~
$ docker image ls
~~~~

## AWS Environment Setup
To deploy the service on AWS, please make sure you have registered an AWS account and installed [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-install-macos.html) in advance.

#### _Elastic Container Registry (ECR)_
ECR is a fully-managed Docker container registry that makes it easy for developers to store, manage, and deploy Docker container images.
To push our image to ECR please follow the steps:

1. Create a access key through [AWS console](https://aws.amazon.com/console/) by navigating to __My Security Credentials__ panel. Then set [access key](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html) and
[region](https://docs.aws.amazon.com/general/latest/gr/rande.html) with the command:
    ~~~~
    $ aws configure
    ~~~~
    __Note:__ The command will create a profile under _~/.aws_ folder.

2. Run the command to authenticate Docker to ECR, then execute the output of the command again:
    ~~~~
    $ aws ecr get-login —no-include-email
    ~~~~

3. Create an ECR repo through AWS console then tag your image in the format of ecr_repo_name:tag_name:
    ~~~~
    $ docker tag repo_name:tag_name ecr_repo_name:tag_name
    ~~~~
    __Note:__ You can create and check your _ecr_repo_name_ through AWS console by navigating to ECS and select __Repositories__.

4. Push docker image to ECR:
    ~~~~
    $ docker push ecr_repo_name:tag_name
    ~~~~

#### _Elastic Container Service (ECS)_
Amazon Elastic Container Service (Amazon ECS) is a highly scalable, high-performance container orchestration service that supports Docker containers and allows you to easily run and scale containerized applications on AWS.
To deploy web service on AWS with ECS Fargate please follow the steps:

1. Navigate to ECS on AWS console then select __Clusters__. And create a cluster with Fargate launch type. You can use your own VPC or use the default VPC later. 
2. Navigate to ECS on AWS console then select __Task Definitions__. Next, create a task definition and add a container with the __image path set to your image stored in ECR__ (In the format of registry/repository:tag).
For example: _aws_account_id_.dkr.ecr._region_.amazonaws.com/_my-web-app_:_latest_
3. Navigate to ECS on AWS console then select __Clusters__. Create a task in the cluster you just created
in previous step. And make sure you __open port 8080 in the security group__ (default service port used by jetty server).

If everything works fine, you should see a instance is launched, and you are able to connect to the service through the public IP.

## DynamoDB
We use DynamoDB as our database in Curriculum.

#### _Using DynamoDB JAVA API_
Add following content to _build.gradle_ file under the project directory.
~~~~
repositories {
    maven {
      url 'https://s3-ap-northeast-1.amazonaws.com/dynamodb-local-tokyo/release'
    }
}

dependencies {
    compile 'com.amazonaws:aws-java-sdk-dynamodb:1.11.86'
}
~~~~
Then you are good to import and develop with DynamoDB API. Besides, you have to create a new [IAM role](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles.html)
with the policy __AmazonDynamoDBFullAccess__ attached if you want to deploy the web service on AWS. 

#### _Note_
- You can also access the DynamoDB in a specific region directly with _aws dynamodb_ command.
- You can also [download DynamoDB](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html) and test it locally.

## ScrewDriver

TBD
