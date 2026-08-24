# RateLimitApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getRateLimitForProcessId**](RateLimitApi.md#getRateLimitForProcessId) | **GET** /rateLimit/{processDefinitionId} |  |
| [**getRateLimitForProcessIdWithHttpInfo**](RateLimitApi.md#getRateLimitForProcessIdWithHttpInfo) | **GET** /rateLimit/{processDefinitionId} |  |
| [**getRateLimits**](RateLimitApi.md#getRateLimits) | **GET** /rateLimits |  |
| [**getRateLimitsWithHttpInfo**](RateLimitApi.md#getRateLimitsWithHttpInfo) | **GET** /rateLimits |  |
| [**upsertRateLimit**](RateLimitApi.md#upsertRateLimit) | **PUT** /rateLimit |  |
| [**upsertRateLimitWithHttpInfo**](RateLimitApi.md#upsertRateLimitWithHttpInfo) | **PUT** /rateLimit |  |



## getRateLimitForProcessId

> RateLimit getRateLimitForProcessId(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.RateLimitApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        RateLimitApi apiInstance = new RateLimitApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            RateLimit result = apiInstance.getRateLimitForProcessId(processDefinitionId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RateLimitApi#getRateLimitForProcessId");
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

[**RateLimit**](RateLimit.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getRateLimitForProcessIdWithHttpInfo

> ApiResponse<RateLimit> getRateLimitForProcessId getRateLimitForProcessIdWithHttpInfo(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.RateLimitApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        RateLimitApi apiInstance = new RateLimitApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ApiResponse<RateLimit> response = apiInstance.getRateLimitForProcessIdWithHttpInfo(processDefinitionId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling RateLimitApi#getRateLimitForProcessId");
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

ApiResponse<[**RateLimit**](RateLimit.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getRateLimits

> List<RateLimit> getRateLimits()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.RateLimitApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        RateLimitApi apiInstance = new RateLimitApi(defaultClient);
        try {
            List<RateLimit> result = apiInstance.getRateLimits();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RateLimitApi#getRateLimits");
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

[**List&lt;RateLimit&gt;**](RateLimit.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getRateLimitsWithHttpInfo

> ApiResponse<List<RateLimit>> getRateLimits getRateLimitsWithHttpInfo()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.RateLimitApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        RateLimitApi apiInstance = new RateLimitApi(defaultClient);
        try {
            ApiResponse<List<RateLimit>> response = apiInstance.getRateLimitsWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling RateLimitApi#getRateLimits");
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

ApiResponse<[**List&lt;RateLimit&gt;**](RateLimit.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## upsertRateLimit

> RateLimit upsertRateLimit(rateLimit)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.RateLimitApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        RateLimitApi apiInstance = new RateLimitApi(defaultClient);
        RateLimit rateLimit = new RateLimit(); // RateLimit | 
        try {
            RateLimit result = apiInstance.upsertRateLimit(rateLimit);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RateLimitApi#upsertRateLimit");
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
| **rateLimit** | [**RateLimit**](RateLimit.md)|  | |

### Return type

[**RateLimit**](RateLimit.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## upsertRateLimitWithHttpInfo

> ApiResponse<RateLimit> upsertRateLimit upsertRateLimitWithHttpInfo(rateLimit)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.RateLimitApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        RateLimitApi apiInstance = new RateLimitApi(defaultClient);
        RateLimit rateLimit = new RateLimit(); // RateLimit | 
        try {
            ApiResponse<RateLimit> response = apiInstance.upsertRateLimitWithHttpInfo(rateLimit);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling RateLimitApi#upsertRateLimit");
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
| **rateLimit** | [**RateLimit**](RateLimit.md)|  | |

### Return type

ApiResponse<[**RateLimit**](RateLimit.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

