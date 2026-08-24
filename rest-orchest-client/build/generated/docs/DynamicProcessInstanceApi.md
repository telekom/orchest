# DynamicProcessInstanceApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createProcessInstance1**](DynamicProcessInstanceApi.md#createProcessInstance1) | **POST** /dyanmicProcessInstances/create |  |
| [**createProcessInstance1WithHttpInfo**](DynamicProcessInstanceApi.md#createProcessInstance1WithHttpInfo) | **POST** /dyanmicProcessInstances/create |  |



## createProcessInstance1

> ResponseDTODynamicProcessInvocationResponse createProcessInstance1(dynamicProcessInvocationRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DynamicProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DynamicProcessInstanceApi apiInstance = new DynamicProcessInstanceApi(defaultClient);
        DynamicProcessInvocationRequest dynamicProcessInvocationRequest = new DynamicProcessInvocationRequest(); // DynamicProcessInvocationRequest | 
        try {
            ResponseDTODynamicProcessInvocationResponse result = apiInstance.createProcessInstance1(dynamicProcessInvocationRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DynamicProcessInstanceApi#createProcessInstance1");
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
| **dynamicProcessInvocationRequest** | [**DynamicProcessInvocationRequest**](DynamicProcessInvocationRequest.md)|  | |

### Return type

[**ResponseDTODynamicProcessInvocationResponse**](ResponseDTODynamicProcessInvocationResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## createProcessInstance1WithHttpInfo

> ApiResponse<ResponseDTODynamicProcessInvocationResponse> createProcessInstance1 createProcessInstance1WithHttpInfo(dynamicProcessInvocationRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DynamicProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DynamicProcessInstanceApi apiInstance = new DynamicProcessInstanceApi(defaultClient);
        DynamicProcessInvocationRequest dynamicProcessInvocationRequest = new DynamicProcessInvocationRequest(); // DynamicProcessInvocationRequest | 
        try {
            ApiResponse<ResponseDTODynamicProcessInvocationResponse> response = apiInstance.createProcessInstance1WithHttpInfo(dynamicProcessInvocationRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DynamicProcessInstanceApi#createProcessInstance1");
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
| **dynamicProcessInvocationRequest** | [**DynamicProcessInvocationRequest**](DynamicProcessInvocationRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTODynamicProcessInvocationResponse**](ResponseDTODynamicProcessInvocationResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

