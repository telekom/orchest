# UserTasks

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**claimTask**](UserTasks.md#claimTask) | **POST** /userTasks/{taskId}/claim | Claim a user task |
| [**claimTaskWithHttpInfo**](UserTasks.md#claimTaskWithHttpInfo) | **POST** /userTasks/{taskId}/claim | Claim a user task |
| [**completeTask**](UserTasks.md#completeTask) | **POST** /userTasks/{taskId}/complete | Complete a user task |
| [**completeTaskWithHttpInfo**](UserTasks.md#completeTaskWithHttpInfo) | **POST** /userTasks/{taskId}/complete | Complete a user task |
| [**getMyTasks**](UserTasks.md#getMyTasks) | **GET** /userTasks | List available user tasks |
| [**getMyTasksWithHttpInfo**](UserTasks.md#getMyTasksWithHttpInfo) | **GET** /userTasks | List available user tasks |
| [**getTask**](UserTasks.md#getTask) | **GET** /userTasks/{taskId} | Get user task details |
| [**getTaskWithHttpInfo**](UserTasks.md#getTaskWithHttpInfo) | **GET** /userTasks/{taskId} | Get user task details |
| [**getTasksForProcessInstance**](UserTasks.md#getTasksForProcessInstance) | **GET** /userTasks/process/{processInstanceId} | List tasks for process instance |
| [**getTasksForProcessInstanceWithHttpInfo**](UserTasks.md#getTasksForProcessInstanceWithHttpInfo) | **GET** /userTasks/process/{processInstanceId} | List tasks for process instance |
| [**reassignTask**](UserTasks.md#reassignTask) | **PATCH** /userTasks/{taskId}/assign | Reassign a user task |
| [**reassignTaskWithHttpInfo**](UserTasks.md#reassignTaskWithHttpInfo) | **PATCH** /userTasks/{taskId}/assign | Reassign a user task |
| [**unclaimTask**](UserTasks.md#unclaimTask) | **POST** /userTasks/{taskId}/unclaim | Unclaim a user task |
| [**unclaimTaskWithHttpInfo**](UserTasks.md#unclaimTaskWithHttpInfo) | **POST** /userTasks/{taskId}/unclaim | Unclaim a user task |



## claimTask

> ResponseDTOUserTaskDTO claimTask(taskId)

Claim a user task

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String taskId = "taskId_example"; // String | 
        try {
            ResponseDTOUserTaskDTO result = apiInstance.claimTask(taskId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#claimTask");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **taskId** | **String**|  | |

### Return type

[**ResponseDTOUserTaskDTO**](ResponseDTOUserTaskDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## claimTaskWithHttpInfo

> ApiResponse<ResponseDTOUserTaskDTO> claimTask claimTaskWithHttpInfo(taskId)

Claim a user task

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String taskId = "taskId_example"; // String | 
        try {
            ApiResponse<ResponseDTOUserTaskDTO> response = apiInstance.claimTaskWithHttpInfo(taskId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#claimTask");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **taskId** | **String**|  | |

### Return type

ApiResponse<[**ResponseDTOUserTaskDTO**](ResponseDTOUserTaskDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## completeTask

> ResponseDTOUserTaskDTO completeTask(taskId, completeUserTaskRequest)

Complete a user task

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String taskId = "taskId_example"; // String | 
        CompleteUserTaskRequest completeUserTaskRequest = new CompleteUserTaskRequest(); // CompleteUserTaskRequest | 
        try {
            ResponseDTOUserTaskDTO result = apiInstance.completeTask(taskId, completeUserTaskRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#completeTask");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **taskId** | **String**|  | |
| **completeUserTaskRequest** | [**CompleteUserTaskRequest**](CompleteUserTaskRequest.md)|  | [optional] |

### Return type

[**ResponseDTOUserTaskDTO**](ResponseDTOUserTaskDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## completeTaskWithHttpInfo

> ApiResponse<ResponseDTOUserTaskDTO> completeTask completeTaskWithHttpInfo(taskId, completeUserTaskRequest)

Complete a user task

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String taskId = "taskId_example"; // String | 
        CompleteUserTaskRequest completeUserTaskRequest = new CompleteUserTaskRequest(); // CompleteUserTaskRequest | 
        try {
            ApiResponse<ResponseDTOUserTaskDTO> response = apiInstance.completeTaskWithHttpInfo(taskId, completeUserTaskRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#completeTask");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **taskId** | **String**|  | |
| **completeUserTaskRequest** | [**CompleteUserTaskRequest**](CompleteUserTaskRequest.md)|  | [optional] |

### Return type

ApiResponse<[**ResponseDTOUserTaskDTO**](ResponseDTOUserTaskDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getMyTasks

> PageUserTaskDTO getMyTasks(page, size, sort)

List available user tasks

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        String sort = "-createdAt"; // String | 
        try {
            PageUserTaskDTO result = apiInstance.getMyTasks(page, size, sort);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#getMyTasks");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sort** | **String**|  | [optional] [default to -createdAt] |

### Return type

[**PageUserTaskDTO**](PageUserTaskDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getMyTasksWithHttpInfo

> ApiResponse<PageUserTaskDTO> getMyTasks getMyTasksWithHttpInfo(page, size, sort)

List available user tasks

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        String sort = "-createdAt"; // String | 
        try {
            ApiResponse<PageUserTaskDTO> response = apiInstance.getMyTasksWithHttpInfo(page, size, sort);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#getMyTasks");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sort** | **String**|  | [optional] [default to -createdAt] |

### Return type

ApiResponse<[**PageUserTaskDTO**](PageUserTaskDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getTask

> ResponseDTOUserTaskDTO getTask(taskId)

Get user task details

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String taskId = "taskId_example"; // String | 
        try {
            ResponseDTOUserTaskDTO result = apiInstance.getTask(taskId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#getTask");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **taskId** | **String**|  | |

### Return type

[**ResponseDTOUserTaskDTO**](ResponseDTOUserTaskDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getTaskWithHttpInfo

> ApiResponse<ResponseDTOUserTaskDTO> getTask getTaskWithHttpInfo(taskId)

Get user task details

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String taskId = "taskId_example"; // String | 
        try {
            ApiResponse<ResponseDTOUserTaskDTO> response = apiInstance.getTaskWithHttpInfo(taskId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#getTask");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **taskId** | **String**|  | |

### Return type

ApiResponse<[**ResponseDTOUserTaskDTO**](ResponseDTOUserTaskDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getTasksForProcessInstance

> PageUserTaskDTO getTasksForProcessInstance(processInstanceId, page, size, sort)

List tasks for process instance

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String processInstanceId = "processInstanceId_example"; // String | 
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        String sort = "-createdAt"; // String | 
        try {
            PageUserTaskDTO result = apiInstance.getTasksForProcessInstance(processInstanceId, page, size, sort);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#getTasksForProcessInstance");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **processInstanceId** | **String**|  | |
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sort** | **String**|  | [optional] [default to -createdAt] |

### Return type

[**PageUserTaskDTO**](PageUserTaskDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getTasksForProcessInstanceWithHttpInfo

> ApiResponse<PageUserTaskDTO> getTasksForProcessInstance getTasksForProcessInstanceWithHttpInfo(processInstanceId, page, size, sort)

List tasks for process instance

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String processInstanceId = "processInstanceId_example"; // String | 
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        String sort = "-createdAt"; // String | 
        try {
            ApiResponse<PageUserTaskDTO> response = apiInstance.getTasksForProcessInstanceWithHttpInfo(processInstanceId, page, size, sort);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#getTasksForProcessInstance");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **processInstanceId** | **String**|  | |
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sort** | **String**|  | [optional] [default to -createdAt] |

### Return type

ApiResponse<[**PageUserTaskDTO**](PageUserTaskDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## reassignTask

> ResponseDTOUserTaskDTO reassignTask(taskId, assignUserTaskRequest)

Reassign a user task

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String taskId = "taskId_example"; // String | 
        AssignUserTaskRequest assignUserTaskRequest = new AssignUserTaskRequest(); // AssignUserTaskRequest | 
        try {
            ResponseDTOUserTaskDTO result = apiInstance.reassignTask(taskId, assignUserTaskRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#reassignTask");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **taskId** | **String**|  | |
| **assignUserTaskRequest** | [**AssignUserTaskRequest**](AssignUserTaskRequest.md)|  | |

### Return type

[**ResponseDTOUserTaskDTO**](ResponseDTOUserTaskDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## reassignTaskWithHttpInfo

> ApiResponse<ResponseDTOUserTaskDTO> reassignTask reassignTaskWithHttpInfo(taskId, assignUserTaskRequest)

Reassign a user task

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String taskId = "taskId_example"; // String | 
        AssignUserTaskRequest assignUserTaskRequest = new AssignUserTaskRequest(); // AssignUserTaskRequest | 
        try {
            ApiResponse<ResponseDTOUserTaskDTO> response = apiInstance.reassignTaskWithHttpInfo(taskId, assignUserTaskRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#reassignTask");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **taskId** | **String**|  | |
| **assignUserTaskRequest** | [**AssignUserTaskRequest**](AssignUserTaskRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTOUserTaskDTO**](ResponseDTOUserTaskDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## unclaimTask

> ResponseDTOUserTaskDTO unclaimTask(taskId)

Unclaim a user task

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String taskId = "taskId_example"; // String | 
        try {
            ResponseDTOUserTaskDTO result = apiInstance.unclaimTask(taskId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#unclaimTask");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **taskId** | **String**|  | |

### Return type

[**ResponseDTOUserTaskDTO**](ResponseDTOUserTaskDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## unclaimTaskWithHttpInfo

> ApiResponse<ResponseDTOUserTaskDTO> unclaimTask unclaimTaskWithHttpInfo(taskId)

Unclaim a user task

### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserTasks;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserTasks apiInstance = new UserTasks(defaultClient);
        String taskId = "taskId_example"; // String | 
        try {
            ApiResponse<ResponseDTOUserTaskDTO> response = apiInstance.unclaimTaskWithHttpInfo(taskId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserTasks#unclaimTask");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **taskId** | **String**|  | |

### Return type

ApiResponse<[**ResponseDTOUserTaskDTO**](ResponseDTOUserTaskDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

