# KafkaInspectionApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getClusterInfo**](KafkaInspectionApi.md#getClusterInfo) | **GET** /kafka/cluster-info |  |
| [**getClusterInfoWithHttpInfo**](KafkaInspectionApi.md#getClusterInfoWithHttpInfo) | **GET** /kafka/cluster-info |  |
| [**getProcessHealth**](KafkaInspectionApi.md#getProcessHealth) | **GET** /kafka/process-health |  |
| [**getProcessHealthWithHttpInfo**](KafkaInspectionApi.md#getProcessHealthWithHttpInfo) | **GET** /kafka/process-health |  |
| [**getWorkerInfo**](KafkaInspectionApi.md#getWorkerInfo) | **GET** /kafka/worker-info |  |
| [**getWorkerInfoWithHttpInfo**](KafkaInspectionApi.md#getWorkerInfoWithHttpInfo) | **GET** /kafka/worker-info |  |



## getClusterInfo

> ResponseDTOClusterInfo getClusterInfo()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.KafkaInspectionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        KafkaInspectionApi apiInstance = new KafkaInspectionApi(defaultClient);
        try {
            ResponseDTOClusterInfo result = apiInstance.getClusterInfo();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling KafkaInspectionApi#getClusterInfo");
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

[**ResponseDTOClusterInfo**](ResponseDTOClusterInfo.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getClusterInfoWithHttpInfo

> ApiResponse<ResponseDTOClusterInfo> getClusterInfo getClusterInfoWithHttpInfo()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.KafkaInspectionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        KafkaInspectionApi apiInstance = new KafkaInspectionApi(defaultClient);
        try {
            ApiResponse<ResponseDTOClusterInfo> response = apiInstance.getClusterInfoWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling KafkaInspectionApi#getClusterInfo");
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

ApiResponse<[**ResponseDTOClusterInfo**](ResponseDTOClusterInfo.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getProcessHealth

> ResponseDTOListProcessHealth getProcessHealth()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.KafkaInspectionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        KafkaInspectionApi apiInstance = new KafkaInspectionApi(defaultClient);
        try {
            ResponseDTOListProcessHealth result = apiInstance.getProcessHealth();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling KafkaInspectionApi#getProcessHealth");
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

[**ResponseDTOListProcessHealth**](ResponseDTOListProcessHealth.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getProcessHealthWithHttpInfo

> ApiResponse<ResponseDTOListProcessHealth> getProcessHealth getProcessHealthWithHttpInfo()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.KafkaInspectionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        KafkaInspectionApi apiInstance = new KafkaInspectionApi(defaultClient);
        try {
            ApiResponse<ResponseDTOListProcessHealth> response = apiInstance.getProcessHealthWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling KafkaInspectionApi#getProcessHealth");
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

ApiResponse<[**ResponseDTOListProcessHealth**](ResponseDTOListProcessHealth.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getWorkerInfo

> ResponseDTOWorkerInfo getWorkerInfo(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.KafkaInspectionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        KafkaInspectionApi apiInstance = new KafkaInspectionApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ResponseDTOWorkerInfo result = apiInstance.getWorkerInfo(processDefinitionId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling KafkaInspectionApi#getWorkerInfo");
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

[**ResponseDTOWorkerInfo**](ResponseDTOWorkerInfo.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getWorkerInfoWithHttpInfo

> ApiResponse<ResponseDTOWorkerInfo> getWorkerInfo getWorkerInfoWithHttpInfo(processDefinitionId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.KafkaInspectionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        KafkaInspectionApi apiInstance = new KafkaInspectionApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            ApiResponse<ResponseDTOWorkerInfo> response = apiInstance.getWorkerInfoWithHttpInfo(processDefinitionId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling KafkaInspectionApi#getWorkerInfo");
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

ApiResponse<[**ResponseDTOWorkerInfo**](ResponseDTOWorkerInfo.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

