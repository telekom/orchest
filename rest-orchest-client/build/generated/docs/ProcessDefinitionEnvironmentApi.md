# ProcessDefinitionEnvironmentApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addProcessEnvVariable**](ProcessDefinitionEnvironmentApi.md#addProcessEnvVariable) | **POST** /processDefinitionsEnvs |  |
| [**addProcessEnvVariableWithHttpInfo**](ProcessDefinitionEnvironmentApi.md#addProcessEnvVariableWithHttpInfo) | **POST** /processDefinitionsEnvs |  |
| [**deleteProcessEnvVariable**](ProcessDefinitionEnvironmentApi.md#deleteProcessEnvVariable) | **DELETE** /processDefinitionsEnvs |  |
| [**deleteProcessEnvVariableWithHttpInfo**](ProcessDefinitionEnvironmentApi.md#deleteProcessEnvVariableWithHttpInfo) | **DELETE** /processDefinitionsEnvs |  |
| [**getProcessDefinition**](ProcessDefinitionEnvironmentApi.md#getProcessDefinition) | **GET** /processDefinitionsEnvs/{processDefinitionId} |  |
| [**getProcessDefinitionWithHttpInfo**](ProcessDefinitionEnvironmentApi.md#getProcessDefinitionWithHttpInfo) | **GET** /processDefinitionsEnvs/{processDefinitionId} |  |
| [**getProcessDefinitions**](ProcessDefinitionEnvironmentApi.md#getProcessDefinitions) | **GET** /processDefinitionsEnvs |  |
| [**getProcessDefinitionsWithHttpInfo**](ProcessDefinitionEnvironmentApi.md#getProcessDefinitionsWithHttpInfo) | **GET** /processDefinitionsEnvs |  |
| [**updateProcessEnvVariable**](ProcessDefinitionEnvironmentApi.md#updateProcessEnvVariable) | **PATCH** /processDefinitionsEnvs |  |
| [**updateProcessEnvVariableWithHttpInfo**](ProcessDefinitionEnvironmentApi.md#updateProcessEnvVariableWithHttpInfo) | **PATCH** /processDefinitionsEnvs |  |



## addProcessEnvVariable

> ResponseDTOProcessEnvVariables addProcessEnvVariable(processEnvVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionEnvironmentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionEnvironmentApi apiInstance = new ProcessDefinitionEnvironmentApi(defaultClient);
        ProcessEnvVariables processEnvVariables = new ProcessEnvVariables(); // ProcessEnvVariables | 
        try {
            ResponseDTOProcessEnvVariables result = apiInstance.addProcessEnvVariable(processEnvVariables);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionEnvironmentApi#addProcessEnvVariable");
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
| **processEnvVariables** | [**ProcessEnvVariables**](ProcessEnvVariables.md)|  | |

### Return type

[**ResponseDTOProcessEnvVariables**](ResponseDTOProcessEnvVariables.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## addProcessEnvVariableWithHttpInfo

> ApiResponse<ResponseDTOProcessEnvVariables> addProcessEnvVariable addProcessEnvVariableWithHttpInfo(processEnvVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionEnvironmentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionEnvironmentApi apiInstance = new ProcessDefinitionEnvironmentApi(defaultClient);
        ProcessEnvVariables processEnvVariables = new ProcessEnvVariables(); // ProcessEnvVariables | 
        try {
            ApiResponse<ResponseDTOProcessEnvVariables> response = apiInstance.addProcessEnvVariableWithHttpInfo(processEnvVariables);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionEnvironmentApi#addProcessEnvVariable");
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
| **processEnvVariables** | [**ProcessEnvVariables**](ProcessEnvVariables.md)|  | |

### Return type

ApiResponse<[**ResponseDTOProcessEnvVariables**](ResponseDTOProcessEnvVariables.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## deleteProcessEnvVariable

> ResponseDTOProcessEnvVariables deleteProcessEnvVariable(processEnvVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionEnvironmentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionEnvironmentApi apiInstance = new ProcessDefinitionEnvironmentApi(defaultClient);
        ProcessEnvVariables processEnvVariables = new ProcessEnvVariables(); // ProcessEnvVariables | 
        try {
            ResponseDTOProcessEnvVariables result = apiInstance.deleteProcessEnvVariable(processEnvVariables);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionEnvironmentApi#deleteProcessEnvVariable");
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
| **processEnvVariables** | [**ProcessEnvVariables**](ProcessEnvVariables.md)|  | |

### Return type

[**ResponseDTOProcessEnvVariables**](ResponseDTOProcessEnvVariables.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## deleteProcessEnvVariableWithHttpInfo

> ApiResponse<ResponseDTOProcessEnvVariables> deleteProcessEnvVariable deleteProcessEnvVariableWithHttpInfo(processEnvVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionEnvironmentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionEnvironmentApi apiInstance = new ProcessDefinitionEnvironmentApi(defaultClient);
        ProcessEnvVariables processEnvVariables = new ProcessEnvVariables(); // ProcessEnvVariables | 
        try {
            ApiResponse<ResponseDTOProcessEnvVariables> response = apiInstance.deleteProcessEnvVariableWithHttpInfo(processEnvVariables);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionEnvironmentApi#deleteProcessEnvVariable");
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
| **processEnvVariables** | [**ProcessEnvVariables**](ProcessEnvVariables.md)|  | |

### Return type

ApiResponse<[**ResponseDTOProcessEnvVariables**](ResponseDTOProcessEnvVariables.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getProcessDefinition

> ResponseDTOListProcessEnvVariables getProcessDefinition(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionEnvironmentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionEnvironmentApi apiInstance = new ProcessDefinitionEnvironmentApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ResponseDTOListProcessEnvVariables result = apiInstance.getProcessDefinition(processDefinitionId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionEnvironmentApi#getProcessDefinition");
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

[**ResponseDTOListProcessEnvVariables**](ResponseDTOListProcessEnvVariables.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getProcessDefinitionWithHttpInfo

> ApiResponse<ResponseDTOListProcessEnvVariables> getProcessDefinition getProcessDefinitionWithHttpInfo(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionEnvironmentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionEnvironmentApi apiInstance = new ProcessDefinitionEnvironmentApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ApiResponse<ResponseDTOListProcessEnvVariables> response = apiInstance.getProcessDefinitionWithHttpInfo(processDefinitionId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionEnvironmentApi#getProcessDefinition");
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

ApiResponse<[**ResponseDTOListProcessEnvVariables**](ResponseDTOListProcessEnvVariables.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getProcessDefinitions

> PageProcessEnvVariables getProcessDefinitions(page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionEnvironmentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionEnvironmentApi apiInstance = new ProcessDefinitionEnvironmentApi(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            PageProcessEnvVariables result = apiInstance.getProcessDefinitions(page, size);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionEnvironmentApi#getProcessDefinitions");
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

### Return type

[**PageProcessEnvVariables**](PageProcessEnvVariables.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getProcessDefinitionsWithHttpInfo

> ApiResponse<PageProcessEnvVariables> getProcessDefinitions getProcessDefinitionsWithHttpInfo(page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionEnvironmentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionEnvironmentApi apiInstance = new ProcessDefinitionEnvironmentApi(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            ApiResponse<PageProcessEnvVariables> response = apiInstance.getProcessDefinitionsWithHttpInfo(page, size);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionEnvironmentApi#getProcessDefinitions");
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

### Return type

ApiResponse<[**PageProcessEnvVariables**](PageProcessEnvVariables.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## updateProcessEnvVariable

> ResponseDTOProcessEnvVariables updateProcessEnvVariable(processEnvVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionEnvironmentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionEnvironmentApi apiInstance = new ProcessDefinitionEnvironmentApi(defaultClient);
        ProcessEnvVariables processEnvVariables = new ProcessEnvVariables(); // ProcessEnvVariables | 
        try {
            ResponseDTOProcessEnvVariables result = apiInstance.updateProcessEnvVariable(processEnvVariables);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionEnvironmentApi#updateProcessEnvVariable");
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
| **processEnvVariables** | [**ProcessEnvVariables**](ProcessEnvVariables.md)|  | |

### Return type

[**ResponseDTOProcessEnvVariables**](ResponseDTOProcessEnvVariables.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## updateProcessEnvVariableWithHttpInfo

> ApiResponse<ResponseDTOProcessEnvVariables> updateProcessEnvVariable updateProcessEnvVariableWithHttpInfo(processEnvVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionEnvironmentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionEnvironmentApi apiInstance = new ProcessDefinitionEnvironmentApi(defaultClient);
        ProcessEnvVariables processEnvVariables = new ProcessEnvVariables(); // ProcessEnvVariables | 
        try {
            ApiResponse<ResponseDTOProcessEnvVariables> response = apiInstance.updateProcessEnvVariableWithHttpInfo(processEnvVariables);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionEnvironmentApi#updateProcessEnvVariable");
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
| **processEnvVariables** | [**ProcessEnvVariables**](ProcessEnvVariables.md)|  | |

### Return type

ApiResponse<[**ResponseDTOProcessEnvVariables**](ResponseDTOProcessEnvVariables.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

