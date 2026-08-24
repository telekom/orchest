# DecisionInstanceApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getDecisionInstance**](DecisionInstanceApi.md#getDecisionInstance) | **GET** /decisionInstances/{decisionInstanceId} |  |
| [**getDecisionInstanceWithHttpInfo**](DecisionInstanceApi.md#getDecisionInstanceWithHttpInfo) | **GET** /decisionInstances/{decisionInstanceId} |  |
| [**getDecisionInstances**](DecisionInstanceApi.md#getDecisionInstances) | **GET** /decisionInstances/pageData |  |
| [**getDecisionInstancesWithHttpInfo**](DecisionInstanceApi.md#getDecisionInstancesWithHttpInfo) | **GET** /decisionInstances/pageData |  |
| [**scrollDecisionInstances**](DecisionInstanceApi.md#scrollDecisionInstances) | **GET** /decisionInstances/scroll |  |
| [**scrollDecisionInstancesWithHttpInfo**](DecisionInstanceApi.md#scrollDecisionInstancesWithHttpInfo) | **GET** /decisionInstances/scroll |  |



## getDecisionInstance

> ResponseDTODecisionInstanceDTO getDecisionInstance(decisionInstanceId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionInstanceApi apiInstance = new DecisionInstanceApi(defaultClient);
        String decisionInstanceId = "decisionInstanceId_example"; // String | 
        try {
            ResponseDTODecisionInstanceDTO result = apiInstance.getDecisionInstance(decisionInstanceId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionInstanceApi#getDecisionInstance");
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
| **decisionInstanceId** | **String**|  | |

### Return type

[**ResponseDTODecisionInstanceDTO**](ResponseDTODecisionInstanceDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getDecisionInstanceWithHttpInfo

> ApiResponse<ResponseDTODecisionInstanceDTO> getDecisionInstance getDecisionInstanceWithHttpInfo(decisionInstanceId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionInstanceApi apiInstance = new DecisionInstanceApi(defaultClient);
        String decisionInstanceId = "decisionInstanceId_example"; // String | 
        try {
            ApiResponse<ResponseDTODecisionInstanceDTO> response = apiInstance.getDecisionInstanceWithHttpInfo(decisionInstanceId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionInstanceApi#getDecisionInstance");
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
| **decisionInstanceId** | **String**|  | |

### Return type

ApiResponse<[**ResponseDTODecisionInstanceDTO**](ResponseDTODecisionInstanceDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getDecisionInstances

> PageDecisionInstanceDTO getDecisionInstances(decisionId, from, to, version, searchText, page, size, sort)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionInstanceApi apiInstance = new DecisionInstanceApi(defaultClient);
        String decisionId = "decisionId_example"; // String | 
        OffsetDateTime from = OffsetDateTime.now(); // OffsetDateTime | 
        OffsetDateTime to = OffsetDateTime.now(); // OffsetDateTime | 
        Integer version = 56; // Integer | 
        String searchText = "searchText_example"; // String | 
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        String sort = "-executedAt"; // String | 
        try {
            PageDecisionInstanceDTO result = apiInstance.getDecisionInstances(decisionId, from, to, version, searchText, page, size, sort);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionInstanceApi#getDecisionInstances");
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
| **decisionId** | **String**|  | [optional] |
| **from** | **OffsetDateTime**|  | [optional] |
| **to** | **OffsetDateTime**|  | [optional] |
| **version** | **Integer**|  | [optional] |
| **searchText** | **String**|  | [optional] |
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sort** | **String**|  | [optional] [default to -executedAt] |

### Return type

[**PageDecisionInstanceDTO**](PageDecisionInstanceDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getDecisionInstancesWithHttpInfo

> ApiResponse<PageDecisionInstanceDTO> getDecisionInstances getDecisionInstancesWithHttpInfo(decisionId, from, to, version, searchText, page, size, sort)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionInstanceApi apiInstance = new DecisionInstanceApi(defaultClient);
        String decisionId = "decisionId_example"; // String | 
        OffsetDateTime from = OffsetDateTime.now(); // OffsetDateTime | 
        OffsetDateTime to = OffsetDateTime.now(); // OffsetDateTime | 
        Integer version = 56; // Integer | 
        String searchText = "searchText_example"; // String | 
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        String sort = "-executedAt"; // String | 
        try {
            ApiResponse<PageDecisionInstanceDTO> response = apiInstance.getDecisionInstancesWithHttpInfo(decisionId, from, to, version, searchText, page, size, sort);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionInstanceApi#getDecisionInstances");
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
| **decisionId** | **String**|  | [optional] |
| **from** | **OffsetDateTime**|  | [optional] |
| **to** | **OffsetDateTime**|  | [optional] |
| **version** | **Integer**|  | [optional] |
| **searchText** | **String**|  | [optional] |
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sort** | **String**|  | [optional] [default to -executedAt] |

### Return type

ApiResponse<[**PageDecisionInstanceDTO**](PageDecisionInstanceDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## scrollDecisionInstances

> DecisionInstanceScrollDTO scrollDecisionInstances(to, decisionId, version, searchText, executedFrom, executedTo, from, sort)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionInstanceApi apiInstance = new DecisionInstanceApi(defaultClient);
        Integer to = 56; // Integer | Exclusive end offset
        String decisionId = "decisionId_example"; // String | 
        Integer version = 56; // Integer | 
        String searchText = "searchText_example"; // String | 
        OffsetDateTime executedFrom = OffsetDateTime.now(); // OffsetDateTime | 
        OffsetDateTime executedTo = OffsetDateTime.now(); // OffsetDateTime | 
        Integer from = 0; // Integer | Inclusive 0-based offset
        String sort = "-executedAt"; // String | 
        try {
            DecisionInstanceScrollDTO result = apiInstance.scrollDecisionInstances(to, decisionId, version, searchText, executedFrom, executedTo, from, sort);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionInstanceApi#scrollDecisionInstances");
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
| **decisionId** | **String**|  | [optional] |
| **version** | **Integer**|  | [optional] |
| **searchText** | **String**|  | [optional] |
| **executedFrom** | **OffsetDateTime**|  | [optional] |
| **executedTo** | **OffsetDateTime**|  | [optional] |
| **from** | **Integer**| Inclusive 0-based offset | [optional] [default to 0] |
| **sort** | **String**|  | [optional] [default to -executedAt] |

### Return type

[**DecisionInstanceScrollDTO**](DecisionInstanceScrollDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## scrollDecisionInstancesWithHttpInfo

> ApiResponse<DecisionInstanceScrollDTO> scrollDecisionInstances scrollDecisionInstancesWithHttpInfo(to, decisionId, version, searchText, executedFrom, executedTo, from, sort)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DecisionInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DecisionInstanceApi apiInstance = new DecisionInstanceApi(defaultClient);
        Integer to = 56; // Integer | Exclusive end offset
        String decisionId = "decisionId_example"; // String | 
        Integer version = 56; // Integer | 
        String searchText = "searchText_example"; // String | 
        OffsetDateTime executedFrom = OffsetDateTime.now(); // OffsetDateTime | 
        OffsetDateTime executedTo = OffsetDateTime.now(); // OffsetDateTime | 
        Integer from = 0; // Integer | Inclusive 0-based offset
        String sort = "-executedAt"; // String | 
        try {
            ApiResponse<DecisionInstanceScrollDTO> response = apiInstance.scrollDecisionInstancesWithHttpInfo(to, decisionId, version, searchText, executedFrom, executedTo, from, sort);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DecisionInstanceApi#scrollDecisionInstances");
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
| **decisionId** | **String**|  | [optional] |
| **version** | **Integer**|  | [optional] |
| **searchText** | **String**|  | [optional] |
| **executedFrom** | **OffsetDateTime**|  | [optional] |
| **executedTo** | **OffsetDateTime**|  | [optional] |
| **from** | **Integer**| Inclusive 0-based offset | [optional] [default to 0] |
| **sort** | **String**|  | [optional] [default to -executedAt] |

### Return type

ApiResponse<[**DecisionInstanceScrollDTO**](DecisionInstanceScrollDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

