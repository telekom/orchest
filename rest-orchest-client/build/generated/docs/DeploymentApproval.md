

# DeploymentApproval


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **String** |  |  [optional] |
|**reviewers** | **List&lt;String&gt;** |  |  [optional] |
|**requestedBy** | **String** |  |  [optional] |
|**state** | [**StateEnum**](#StateEnum) |  |  [optional] |
|**resourceType** | **String** |  |  [optional] |
|**resourceDeploymentRequest** | [**ResourceDeploymentRequest**](ResourceDeploymentRequest.md) |  |  [optional] |
|**definitionId** | **String** |  |  [optional] |
|**nextVersion** | **String** |  |  [optional] |
|**auditLog** | [**DeploymentAuditLog**](DeploymentAuditLog.md) |  |  [optional] |
|**createdAt** | **OffsetDateTime** |  |  [optional] |
|**approvalTime** | **OffsetDateTime** |  |  [optional] |



## Enum: StateEnum

| Name | Value |
|---- | -----|
| REQUESTED | &quot;REQUESTED&quot; |
| ACCEPTED | &quot;ACCEPTED&quot; |
| REJECTED | &quot;REJECTED&quot; |


## Implemented Interfaces

* Serializable


