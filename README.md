## Micronaut 4.1.6 Documentation

- [User Guide](https://docs.micronaut.io/4.1.6/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.1.6/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.1.6/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

---

# How to run backend

- Install JDK > 17.0
- .\gradlew clean
- .\gradlew build
- .\gradlew run -t
  Server will be running on http://localhost
  Swagger url http://localhost/swagger-ui/#/default

# Used Technology

- Dependency Injection
- Domain Driven Design
- Groovy
- Micronaut

## Terraform Deployment

- AWS Fargate ( aws_deployment.md)

# Azure Container Instance Workflow

Workflow file: [`.github/workflows/azure-container-instance.yml`](.github/workflows/azure-container-instance.yml)

### Workflow description

For pushes to the `master` branch, the workflow will:

1. Setup the build environment with respect to the selected java/graalvm version.
2. Login to Docker registry.
3. Login to [Azure Command-Line Interface](https://docs.microsoft.com/cs-cz/cli/azure/).
4. Build, tag and push Docker image with Micronaut application to the Docker Registry.
5. Deploy to [Azure Container Instances](https://docs.microsoft.com/cs-cz/azure/container-instances/).

### Dependencies on other GitHub Actions

- [Login to Docker Registry `docker/login`](https://github.com/docker/login-action)
- [Setup GraalVM `DeLaGuardo/setup-graalvm`](https://github.com/DeLaGuardo/setup-graalvm)
- [Setup Azure CLI `azure/login`](https://github.com/Azure/login)

### Setup

Add the following GitHub secrets:


| Name                   | Description                                                                                                                                                                                                                                                                                       |
| ------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| DOCKER_USERNAME        | Docker registry username. In case of Azure Container Registry, provide Azure username or Service principal ID, see more on[Azure Container Registry authentication with service principals](https://docs.microsoft.com/en-us/azure/container-registry/container-registry-auth-service-principal). |
| DOCKER_PASSWORD        | Docker registry password. In case of Azure Container Registry, provide Azure password or Service principal password.                                                                                                                                                                              |
| DOCKER_REPOSITORY_PATH | Docker image repository. In case of Azure Container Registry, for image`micronaut.azurecr.io/foo/bar:0.1`, the `foo` is an _image repository_.                                                                                                                                                    |
| DOCKER_REGISTRY_URL    | Docker registry url. In case of Azure Container Registry use the Container registry login path, e.g. for the image`micronaut.azurecr.io/foo/bar:0.1`, the `micronaut.azurecr.io` is a _registry url_.                                                                                             |
| AZURE_CREDENTIALS      | Azure Service Principal, see more on[Azure/aci-deploy#Azure Service Principal for RBAC](https://github.com/Azure/aci-deploy#azure-service-principal-for-rbac).                                                                                                                                    |
| AZURE_RESOURCE_GROUP   | Azure Resource Group name, see more on[Resource groups](https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/overview#resource-groups).                                                                                                                                       |

The workflow file also contains additional configuration options that are now configured to:


| Name            | Description                                                                                                                                                                                                                                                                   | Default value |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- |
| AZURE_LOCATION  | Location where the Container Instance will be created. See[Resource availability for Azure Container Instances in Azure regions](https://docs.microsoft.com/en-us/azure//container-instances/container-instances-region-availability) to find out what regions are supported. | `westeurope`  |
| AZURE_DNS_LABEL | The dns name label for container group with public IP.                                                                                                                                                                                                                        | `SuperDac`    |

### Verification

Call the rest api endpoint `[AZURE_DNS_LABEL].[AZURE_LOCATION].azurecontainer.io:[PORT]/superDac`:

```
curl http://SuperDac.westeurope.westeurope.azurecontainer.io:8080/superDac
```

- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [Shadow Gradle Plugin](https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow)

## Feature serialization-jackson documentation

- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)

## Feature openapi documentation

- [Micronaut OpenAPI Support documentation](https://micronaut-projects.github.io/micronaut-openapi/latest/guide/index.html)
- [https://www.openapis.org](https://www.openapis.org)

## Feature micronaut-aop documentation

- [Micronaut Aspect-Oriented Programming (AOP) documentation](https://docs.micronaut.io/latest/guide/index.html#aop)

## Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#nettyHttpClient)

## Feature swagger-ui documentation

- [Micronaut Swagger UI documentation](https://micronaut-projects.github.io/micronaut-openapi/latest/guide/index.html)
- [https://swagger.io/tools/swagger-ui/](https://swagger.io/tools/swagger-ui/)

## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)

## Feature http-session documentation

- [Micronaut HTTP Sessions documentation](https://docs.micronaut.io/latest/guide/index.html#sessions)

## Feature github-workflow-azure-container-instance documentation

- [https://docs.github.com/en/free-pro-team@latest/actions](https://docs.github.com/en/free-pro-team@latest/actions)
