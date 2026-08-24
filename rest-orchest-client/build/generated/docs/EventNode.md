

# EventNode


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**eventType** | [**EventTypeEnum**](#EventTypeEnum) |  |  [optional] |
|**attachedToId** | **String** |  |  [optional] |
|**messageName** | **String** |  |  [optional] |
|**messageCorrelationKey** | **String** |  |  [optional] |
|**signalRef** | **String** |  |  [optional] |
|**timerDuration** | **String** |  |  [optional] |
|**timerDate** | **String** |  |  [optional] |
|**timerCycle** | **String** |  |  [optional] |
|**errorRef** | **String** |  |  [optional] |
|**errorCode** | **String** |  |  [optional] |
|**escalationRef** | **String** |  |  [optional] |
|**escalationCode** | **String** |  |  [optional] |
|**condition** | **String** |  |  [optional] |
|**linkName** | **String** |  |  [optional] |
|**zeebeCorrelationKey** | **String** |  |  [optional] |
|**cancelActivity** | **Boolean** |  |  [optional] |



## Enum: EventTypeEnum

| Name | Value |
|---- | -----|
| NONE | &quot;NONE&quot; |
| TIMER | &quot;TIMER&quot; |
| MESSAGE | &quot;MESSAGE&quot; |
| SIGNAL | &quot;SIGNAL&quot; |
| ERROR | &quot;ERROR&quot; |
| ESCALATION | &quot;ESCALATION&quot; |
| CONDITIONAL | &quot;CONDITIONAL&quot; |
| COMPENSATION | &quot;COMPENSATION&quot; |
| LINK | &quot;LINK&quot; |
| TERMINATE | &quot;TERMINATE&quot; |
| MULTIPLE | &quot;MULTIPLE&quot; |
| PARALLEL_MULTIPLE | &quot;PARALLEL_MULTIPLE&quot; |
| CANCEL | &quot;CANCEL&quot; |


## Implemented Interfaces

* Serializable


