

# BaseNode


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **String** |  |  [optional] |
|**name** | **String** |  |  [optional] |
|**type** | [**TypeEnum**](#TypeEnum) |  |  [optional] |
|**scopeId** | **String** |  |  [optional] |
|**scopeType** | [**ScopeTypeEnum**](#ScopeTypeEnum) |  |  [optional] |
|**outgoingSequenceFlowIds** | **Map&lt;String, String&gt;** |  |  [optional] |
|**incomingSequenceFlowIds** | **Map&lt;String, String&gt;** |  |  [optional] |
|**inputMappings** | [**List&lt;DataMapping&gt;**](DataMapping.md) |  |  [optional] |
|**outputMappings** | [**List&lt;DataMapping&gt;**](DataMapping.md) |  |  [optional] |
|**properties** | **Map&lt;String, Object&gt;** |  |  [optional] |
|**atClass** | **String** |  |  |



## Enum: TypeEnum

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



## Enum: ScopeTypeEnum

| Name | Value |
|---- | -----|
| PROCESS | &quot;PROCESS&quot; |
| SUBPROCESS | &quot;SUBPROCESS&quot; |
| EVENT_SUBPROCESS | &quot;EVENT_SUBPROCESS&quot; |


## Implemented Interfaces

* Serializable


