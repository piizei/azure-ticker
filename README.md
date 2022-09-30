# Example of real-time flow of data from messaging to UI in Azure

## Create infra

### Function app

Function app will process the incoming data from service-bus and relay it to Signal-R

```
export RESOURCE_GROUP=MyTickTesting
export APP_NAME=UniqueName
az group create -n $RESOURCE_GROUP -l westeurope

az deployment group create \
  --name funcdeployment \
  --resource-group $RESOURCE_GROUP \
  --template-file infra/function.bicep \
  --parameters "appName=$APP_NAME" 

```

### Service bus

```
az deployment group create \
  --name sbdeployment \
  --resource-group $RESOURCE_GROUP \
  --template-file infra/servicebus.bicep \
  --parameters "appName=$APP_NAME" 

```

### Grant function app permission to use the service bus

```
 identity=$(az functionapp identity show --name tickfunc --resource-group $RESOURCE_GROUP --name $APP_NAME --query principalId --out tsv)
 busid=$(az servicebus namespace show  -n $APP_NAME --resource-group $RESOURCE_GROUP --query "id" --out tsv)
 az role assignment create --assignee $identity --role 'Azure Service Bus Data Sender' --scope $busid
 az role assignment create --assignee $identity --role 'Azure Service Bus Data Receiver' --scope $busid
 ```

### Signal-R

```
az deployment group create \
  --name signalrdeployment \
  --resource-group $RESOURCE_GROUP \
  --template-file infra/signalr.bicep \
  --parameters "appName=$APP_NAME" 

```