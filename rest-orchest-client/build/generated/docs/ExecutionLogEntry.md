

# ExecutionLogEntry


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**nodeId** | **String** |  |  [optional] |
|**nodeName** | **String** |  |  [optional] |
|**nodeType** | [**NodeTypeEnum**](#NodeTypeEnum) |  |  [optional] |
|**sourceNodeId** | **String** |  |  [optional] |
|**sequenceFlowIds** | **Set&lt;String&gt;** |  |  [optional] |
|**stateChanges** | [**List&lt;StateChange&gt;**](StateChange.md) |  |  [optional] |
|**metaData** | **Map&lt;String, Object&gt;** |  |  [optional] |
|**state** | [**StateEnum**](#StateEnum) |  |  [optional] |



## Enum: NodeTypeEnum

| Name | Value |
|---- | -----|
| START_EVENT | &quot;START_EVENT&quot; |
| END_EVENT | &quot;END_EVENT&quot; |
| TASK | &quot;TASK&quot; |
| USER_TASK | &quot;USER_TASK&quot; |
| SERVICE_TASK | &quot;SERVICE_TASK&quot; |
| RECEIVE_TASK | &quot;RECEIVE_TASK&quot; |
| SEND_TASK | &quot;SEND_TASK&quot; |
| BUSINESS_RULE_TASK | &quot;BUSINESS_RULE_TASK&quot; |
| SCRIPT_TASK | &quot;SCRIPT_TASK&quot; |
| MANUAL_TASK | &quot;MANUAL_TASK&quot; |
| SUB_PROCESS | &quot;SUB_PROCESS&quot; |
| CALL_ACTIVITY | &quot;CALL_ACTIVITY&quot; |
| EXCLUSIVE_GATEWAY | &quot;EXCLUSIVE_GATEWAY&quot; |
| PARALLEL_GATEWAY | &quot;PARALLEL_GATEWAY&quot; |
| INCLUSIVE_GATEWAY | &quot;INCLUSIVE_GATEWAY&quot; |
| EVENT_BASED_GATEWAY | &quot;EVENT_BASED_GATEWAY&quot; |
| BOUNDARY_EVENT | &quot;BOUNDARY_EVENT&quot; |
| INTERMEDIATE_CATCH_EVENT | &quot;INTERMEDIATE_CATCH_EVENT&quot; |
| INTERMEDIATE_THROW_EVENT | &quot;INTERMEDIATE_THROW_EVENT&quot; |



## Enum: StateEnum

| Name | Value |
|---- | -----|
| TRIGGERED | &quot;TRIGGERED&quot; |
| REGISTERED | &quot;REGISTERED&quot; |
| STARTED | &quot;STARTED&quot; |
| PENDING | &quot;PENDING&quot; |
| INCIDENT | &quot;INCIDENT&quot; |
| FAILED | &quot;FAILED&quot; |
| COMPLETED | &quot;COMPLETED&quot; |
| CANCELLED | &quot;CANCELLED&quot; |


## Implemented Interfaces

* Serializable


