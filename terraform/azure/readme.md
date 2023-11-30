# Azure Container Instance with public IP



## Variables

| Name | Description | Default |
|-|-|-|
| `resource_group_name_prefix` | Prefix of the resource group name that's combined with a random ID so name is unique in your Azure subscription. | rg |
| `resource_group_location` | Location of the resource group. | eastus |
| `image` | Container image to deploy. | mcr.microsoft.com/azuredocs/aci-helloworld" |
| `port` | Port to open on the container and the public IP address. | 80 |
| `cpu_cores` | Quantity of CPU cores to allocate to the container. | 1 |
| `memory_in_gb` | Amount of memory to allocate to the container in gigabytes. | 2 |
| `restart_policy` | Behavior of Azure runtime if container has stopped. | Always |

