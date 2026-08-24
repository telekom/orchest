# ProcessInstanceApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**cancelInstances**](ProcessInstanceApi.md#cancelInstances) | **PATCH** /processInstances/cancelInstances |  |
| [**cancelInstancesWithHttpInfo**](ProcessInstanceApi.md#cancelInstancesWithHttpInfo) | **PATCH** /processInstances/cancelInstances |  |
| [**cancelInstancesBatch**](ProcessInstanceApi.md#cancelInstancesBatch) | **POST** /processInstances/cancelBatch |  |
| [**cancelInstancesBatchWithHttpInfo**](ProcessInstanceApi.md#cancelInstancesBatchWithHttpInfo) | **POST** /processInstances/cancelBatch |  |
| [**createProcessInstance**](ProcessInstanceApi.md#createProcessInstance) | **POST** /processInstances/create |  |
| [**createProcessInstanceWithHttpInfo**](ProcessInstanceApi.md#createProcessInstanceWithHttpInfo) | **POST** /processInstances/create |  |
| [**getPageData**](ProcessInstanceApi.md#getPageData) | **GET** /processInstances/pageData |  |
| [**getPageDataWithHttpInfo**](ProcessInstanceApi.md#getPageDataWithHttpInfo) | **GET** /processInstances/pageData |  |
| [**getProcessInstance**](ProcessInstanceApi.md#getProcessInstance) | **GET** /processInstances/id/{processInstanceId} |  |
| [**getProcessInstanceWithHttpInfo**](ProcessInstanceApi.md#getProcessInstanceWithHttpInfo) | **GET** /processInstances/id/{processInstanceId} |  |
| [**modifyInstance**](ProcessInstanceApi.md#modifyInstance) | **PATCH** /processInstances/modifyInstance |  |
| [**modifyInstanceWithHttpInfo**](ProcessInstanceApi.md#modifyInstanceWithHttpInfo) | **PATCH** /processInstances/modifyInstance |  |
| [**resolveIncident**](ProcessInstanceApi.md#resolveIncident) | **POST** /processInstances/resolveIncident |  |
| [**resolveIncidentWithHttpInfo**](ProcessInstanceApi.md#resolveIncidentWithHttpInfo) | **POST** /processInstances/resolveIncident |  |
| [**retryInstance**](ProcessInstanceApi.md#retryInstance) | **POST** /processInstances/retry |  |
| [**retryInstanceWithHttpInfo**](ProcessInstanceApi.md#retryInstanceWithHttpInfo) | **POST** /processInstances/retry |  |
| [**retryInstancesBatch**](ProcessInstanceApi.md#retryInstancesBatch) | **POST** /processInstances/retryBatch |  |
| [**retryInstancesBatchWithHttpInfo**](ProcessInstanceApi.md#retryInstancesBatchWithHttpInfo) | **POST** /processInstances/retryBatch |  |
| [**scrollProcessInstances**](ProcessInstanceApi.md#scrollProcessInstances) | **GET** /processInstances/scroll |  |
| [**scrollProcessInstancesWithHttpInfo**](ProcessInstanceApi.md#scrollProcessInstancesWithHttpInfo) | **GET** /processInstances/scroll |  |



## cancelInstances

> String cancelInstances(cancelInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        CancelInstanceRequest cancelInstanceRequest = new CancelInstanceRequest(); // CancelInstanceRequest | 
        try {
            String result = apiInstance.cancelInstances(cancelInstanceRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#cancelInstances");
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
| **cancelInstanceRequest** | [**CancelInstanceRequest**](CancelInstanceRequest.md)|  | |

### Return type

**String**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## cancelInstancesWithHttpInfo

> ApiResponse<String> cancelInstances cancelInstancesWithHttpInfo(cancelInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        CancelInstanceRequest cancelInstanceRequest = new CancelInstanceRequest(); // CancelInstanceRequest | 
        try {
            ApiResponse<String> response = apiInstance.cancelInstancesWithHttpInfo(cancelInstanceRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#cancelInstances");
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
| **cancelInstanceRequest** | [**CancelInstanceRequest**](CancelInstanceRequest.md)|  | |

### Return type

ApiResponse<**String**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## cancelInstancesBatch

> String cancelInstancesBatch(batchInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        BatchInstanceRequest batchInstanceRequest = new BatchInstanceRequest(); // BatchInstanceRequest | 
        try {
            String result = apiInstance.cancelInstancesBatch(batchInstanceRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#cancelInstancesBatch");
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
| **batchInstanceRequest** | [**BatchInstanceRequest**](BatchInstanceRequest.md)|  | |

### Return type

**String**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## cancelInstancesBatchWithHttpInfo

> ApiResponse<String> cancelInstancesBatch cancelInstancesBatchWithHttpInfo(batchInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        BatchInstanceRequest batchInstanceRequest = new BatchInstanceRequest(); // BatchInstanceRequest | 
        try {
            ApiResponse<String> response = apiInstance.cancelInstancesBatchWithHttpInfo(batchInstanceRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#cancelInstancesBatch");
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
| **batchInstanceRequest** | [**BatchInstanceRequest**](BatchInstanceRequest.md)|  | |

### Return type

ApiResponse<**String**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## createProcessInstance

> ResponseDTOProcessInvocationResponse createProcessInstance(processInvocationRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        ProcessInvocationRequest processInvocationRequest = new ProcessInvocationRequest(); // ProcessInvocationRequest | 
        try {
            ResponseDTOProcessInvocationResponse result = apiInstance.createProcessInstance(processInvocationRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#createProcessInstance");
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
| **processInvocationRequest** | [**ProcessInvocationRequest**](ProcessInvocationRequest.md)|  | |

### Return type

[**ResponseDTOProcessInvocationResponse**](ResponseDTOProcessInvocationResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## createProcessInstanceWithHttpInfo

> ApiResponse<ResponseDTOProcessInvocationResponse> createProcessInstance createProcessInstanceWithHttpInfo(processInvocationRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        ProcessInvocationRequest processInvocationRequest = new ProcessInvocationRequest(); // ProcessInvocationRequest | 
        try {
            ApiResponse<ResponseDTOProcessInvocationResponse> response = apiInstance.createProcessInstanceWithHttpInfo(processInvocationRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#createProcessInstance");
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
| **processInvocationRequest** | [**ProcessInvocationRequest**](ProcessInvocationRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTOProcessInvocationResponse**](ResponseDTOProcessInvocationResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getPageData

> PageString getPageData(processDefinitionId, version, state, searchText, createdFrom, createdTo, page, size, sort)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        String state = "state_example"; // String | 
        String searchText = "searchText_example"; // String | 
        OffsetDateTime createdFrom = OffsetDateTime.now(); // OffsetDateTime | Inclusive lower bound on createdAt (ISO-8601 offset date-time)
        OffsetDateTime createdTo = OffsetDateTime.now(); // OffsetDateTime | Exclusive upper bound on createdAt (ISO-8601 offset date-time)
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        String sort = "-createdAt"; // String | 
        try {
            PageString result = apiInstance.getPageData(processDefinitionId, version, state, searchText, createdFrom, createdTo, page, size, sort);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#getPageData");
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
| **processDefinitionId** | **String**|  | [optional] |
| **version** | **Integer**|  | [optional] |
| **state** | **String**|  | [optional] |
| **searchText** | **String**|  | [optional] |
| **createdFrom** | **OffsetDateTime**| Inclusive lower bound on createdAt (ISO-8601 offset date-time) | [optional] |
| **createdTo** | **OffsetDateTime**| Exclusive upper bound on createdAt (ISO-8601 offset date-time) | [optional] |
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sort** | **String**|  | [optional] [default to -createdAt] |

### Return type

[**PageString**](PageString.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getPageDataWithHttpInfo

> ApiResponse<PageString> getPageData getPageDataWithHttpInfo(processDefinitionId, version, state, searchText, createdFrom, createdTo, page, size, sort)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        String state = "state_example"; // String | 
        String searchText = "searchText_example"; // String | 
        OffsetDateTime createdFrom = OffsetDateTime.now(); // OffsetDateTime | Inclusive lower bound on createdAt (ISO-8601 offset date-time)
        OffsetDateTime createdTo = OffsetDateTime.now(); // OffsetDateTime | Exclusive upper bound on createdAt (ISO-8601 offset date-time)
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        String sort = "-createdAt"; // String | 
        try {
            ApiResponse<PageString> response = apiInstance.getPageDataWithHttpInfo(processDefinitionId, version, state, searchText, createdFrom, createdTo, page, size, sort);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#getPageData");
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
| **processDefinitionId** | **String**|  | [optional] |
| **version** | **Integer**|  | [optional] |
| **state** | **String**|  | [optional] |
| **searchText** | **String**|  | [optional] |
| **createdFrom** | **OffsetDateTime**| Inclusive lower bound on createdAt (ISO-8601 offset date-time) | [optional] |
| **createdTo** | **OffsetDateTime**| Exclusive upper bound on createdAt (ISO-8601 offset date-time) | [optional] |
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sort** | **String**|  | [optional] [default to -createdAt] |

### Return type

ApiResponse<[**PageString**](PageString.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getProcessInstance

> ResponseDTOProcessInstanceDTO getProcessInstance(processInstanceId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        String processInstanceId = "processInstanceId_example"; // String | 
        try {
            ResponseDTOProcessInstanceDTO result = apiInstance.getProcessInstance(processInstanceId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#getProcessInstance");
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

### Return type

[**ResponseDTOProcessInstanceDTO**](ResponseDTOProcessInstanceDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getProcessInstanceWithHttpInfo

> ApiResponse<ResponseDTOProcessInstanceDTO> getProcessInstance getProcessInstanceWithHttpInfo(processInstanceId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        String processInstanceId = "processInstanceId_example"; // String | 
        try {
            ApiResponse<ResponseDTOProcessInstanceDTO> response = apiInstance.getProcessInstanceWithHttpInfo(processInstanceId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#getProcessInstance");
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

### Return type

ApiResponse<[**ResponseDTOProcessInstanceDTO**](ResponseDTOProcessInstanceDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## modifyInstance

> String modifyInstance(updateInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        UpdateInstanceRequest updateInstanceRequest = new UpdateInstanceRequest(); // UpdateInstanceRequest | 
        try {
            String result = apiInstance.modifyInstance(updateInstanceRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#modifyInstance");
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
| **updateInstanceRequest** | [**UpdateInstanceRequest**](UpdateInstanceRequest.md)|  | |

### Return type

**String**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## modifyInstanceWithHttpInfo

> ApiResponse<String> modifyInstance modifyInstanceWithHttpInfo(updateInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        UpdateInstanceRequest updateInstanceRequest = new UpdateInstanceRequest(); // UpdateInstanceRequest | 
        try {
            ApiResponse<String> response = apiInstance.modifyInstanceWithHttpInfo(updateInstanceRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#modifyInstance");
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
| **updateInstanceRequest** | [**UpdateInstanceRequest**](UpdateInstanceRequest.md)|  | |

### Return type

ApiResponse<**String**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## resolveIncident

> String resolveIncident(batchInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        BatchInstanceRequest batchInstanceRequest = new BatchInstanceRequest(); // BatchInstanceRequest | 
        try {
            String result = apiInstance.resolveIncident(batchInstanceRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#resolveIncident");
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
| **batchInstanceRequest** | [**BatchInstanceRequest**](.md)|  | |

### Return type

**String**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## resolveIncidentWithHttpInfo

> ApiResponse<String> resolveIncident resolveIncidentWithHttpInfo(batchInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        BatchInstanceRequest batchInstanceRequest = new BatchInstanceRequest(); // BatchInstanceRequest | 
        try {
            ApiResponse<String> response = apiInstance.resolveIncidentWithHttpInfo(batchInstanceRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#resolveIncident");
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
| **batchInstanceRequest** | [**BatchInstanceRequest**](.md)|  | |

### Return type

ApiResponse<**String**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## retryInstance

> String retryInstance(retryProcessEvent)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        RetryProcessEvent retryProcessEvent = new RetryProcessEvent(); // RetryProcessEvent | 
        try {
            String result = apiInstance.retryInstance(retryProcessEvent);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#retryInstance");
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
| **retryProcessEvent** | [**RetryProcessEvent**](RetryProcessEvent.md)|  | |

### Return type

**String**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## retryInstanceWithHttpInfo

> ApiResponse<String> retryInstance retryInstanceWithHttpInfo(retryProcessEvent)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        RetryProcessEvent retryProcessEvent = new RetryProcessEvent(); // RetryProcessEvent | 
        try {
            ApiResponse<String> response = apiInstance.retryInstanceWithHttpInfo(retryProcessEvent);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#retryInstance");
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
| **retryProcessEvent** | [**RetryProcessEvent**](RetryProcessEvent.md)|  | |

### Return type

ApiResponse<**String**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## retryInstancesBatch

> String retryInstancesBatch(batchInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        BatchInstanceRequest batchInstanceRequest = new BatchInstanceRequest(); // BatchInstanceRequest | 
        try {
            String result = apiInstance.retryInstancesBatch(batchInstanceRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#retryInstancesBatch");
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
| **batchInstanceRequest** | [**BatchInstanceRequest**](BatchInstanceRequest.md)|  | |

### Return type

**String**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## retryInstancesBatchWithHttpInfo

> ApiResponse<String> retryInstancesBatch retryInstancesBatchWithHttpInfo(batchInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        BatchInstanceRequest batchInstanceRequest = new BatchInstanceRequest(); // BatchInstanceRequest | 
        try {
            ApiResponse<String> response = apiInstance.retryInstancesBatchWithHttpInfo(batchInstanceRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#retryInstancesBatch");
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
| **batchInstanceRequest** | [**BatchInstanceRequest**](BatchInstanceRequest.md)|  | |

### Return type

ApiResponse<**String**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## scrollProcessInstances

> ProcessInstanceScrollDTO scrollProcessInstances(to, processDefinitionId, version, state, searchText, createdFrom, createdTo, from, sort)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        Integer to = 56; // Integer | Exclusive end offset
        String processDefinitionId = "processDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        String state = "state_example"; // String | 
        String searchText = "searchText_example"; // String | 
        OffsetDateTime createdFrom = OffsetDateTime.now(); // OffsetDateTime | Inclusive lower bound on createdAt (ISO-8601 offset date-time)
        OffsetDateTime createdTo = OffsetDateTime.now(); // OffsetDateTime | Exclusive upper bound on createdAt (ISO-8601 offset date-time)
        Integer from = 0; // Integer | Inclusive 0-based offset
        String sort = "-createdAt"; // String | 
        try {
            ProcessInstanceScrollDTO result = apiInstance.scrollProcessInstances(to, processDefinitionId, version, state, searchText, createdFrom, createdTo, from, sort);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#scrollProcessInstances");
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
| **to** | **Integer**| Exclusive end offset | |
| **processDefinitionId** | **String**|  | [optional] |
| **version** | **Integer**|  | [optional] |
| **state** | **String**|  | [optional] |
| **searchText** | **String**|  | [optional] |
| **createdFrom** | **OffsetDateTime**| Inclusive lower bound on createdAt (ISO-8601 offset date-time) | [optional] |
| **createdTo** | **OffsetDateTime**| Exclusive upper bound on createdAt (ISO-8601 offset date-time) | [optional] |
| **from** | **Integer**| Inclusive 0-based offset | [optional] [default to 0] |
| **sort** | **String**|  | [optional] [default to -createdAt] |

### Return type

[**ProcessInstanceScrollDTO**](ProcessInstanceScrollDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## scrollProcessInstancesWithHttpInfo

> ApiResponse<ProcessInstanceScrollDTO> scrollProcessInstances scrollProcessInstancesWithHttpInfo(to, processDefinitionId, version, state, searchText, createdFrom, createdTo, from, sort)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        Integer to = 56; // Integer | Exclusive end offset
        String processDefinitionId = "processDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        String state = "state_example"; // String | 
        String searchText = "searchText_example"; // String | 
        OffsetDateTime createdFrom = OffsetDateTime.now(); // OffsetDateTime | Inclusive lower bound on createdAt (ISO-8601 offset date-time)
        OffsetDateTime createdTo = OffsetDateTime.now(); // OffsetDateTime | Exclusive upper bound on createdAt (ISO-8601 offset date-time)
        Integer from = 0; // Integer | Inclusive 0-based offset
        String sort = "-createdAt"; // String | 
        try {
            ApiResponse<ProcessInstanceScrollDTO> response = apiInstance.scrollProcessInstancesWithHttpInfo(to, processDefinitionId, version, state, searchText, createdFrom, createdTo, from, sort);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#scrollProcessInstances");
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
| **to** | **Integer**| Exclusive end offset | |
| **processDefinitionId** | **String**|  | [optional] |
| **version** | **Integer**|  | [optional] |
| **state** | **String**|  | [optional] |
| **searchText** | **String**|  | [optional] |
| **createdFrom** | **OffsetDateTime**| Inclusive lower bound on createdAt (ISO-8601 offset date-time) | [optional] |
| **createdTo** | **OffsetDateTime**| Exclusive upper bound on createdAt (ISO-8601 offset date-time) | [optional] |
| **from** | **Integer**| Inclusive 0-based offset | [optional] [default to 0] |
| **sort** | **String**|  | [optional] [default to -createdAt] |

### Return type

ApiResponse<[**ProcessInstanceScrollDTO**](ProcessInstanceScrollDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

