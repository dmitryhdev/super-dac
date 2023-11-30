# How to Deploy on Azure ACI

### Build Docker Image and deploy on Dockerhub

Detailed instructions can be found [here](https://www.stacksimplify.com/aws-eks/docker-basics/build-docker-image/).

Docker Image url should be used in varaible.tf(terraform/azure/readme.md)

### How to deploy AWS Fargate and Load Balancer using Terraform

Terraform files can be found in terraform/aws directory.

- install Terraform
- deploy using Terraform

  ```
  cd terraform
  cd azure
  terraform init
  terraform apply

  ```

After you running terraform apply, you will get the ip address of deployed server.
