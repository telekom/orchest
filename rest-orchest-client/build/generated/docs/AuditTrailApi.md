# AuditTrailApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getByPath**](AuditTrailApi.md#getByPath) | **GET** /auditTrails/path |  |
| [**getByPathWithHttpInfo**](AuditTrailApi.md#getByPathWithHttpInfo) | **GET** /auditTrails/path |  |
| [**getByTimeRange**](AuditTrailApi.md#getByTimeRange) | **GET** /auditTrails/time |  |
| [**getByTimeRangeWithHttpInfo**](AuditTrailApi.md#getByTimeRangeWithHttpInfo) | **GET** /auditTrails/time |  |
| [**getByUser**](AuditTrailApi.md#getByUser) | **GET** /auditTrails/user |  |
| [**getByUserWithHttpInfo**](AuditTrailApi.md#getByUserWithHttpInfo) | **GET** /auditTrails/user |  |



## getByPath

> List<AuditTrailEntry> getByPath(path, from, to, limit)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.AuditTrailApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        AuditTrailApi apiInstance = new AuditTrailApi(defaultClient);
        String path = "path_example"; // String | 
        OffsetDateTime from = OffsetDateTime.now(); // OffsetDateTime | 
        OffsetDateTime to = OffsetDateTime.now(); // OffsetDateTime | 
        Integer limit = 100; // Integer | 
        try {
            List<AuditTrailEntry> result = apiInstance.getByPath(path, from, to, limit);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AuditTrailApi#getByPath");
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
| **path** | **String**|  | |
| **from** | **OffsetDateTime**|  | |
| **to** | **OffsetDateTime**|  | |
| **limit** | **Integer**|  | [optional] [default to 100] |

### Return type

[**List&lt;AuditTrailEntry&gt;**](AuditTrailEntry.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getByPathWithHttpInfo

> ApiResponse<List<AuditTrailEntry>> getByPath getByPathWithHttpInfo(path, from, to, limit)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.AuditTrailApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        AuditTrailApi apiInstance = new AuditTrailApi(defaultClient);
        String path = "path_example"; // String | 
        OffsetDateTime from = OffsetDateTime.now(); // OffsetDateTime | 
        OffsetDateTime to = OffsetDateTime.now(); // OffsetDateTime | 
        Integer limit = 100; // Integer | 
        try {
            ApiResponse<List<AuditTrailEntry>> response = apiInstance.getByPathWithHttpInfo(path, from, to, limit);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling AuditTrailApi#getByPath");
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
| **path** | **String**|  | |
| **from** | **OffsetDateTime**|  | |
| **to** | **OffsetDateTime**|  | |
| **limit** | **Integer**|  | [optional] [default to 100] |

### Return type

ApiResponse<[**List&lt;AuditTrailEntry&gt;**](AuditTrailEntry.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getByTimeRange

> List<AuditTrailEntry> getByTimeRange(from, to, limit)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.AuditTrailApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        AuditTrailApi apiInstance = new AuditTrailApi(defaultClient);
        OffsetDateTime from = OffsetDateTime.now(); // OffsetDateTime | 
        OffsetDateTime to = OffsetDateTime.now(); // OffsetDateTime | 
        Integer limit = 100; // Integer | 
        try {
            List<AuditTrailEntry> result = apiInstance.getByTimeRange(from, to, limit);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AuditTrailApi#getByTimeRange");
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
| **from** | **OffsetDateTime**|  | |
| **to** | **OffsetDateTime**|  | |
| **limit** | **Integer**|  | [optional] [default to 100] |

### Return type

[**List&lt;AuditTrailEntry&gt;**](AuditTrailEntry.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getByTimeRangeWithHttpInfo

> ApiResponse<List<AuditTrailEntry>> getByTimeRange getByTimeRangeWithHttpInfo(from, to, limit)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.AuditTrailApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        AuditTrailApi apiInstance = new AuditTrailApi(defaultClient);
        OffsetDateTime from = OffsetDateTime.now(); // OffsetDateTime | 
        OffsetDateTime to = OffsetDateTime.now(); // OffsetDateTime | 
        Integer limit = 100; // Integer | 
        try {
            ApiResponse<List<AuditTrailEntry>> response = apiInstance.getByTimeRangeWithHttpInfo(from, to, limit);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling AuditTrailApi#getByTimeRange");
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
| **from** | **OffsetDateTime**|  | |
| **to** | **OffsetDateTime**|  | |
| **limit** | **Integer**|  | [optional] [default to 100] |

### Return type

ApiResponse<[**List&lt;AuditTrailEntry&gt;**](AuditTrailEntry.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getByUser

> List<AuditTrailEntry> getByUser(email, limit)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.AuditTrailApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        AuditTrailApi apiInstance = new AuditTrailApi(defaultClient);
        String email = "email_example"; // String | 
        Integer limit = 50; // Integer | 
        try {
            List<AuditTrailEntry> result = apiInstance.getByUser(email, limit);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AuditTrailApi#getByUser");
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
| **email** | **String**|  | |
| **limit** | **Integer**|  | [optional] [default to 50] |

### Return type

[**List&lt;AuditTrailEntry&gt;**](AuditTrailEntry.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getByUserWithHttpInfo

> ApiResponse<List<AuditTrailEntry>> getByUser getByUserWithHttpInfo(email, limit)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.AuditTrailApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        AuditTrailApi apiInstance = new AuditTrailApi(defaultClient);
        String email = "email_example"; // String | 
        Integer limit = 50; // Integer | 
        try {
            ApiResponse<List<AuditTrailEntry>> response = apiInstance.getByUserWithHttpInfo(email, limit);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling AuditTrailApi#getByUser");
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
| **email** | **String**|  | |
| **limit** | **Integer**|  | [optional] [default to 50] |

### Return type

ApiResponse<[**List&lt;AuditTrailEntry&gt;**](AuditTrailEntry.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

