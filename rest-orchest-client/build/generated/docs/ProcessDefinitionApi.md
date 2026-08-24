# ProcessDefinitionApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**deleteProcessDefinition**](ProcessDefinitionApi.md#deleteProcessDefinition) | **DELETE** /processDefinitions/{processDefinitionId}/{version} |  |
| [**deleteProcessDefinitionWithHttpInfo**](ProcessDefinitionApi.md#deleteProcessDefinitionWithHttpInfo) | **DELETE** /processDefinitions/{processDefinitionId}/{version} |  |
| [**deployResource**](ProcessDefinitionApi.md#deployResource) | **POST** /processDefinitions/upload |  |
| [**deployResourceWithHttpInfo**](ProcessDefinitionApi.md#deployResourceWithHttpInfo) | **POST** /processDefinitions/upload |  |
| [**getProcessDefinition1**](ProcessDefinitionApi.md#getProcessDefinition1) | **GET** /processDefinitions/{processDefinitionId}/{version} |  |
| [**getProcessDefinition1WithHttpInfo**](ProcessDefinitionApi.md#getProcessDefinition1WithHttpInfo) | **GET** /processDefinitions/{processDefinitionId}/{version} |  |
| [**getProcessDefinitions1**](ProcessDefinitionApi.md#getProcessDefinitions1) | **GET** /processDefinitions |  |
| [**getProcessDefinitions1WithHttpInfo**](ProcessDefinitionApi.md#getProcessDefinitions1WithHttpInfo) | **GET** /processDefinitions |  |
| [**listProcessDefinitionsIds**](ProcessDefinitionApi.md#listProcessDefinitionsIds) | **GET** /processDefinitions/ids |  |
| [**listProcessDefinitionsIdsWithHttpInfo**](ProcessDefinitionApi.md#listProcessDefinitionsIdsWithHttpInfo) | **GET** /processDefinitions/ids |  |



## deleteProcessDefinition

> ResponseDTOVoid deleteProcessDefinition(processDefinitionId, version)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        try {
            ResponseDTOVoid result = apiInstance.deleteProcessDefinition(processDefinitionId, version);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#deleteProcessDefinition");
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
| **version** | **Integer**|  | |

### Return type

[**ResponseDTOVoid**](ResponseDTOVoid.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## deleteProcessDefinitionWithHttpInfo

> ApiResponse<ResponseDTOVoid> deleteProcessDefinition deleteProcessDefinitionWithHttpInfo(processDefinitionId, version)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        try {
            ApiResponse<ResponseDTOVoid> response = apiInstance.deleteProcessDefinitionWithHttpInfo(processDefinitionId, version);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#deleteProcessDefinition");
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
| **version** | **Integer**|  | |

### Return type

ApiResponse<[**ResponseDTOVoid**](ResponseDTOVoid.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## deployResource

> ResponseDTOResourceDeploymentResponse deployResource(resourceDeploymentRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        ResourceDeploymentRequest resourceDeploymentRequest = new ResourceDeploymentRequest(); // ResourceDeploymentRequest | 
        try {
            ResponseDTOResourceDeploymentResponse result = apiInstance.deployResource(resourceDeploymentRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#deployResource");
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
| **resourceDeploymentRequest** | [**ResourceDeploymentRequest**](ResourceDeploymentRequest.md)|  | |

### Return type

[**ResponseDTOResourceDeploymentResponse**](ResponseDTOResourceDeploymentResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## deployResourceWithHttpInfo

> ApiResponse<ResponseDTOResourceDeploymentResponse> deployResource deployResourceWithHttpInfo(resourceDeploymentRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        ResourceDeploymentRequest resourceDeploymentRequest = new ResourceDeploymentRequest(); // ResourceDeploymentRequest | 
        try {
            ApiResponse<ResponseDTOResourceDeploymentResponse> response = apiInstance.deployResourceWithHttpInfo(resourceDeploymentRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#deployResource");
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
| **resourceDeploymentRequest** | [**ResourceDeploymentRequest**](ResourceDeploymentRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTOResourceDeploymentResponse**](ResponseDTOResourceDeploymentResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getProcessDefinition1

> ResponseDTOResourceDefinitionDTO getProcessDefinition1(processDefinitionId, version)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        try {
            ResponseDTOResourceDefinitionDTO result = apiInstance.getProcessDefinition1(processDefinitionId, version);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#getProcessDefinition1");
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
| **version** | **Integer**|  | |

### Return type

[**ResponseDTOResourceDefinitionDTO**](ResponseDTOResourceDefinitionDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getProcessDefinition1WithHttpInfo

> ApiResponse<ResponseDTOResourceDefinitionDTO> getProcessDefinition1 getProcessDefinition1WithHttpInfo(processDefinitionId, version)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        try {
            ApiResponse<ResponseDTOResourceDefinitionDTO> response = apiInstance.getProcessDefinition1WithHttpInfo(processDefinitionId, version);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#getProcessDefinition1");
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
| **version** | **Integer**|  | |

### Return type

ApiResponse<[**ResponseDTOResourceDefinitionDTO**](ResponseDTOResourceDefinitionDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getProcessDefinitions1

> PageResourceDefinitionDTO getProcessDefinitions1(page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            PageResourceDefinitionDTO result = apiInstance.getProcessDefinitions1(page, size);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#getProcessDefinitions1");
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

[**PageResourceDefinitionDTO**](PageResourceDefinitionDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getProcessDefinitions1WithHttpInfo

> ApiResponse<PageResourceDefinitionDTO> getProcessDefinitions1 getProcessDefinitions1WithHttpInfo(page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            ApiResponse<PageResourceDefinitionDTO> response = apiInstance.getProcessDefinitions1WithHttpInfo(page, size);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#getProcessDefinitions1");
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

ApiResponse<[**PageResourceDefinitionDTO**](PageResourceDefinitionDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## listProcessDefinitionsIds

> ResponseDTOListResourceDefinitionDTO listProcessDefinitionsIds()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        try {
            ResponseDTOListResourceDefinitionDTO result = apiInstance.listProcessDefinitionsIds();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#listProcessDefinitionsIds");
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

[**ResponseDTOListResourceDefinitionDTO**](ResponseDTOListResourceDefinitionDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## listProcessDefinitionsIdsWithHttpInfo

> ApiResponse<ResponseDTOListResourceDefinitionDTO> listProcessDefinitionsIds listProcessDefinitionsIdsWithHttpInfo()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        try {
            ApiResponse<ResponseDTOListResourceDefinitionDTO> response = apiInstance.listProcessDefinitionsIdsWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#listProcessDefinitionsIds");
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

ApiResponse<[**ResponseDTOListResourceDefinitionDTO**](ResponseDTOListResourceDefinitionDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

