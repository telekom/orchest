# VariablesApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getVariables**](VariablesApi.md#getVariables) | **GET** /variables/{processInstanceId} |  |
| [**getVariablesWithHttpInfo**](VariablesApi.md#getVariablesWithHttpInfo) | **GET** /variables/{processInstanceId} |  |
| [**modifyVariables**](VariablesApi.md#modifyVariables) | **POST** /variables |  |
| [**modifyVariablesWithHttpInfo**](VariablesApi.md#modifyVariablesWithHttpInfo) | **POST** /variables |  |



## getVariables

> ResponseDTOVariablesResponse getVariables(processInstanceId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.VariablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        VariablesApi apiInstance = new VariablesApi(defaultClient);
        String processInstanceId = "processInstanceId_example"; // String | 
        try {
            ResponseDTOVariablesResponse result = apiInstance.getVariables(processInstanceId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling VariablesApi#getVariables");
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
| **processInstanceId** | **String**|  | |

### Return type

[**ResponseDTOVariablesResponse**](ResponseDTOVariablesResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getVariablesWithHttpInfo

> ApiResponse<ResponseDTOVariablesResponse> getVariables getVariablesWithHttpInfo(processInstanceId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.VariablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        VariablesApi apiInstance = new VariablesApi(defaultClient);
        String processInstanceId = "processInstanceId_example"; // String | 
        try {
            ApiResponse<ResponseDTOVariablesResponse> response = apiInstance.getVariablesWithHttpInfo(processInstanceId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling VariablesApi#getVariables");
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
| **processInstanceId** | **String**|  | |

### Return type

ApiResponse<[**ResponseDTOVariablesResponse**](ResponseDTOVariablesResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## modifyVariables

> ResponseDTOVariablesResponse modifyVariables(variablesRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.VariablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        VariablesApi apiInstance = new VariablesApi(defaultClient);
        VariablesRequest variablesRequest = new VariablesRequest(); // VariablesRequest | 
        try {
            ResponseDTOVariablesResponse result = apiInstance.modifyVariables(variablesRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling VariablesApi#modifyVariables");
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
| **variablesRequest** | [**VariablesRequest**](VariablesRequest.md)|  | |

### Return type

[**ResponseDTOVariablesResponse**](ResponseDTOVariablesResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## modifyVariablesWithHttpInfo

> ApiResponse<ResponseDTOVariablesResponse> modifyVariables modifyVariablesWithHttpInfo(variablesRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.VariablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        VariablesApi apiInstance = new VariablesApi(defaultClient);
        VariablesRequest variablesRequest = new VariablesRequest(); // VariablesRequest | 
        try {
            ApiResponse<ResponseDTOVariablesResponse> response = apiInstance.modifyVariablesWithHttpInfo(variablesRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling VariablesApi#modifyVariables");
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
| **variablesRequest** | [**VariablesRequest**](VariablesRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTOVariablesResponse**](ResponseDTOVariablesResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

