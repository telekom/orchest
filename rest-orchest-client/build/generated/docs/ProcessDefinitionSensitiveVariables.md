# ProcessDefinitionSensitiveVariables

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addProcessSensitiveVariables**](ProcessDefinitionSensitiveVariables.md#addProcessSensitiveVariables) | **POST** /processDefinitionsSensitiveVariables |  |
| [**addProcessSensitiveVariablesWithHttpInfo**](ProcessDefinitionSensitiveVariables.md#addProcessSensitiveVariablesWithHttpInfo) | **POST** /processDefinitionsSensitiveVariables |  |
| [**deleteProcessSensitiveVariables**](ProcessDefinitionSensitiveVariables.md#deleteProcessSensitiveVariables) | **DELETE** /processDefinitionsSensitiveVariables |  |
| [**deleteProcessSensitiveVariablesWithHttpInfo**](ProcessDefinitionSensitiveVariables.md#deleteProcessSensitiveVariablesWithHttpInfo) | **DELETE** /processDefinitionsSensitiveVariables |  |
| [**getAll**](ProcessDefinitionSensitiveVariables.md#getAll) | **GET** /processDefinitionsSensitiveVariables |  |
| [**getAllWithHttpInfo**](ProcessDefinitionSensitiveVariables.md#getAllWithHttpInfo) | **GET** /processDefinitionsSensitiveVariables |  |
| [**getProcessSensitiveVariables**](ProcessDefinitionSensitiveVariables.md#getProcessSensitiveVariables) | **GET** /processDefinitionsSensitiveVariables/{processDefinitionId} |  |
| [**getProcessSensitiveVariablesWithHttpInfo**](ProcessDefinitionSensitiveVariables.md#getProcessSensitiveVariablesWithHttpInfo) | **GET** /processDefinitionsSensitiveVariables/{processDefinitionId} |  |
| [**updateProcessSensitiveVariables**](ProcessDefinitionSensitiveVariables.md#updateProcessSensitiveVariables) | **PATCH** /processDefinitionsSensitiveVariables |  |
| [**updateProcessSensitiveVariablesWithHttpInfo**](ProcessDefinitionSensitiveVariables.md#updateProcessSensitiveVariablesWithHttpInfo) | **PATCH** /processDefinitionsSensitiveVariables |  |



## addProcessSensitiveVariables

> ResponseDTOProcessSensitiveVariables addProcessSensitiveVariables(processSensitiveVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionSensitiveVariables;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionSensitiveVariables apiInstance = new ProcessDefinitionSensitiveVariables(defaultClient);
        ProcessSensitiveVariables processSensitiveVariables = new ProcessSensitiveVariables(); // ProcessSensitiveVariables | 
        try {
            ResponseDTOProcessSensitiveVariables result = apiInstance.addProcessSensitiveVariables(processSensitiveVariables);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionSensitiveVariables#addProcessSensitiveVariables");
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
| **processSensitiveVariables** | [**ProcessSensitiveVariables**](ProcessSensitiveVariables.md)|  | |

### Return type

[**ResponseDTOProcessSensitiveVariables**](ResponseDTOProcessSensitiveVariables.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## addProcessSensitiveVariablesWithHttpInfo

> ApiResponse<ResponseDTOProcessSensitiveVariables> addProcessSensitiveVariables addProcessSensitiveVariablesWithHttpInfo(processSensitiveVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionSensitiveVariables;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionSensitiveVariables apiInstance = new ProcessDefinitionSensitiveVariables(defaultClient);
        ProcessSensitiveVariables processSensitiveVariables = new ProcessSensitiveVariables(); // ProcessSensitiveVariables | 
        try {
            ApiResponse<ResponseDTOProcessSensitiveVariables> response = apiInstance.addProcessSensitiveVariablesWithHttpInfo(processSensitiveVariables);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionSensitiveVariables#addProcessSensitiveVariables");
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
| **processSensitiveVariables** | [**ProcessSensitiveVariables**](ProcessSensitiveVariables.md)|  | |

### Return type

ApiResponse<[**ResponseDTOProcessSensitiveVariables**](ResponseDTOProcessSensitiveVariables.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## deleteProcessSensitiveVariables

> ResponseDTOString deleteProcessSensitiveVariables(processSensitiveVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionSensitiveVariables;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionSensitiveVariables apiInstance = new ProcessDefinitionSensitiveVariables(defaultClient);
        ProcessSensitiveVariables processSensitiveVariables = new ProcessSensitiveVariables(); // ProcessSensitiveVariables | 
        try {
            ResponseDTOString result = apiInstance.deleteProcessSensitiveVariables(processSensitiveVariables);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionSensitiveVariables#deleteProcessSensitiveVariables");
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
| **processSensitiveVariables** | [**ProcessSensitiveVariables**](ProcessSensitiveVariables.md)|  | |

### Return type

[**ResponseDTOString**](ResponseDTOString.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## deleteProcessSensitiveVariablesWithHttpInfo

> ApiResponse<ResponseDTOString> deleteProcessSensitiveVariables deleteProcessSensitiveVariablesWithHttpInfo(processSensitiveVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionSensitiveVariables;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionSensitiveVariables apiInstance = new ProcessDefinitionSensitiveVariables(defaultClient);
        ProcessSensitiveVariables processSensitiveVariables = new ProcessSensitiveVariables(); // ProcessSensitiveVariables | 
        try {
            ApiResponse<ResponseDTOString> response = apiInstance.deleteProcessSensitiveVariablesWithHttpInfo(processSensitiveVariables);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionSensitiveVariables#deleteProcessSensitiveVariables");
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
| **processSensitiveVariables** | [**ProcessSensitiveVariables**](ProcessSensitiveVariables.md)|  | |

### Return type

ApiResponse<[**ResponseDTOString**](ResponseDTOString.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getAll

> PageProcessSensitiveVariables getAll(page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionSensitiveVariables;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionSensitiveVariables apiInstance = new ProcessDefinitionSensitiveVariables(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            PageProcessSensitiveVariables result = apiInstance.getAll(page, size);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionSensitiveVariables#getAll");
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

[**PageProcessSensitiveVariables**](PageProcessSensitiveVariables.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getAllWithHttpInfo

> ApiResponse<PageProcessSensitiveVariables> getAll getAllWithHttpInfo(page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionSensitiveVariables;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionSensitiveVariables apiInstance = new ProcessDefinitionSensitiveVariables(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            ApiResponse<PageProcessSensitiveVariables> response = apiInstance.getAllWithHttpInfo(page, size);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionSensitiveVariables#getAll");
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

ApiResponse<[**PageProcessSensitiveVariables**](PageProcessSensitiveVariables.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getProcessSensitiveVariables

> ResponseDTOListProcessSensitiveVariables getProcessSensitiveVariables(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionSensitiveVariables;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionSensitiveVariables apiInstance = new ProcessDefinitionSensitiveVariables(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ResponseDTOListProcessSensitiveVariables result = apiInstance.getProcessSensitiveVariables(processDefinitionId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionSensitiveVariables#getProcessSensitiveVariables");
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

[**ResponseDTOListProcessSensitiveVariables**](ResponseDTOListProcessSensitiveVariables.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getProcessSensitiveVariablesWithHttpInfo

> ApiResponse<ResponseDTOListProcessSensitiveVariables> getProcessSensitiveVariables getProcessSensitiveVariablesWithHttpInfo(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionSensitiveVariables;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionSensitiveVariables apiInstance = new ProcessDefinitionSensitiveVariables(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ApiResponse<ResponseDTOListProcessSensitiveVariables> response = apiInstance.getProcessSensitiveVariablesWithHttpInfo(processDefinitionId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionSensitiveVariables#getProcessSensitiveVariables");
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

ApiResponse<[**ResponseDTOListProcessSensitiveVariables**](ResponseDTOListProcessSensitiveVariables.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## updateProcessSensitiveVariables

> ResponseDTOProcessSensitiveVariables updateProcessSensitiveVariables(processSensitiveVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionSensitiveVariables;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionSensitiveVariables apiInstance = new ProcessDefinitionSensitiveVariables(defaultClient);
        ProcessSensitiveVariables processSensitiveVariables = new ProcessSensitiveVariables(); // ProcessSensitiveVariables | 
        try {
            ResponseDTOProcessSensitiveVariables result = apiInstance.updateProcessSensitiveVariables(processSensitiveVariables);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionSensitiveVariables#updateProcessSensitiveVariables");
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
| **processSensitiveVariables** | [**ProcessSensitiveVariables**](ProcessSensitiveVariables.md)|  | |

### Return type

[**ResponseDTOProcessSensitiveVariables**](ResponseDTOProcessSensitiveVariables.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## updateProcessSensitiveVariablesWithHttpInfo

> ApiResponse<ResponseDTOProcessSensitiveVariables> updateProcessSensitiveVariables updateProcessSensitiveVariablesWithHttpInfo(processSensitiveVariables)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionSensitiveVariables;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionSensitiveVariables apiInstance = new ProcessDefinitionSensitiveVariables(defaultClient);
        ProcessSensitiveVariables processSensitiveVariables = new ProcessSensitiveVariables(); // ProcessSensitiveVariables | 
        try {
            ApiResponse<ResponseDTOProcessSensitiveVariables> response = apiInstance.updateProcessSensitiveVariablesWithHttpInfo(processSensitiveVariables);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionSensitiveVariables#updateProcessSensitiveVariables");
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
| **processSensitiveVariables** | [**ProcessSensitiveVariables**](ProcessSensitiveVariables.md)|  | |

### Return type

ApiResponse<[**ResponseDTOProcessSensitiveVariables**](ResponseDTOProcessSensitiveVariables.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

