# DecisionDefinitionApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**deleteDecisionDefinition**](DecisionDefinitionApi.md#deleteDecisionDefinition) | **DELETE** /decisionDefinitions/{decisionDefinitionId}/{version} |  |
| [**deleteDecisionDefinitionWithHttpInfo**](DecisionDefinitionApi.md#deleteDecisionDefinitionWithHttpInfo) | **DELETE** /decisionDefinitions/{decisionDefinitionId}/{version} |  |
| [**deployResource1**](DecisionDefinitionApi.md#deployResource1) | **POST** /decisionDefinitions/upload |  |
| [**deployResource1WithHttpInfo**](DecisionDefinitionApi.md#deployResource1WithHttpInfo) | **POST** /decisionDefinitions/upload |  |
| [**evaluateDecision**](DecisionDefinitionApi.md#evaluateDecision) | **POST** /decisionDefinitions/evaluate |  |
| [**evaluateDecisionWithHttpInfo**](DecisionDefinitionApi.md#evaluateDecisionWithHttpInfo) | **POST** /decisionDefinitions/evaluate |  |
| [**getDecisionDefinition**](DecisionDefinitionApi.md#getDecisionDefinition) | **GET** /decisionDefinitions/{decisionDefinitionId}/{version} |  |
| [**getDecisionDefinitionWithHttpInfo**](DecisionDefinitionApi.md#getDecisionDefinitionWithHttpInfo) | **GET** /decisionDefinitions/{decisionDefinitionId}/{version} |  |
| [**getDecisionDefinitions**](DecisionDefinitionApi.md#getDecisionDefinitions) | **GET** /decisionDefinitions |  |
| [**getDecisionDefinitionsWithHttpInfo**](DecisionDefinitionApi.md#getDecisionDefinitionsWithHttpInfo) | **GET** /decisionDefinitions |  |
| [**listDecisionDefinitions**](DecisionDefinitionApi.md#listDecisionDefinitions) | **GET** /decisionDefinitions/ids |  |
| [**listDecisionDefinitionsWithHttpInfo**](DecisionDefinitionApi.md#listDecisionDefinitionsWithHttpInfo) | **GET** /decisionDefinitions/ids |  |



## deleteDecisionDefinition

> ResponseDTOVoid deleteDecisionDefinition(decisionDefinitionId, version)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        String decisionDefinitionId = "decisionDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        try {
            ResponseDTOVoid result = apiInstance.deleteDecisionDefinition(decisionDefinitionId, version);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#deleteDecisionDefinition");
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
| **decisionDefinitionId** | **String**|  | |
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

## deleteDecisionDefinitionWithHttpInfo

> ApiResponse<ResponseDTOVoid> deleteDecisionDefinition deleteDecisionDefinitionWithHttpInfo(decisionDefinitionId, version)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        String decisionDefinitionId = "decisionDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        try {
            ApiResponse<ResponseDTOVoid> response = apiInstance.deleteDecisionDefinitionWithHttpInfo(decisionDefinitionId, version);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#deleteDecisionDefinition");
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
| **decisionDefinitionId** | **String**|  | |
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


## deployResource1

> ResponseDTOResourceDeploymentResponse deployResource1(resourceDeploymentRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        ResourceDeploymentRequest resourceDeploymentRequest = new ResourceDeploymentRequest(); // ResourceDeploymentRequest | 
        try {
            ResponseDTOResourceDeploymentResponse result = apiInstance.deployResource1(resourceDeploymentRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#deployResource1");
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

## deployResource1WithHttpInfo

> ApiResponse<ResponseDTOResourceDeploymentResponse> deployResource1 deployResource1WithHttpInfo(resourceDeploymentRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        ResourceDeploymentRequest resourceDeploymentRequest = new ResourceDeploymentRequest(); // ResourceDeploymentRequest | 
        try {
            ApiResponse<ResponseDTOResourceDeploymentResponse> response = apiInstance.deployResource1WithHttpInfo(resourceDeploymentRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#deployResource1");
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


## evaluateDecision

> ResponseDTODecisionEvaluationResponseDTO evaluateDecision(evaluateDecisionRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        EvaluateDecisionRequest evaluateDecisionRequest = new EvaluateDecisionRequest(); // EvaluateDecisionRequest | 
        try {
            ResponseDTODecisionEvaluationResponseDTO result = apiInstance.evaluateDecision(evaluateDecisionRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#evaluateDecision");
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
| **evaluateDecisionRequest** | [**EvaluateDecisionRequest**](EvaluateDecisionRequest.md)|  | |

### Return type

[**ResponseDTODecisionEvaluationResponseDTO**](ResponseDTODecisionEvaluationResponseDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## evaluateDecisionWithHttpInfo

> ApiResponse<ResponseDTODecisionEvaluationResponseDTO> evaluateDecision evaluateDecisionWithHttpInfo(evaluateDecisionRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        EvaluateDecisionRequest evaluateDecisionRequest = new EvaluateDecisionRequest(); // EvaluateDecisionRequest | 
        try {
            ApiResponse<ResponseDTODecisionEvaluationResponseDTO> response = apiInstance.evaluateDecisionWithHttpInfo(evaluateDecisionRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#evaluateDecision");
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
| **evaluateDecisionRequest** | [**EvaluateDecisionRequest**](EvaluateDecisionRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTODecisionEvaluationResponseDTO**](ResponseDTODecisionEvaluationResponseDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getDecisionDefinition

> ResponseDTOResourceDefinitionDTO getDecisionDefinition(decisionDefinitionId, version)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        String decisionDefinitionId = "decisionDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        try {
            ResponseDTOResourceDefinitionDTO result = apiInstance.getDecisionDefinition(decisionDefinitionId, version);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#getDecisionDefinition");
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
| **decisionDefinitionId** | **String**|  | |
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

## getDecisionDefinitionWithHttpInfo

> ApiResponse<ResponseDTOResourceDefinitionDTO> getDecisionDefinition getDecisionDefinitionWithHttpInfo(decisionDefinitionId, version)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        String decisionDefinitionId = "decisionDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        try {
            ApiResponse<ResponseDTOResourceDefinitionDTO> response = apiInstance.getDecisionDefinitionWithHttpInfo(decisionDefinitionId, version);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#getDecisionDefinition");
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
| **decisionDefinitionId** | **String**|  | |
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


## getDecisionDefinitions

> PageResourceDefinitionDTO getDecisionDefinitions(page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            PageResourceDefinitionDTO result = apiInstance.getDecisionDefinitions(page, size);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#getDecisionDefinitions");
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

## getDecisionDefinitionsWithHttpInfo

> ApiResponse<PageResourceDefinitionDTO> getDecisionDefinitions getDecisionDefinitionsWithHttpInfo(page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            ApiResponse<PageResourceDefinitionDTO> response = apiInstance.getDecisionDefinitionsWithHttpInfo(page, size);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#getDecisionDefinitions");
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


## listDecisionDefinitions

> ResponseDTOListResourceDefinitionDTO listDecisionDefinitions()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        try {
            ResponseDTOListResourceDefinitionDTO result = apiInstance.listDecisionDefinitions();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#listDecisionDefinitions");
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

## listDecisionDefinitionsWithHttpInfo

> ApiResponse<ResponseDTOListResourceDefinitionDTO> listDecisionDefinitions listDecisionDefinitionsWithHttpInfo()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionDefinitionApi apiInstance = new DecisionDefinitionApi(defaultClient);
        try {
            ApiResponse<ResponseDTOListResourceDefinitionDTO> response = apiInstance.listDecisionDefinitionsWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionDefinitionApi#listDecisionDefinitions");
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

