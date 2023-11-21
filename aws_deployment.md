# How to Deploy on AWS Fargate

### Build Docker Image and deploy on AWS ECR

- Prerequisites
  Install Docker
  Install AWS-cli
- Configure AWS cli
  Detailed Instruction can be found [here](https://k21academy.com/amazon-web-services/aws-cli/).
- deploy on AWS ECR
  Detailed Instruction can be found [here](https://www.freecodecamp.org/news/build-and-push-docker-images-to-aws-ecr/).

```
.\gradlew clean 
.\gradlew build
aws configure
aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws/b2m0y9g8
docker build -t okta-dac .
docker tag okta-dac:latest public.ecr.aws/b2m0y9g8/okta-dac:latest
docker push public.ecr.aws/b2m0y9g8/okta-dac:latest

```

At this momoent, you will get the public aws ecr image url.

(ex: The push refers to repository [public.ecr.aws/b2m0y9g8/okta-dac] )

(Tip: You don't need to deploy docker image on ecr, because you can deploy aws fargate from ecr image we've already deployed.

This instruction is only for transparency and hobbist)

### How to deploy AWS Fargate and Load Balancer using Terraform

Terraform files can be found in terraform/aws directory.

- install Terraform
- deploy using Terraform

  ```
  cd terraform
  cd aws
  terraform init
  terraform apply

  ```

  (Replace aws access key and aws secret key in variables.tf with real value!)

After you running terraform apply, you will get the load balancer url(ex: http://pat-lb-1368677718.us-east-1.elb.amazonaws.com/)
