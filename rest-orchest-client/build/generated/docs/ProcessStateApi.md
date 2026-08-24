# ProcessStateApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addProcessState**](ProcessStateApi.md#addProcessState) | **POST** /processStates |  |
| [**addProcessStateWithHttpInfo**](ProcessStateApi.md#addProcessStateWithHttpInfo) | **POST** /processStates |  |
| [**getProcessStateForProcessId**](ProcessStateApi.md#getProcessStateForProcessId) | **GET** /processStates/{processDefinitionId} |  |
| [**getProcessStateForProcessIdWithHttpInfo**](ProcessStateApi.md#getProcessStateForProcessIdWithHttpInfo) | **GET** /processStates/{processDefinitionId} |  |
| [**getProcessStates**](ProcessStateApi.md#getProcessStates) | **GET** /processStates |  |
| [**getProcessStatesWithHttpInfo**](ProcessStateApi.md#getProcessStatesWithHttpInfo) | **GET** /processStates |  |
| [**removeProcessState**](ProcessStateApi.md#removeProcessState) | **DELETE** /processStates/{processDefinitionId} |  |
| [**removeProcessStateWithHttpInfo**](ProcessStateApi.md#removeProcessStateWithHttpInfo) | **DELETE** /processStates/{processDefinitionId} |  |
| [**upsertProcessState**](ProcessStateApi.md#upsertProcessState) | **PUT** /processStates |  |
| [**upsertProcessStateWithHttpInfo**](ProcessStateApi.md#upsertProcessStateWithHttpInfo) | **PUT** /processStates |  |



## addProcessState

> ProcessState addProcessState(processState)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessStateApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessStateApi apiInstance = new ProcessStateApi(defaultClient);
        ProcessState processState = new ProcessState(); // ProcessState | 
        try {
            ProcessState result = apiInstance.addProcessState(processState);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessStateApi#addProcessState");
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
| **processState** | [**ProcessState**](ProcessState.md)|  | |

### Return type

[**ProcessState**](ProcessState.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## addProcessStateWithHttpInfo

> ApiResponse<ProcessState> addProcessState addProcessStateWithHttpInfo(processState)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessStateApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessStateApi apiInstance = new ProcessStateApi(defaultClient);
        ProcessState processState = new ProcessState(); // ProcessState | 
        try {
            ApiResponse<ProcessState> response = apiInstance.addProcessStateWithHttpInfo(processState);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessStateApi#addProcessState");
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
| **processState** | [**ProcessState**](ProcessState.md)|  | |

### Return type

ApiResponse<[**ProcessState**](ProcessState.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getProcessStateForProcessId

> ProcessState getProcessStateForProcessId(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessStateApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessStateApi apiInstance = new ProcessStateApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ProcessState result = apiInstance.getProcessStateForProcessId(processDefinitionId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessStateApi#getProcessStateForProcessId");
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
| **processDefinitionId** | **String**|  | |

### Return type

[**ProcessState**](ProcessState.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getProcessStateForProcessIdWithHttpInfo

> ApiResponse<ProcessState> getProcessStateForProcessId getProcessStateForProcessIdWithHttpInfo(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessStateApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessStateApi apiInstance = new ProcessStateApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ApiResponse<ProcessState> response = apiInstance.getProcessStateForProcessIdWithHttpInfo(processDefinitionId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessStateApi#getProcessStateForProcessId");
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
| **processDefinitionId** | **String**|  | |

### Return type

ApiResponse<[**ProcessState**](ProcessState.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getProcessStates

> List<ProcessState> getProcessStates()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessStateApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessStateApi apiInstance = new ProcessStateApi(defaultClient);
        try {
            List<ProcessState> result = apiInstance.getProcessStates();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessStateApi#getProcessStates");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**List&lt;ProcessState&gt;**](ProcessState.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getProcessStatesWithHttpInfo

> ApiResponse<List<ProcessState>> getProcessStates getProcessStatesWithHttpInfo()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessStateApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessStateApi apiInstance = new ProcessStateApi(defaultClient);
        try {
            ApiResponse<List<ProcessState>> response = apiInstance.getProcessStatesWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessStateApi#getProcessStates");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

ApiResponse<[**List&lt;ProcessState&gt;**](ProcessState.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## removeProcessState

> ProcessState removeProcessState(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessStateApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessStateApi apiInstance = new ProcessStateApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ProcessState result = apiInstance.removeProcessState(processDefinitionId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessStateApi#removeProcessState");
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
| **processDefinitionId** | **String**|  | |

### Return type

[**ProcessState**](ProcessState.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## removeProcessStateWithHttpInfo

> ApiResponse<ProcessState> removeProcessState removeProcessStateWithHttpInfo(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessStateApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessStateApi apiInstance = new ProcessStateApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ApiResponse<ProcessState> response = apiInstance.removeProcessStateWithHttpInfo(processDefinitionId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessStateApi#removeProcessState");
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
| **processDefinitionId** | **String**|  | |

### Return type

ApiResponse<[**ProcessState**](ProcessState.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## upsertProcessState

> ProcessState upsertProcessState(processState)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessStateApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessStateApi apiInstance = new ProcessStateApi(defaultClient);
        ProcessState processState = new ProcessState(); // ProcessState | 
        try {
            ProcessState result = apiInstance.upsertProcessState(processState);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessStateApi#upsertProcessState");
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
| **processState** | [**ProcessState**](ProcessState.md)|  | |

### Return type

[**ProcessState**](ProcessState.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## upsertProcessStateWithHttpInfo

> ApiResponse<ProcessState> upsertProcessState upsertProcessStateWithHttpInfo(processState)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessStateApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessStateApi apiInstance = new ProcessStateApi(defaultClient);
        ProcessState processState = new ProcessState(); // ProcessState | 
        try {
            ApiResponse<ProcessState> response = apiInstance.upsertProcessStateWithHttpInfo(processState);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessStateApi#upsertProcessState");
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
| **processState** | [**ProcessState**](ProcessState.md)|  | |

### Return type

ApiResponse<[**ProcessState**](ProcessState.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

