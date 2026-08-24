# OrchestChatApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**analyse**](OrchestChatApi.md#analyse) | **POST** /chats/analyse |  |
| [**analyseWithHttpInfo**](OrchestChatApi.md#analyseWithHttpInfo) | **POST** /chats/analyse |  |



## analyse

> ResponseDTOString analyse(chatRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.OrchestChatApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        OrchestChatApi apiInstance = new OrchestChatApi(defaultClient);
        ChatRequest chatRequest = new ChatRequest(); // ChatRequest | 
        try {
            ResponseDTOString result = apiInstance.analyse(chatRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling OrchestChatApi#analyse");
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
| **chatRequest** | [**ChatRequest**](ChatRequest.md)|  | |

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

## analyseWithHttpInfo

> ApiResponse<ResponseDTOString> analyse analyseWithHttpInfo(chatRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.OrchestChatApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        OrchestChatApi apiInstance = new OrchestChatApi(defaultClient);
        ChatRequest chatRequest = new ChatRequest(); // ChatRequest | 
        try {
            ApiResponse<ResponseDTOString> response = apiInstance.analyseWithHttpInfo(chatRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling OrchestChatApi#analyse");
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
| **chatRequest** | [**ChatRequest**](ChatRequest.md)|  | |

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

