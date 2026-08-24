# Eventing

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**publishMessage**](Eventing.md#publishMessage) | **POST** /eventing/sendMessage |  |
| [**publishMessageWithHttpInfo**](Eventing.md#publishMessageWithHttpInfo) | **POST** /eventing/sendMessage |  |
| [**publishSignal**](Eventing.md#publishSignal) | **POST** /eventing/sendSignal |  |
| [**publishSignalWithHttpInfo**](Eventing.md#publishSignalWithHttpInfo) | **POST** /eventing/sendSignal |  |



## publishMessage

> ResponseDTOMessageEventResponse publishMessage(sendMessageEventRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.Eventing;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        Eventing apiInstance = new Eventing(defaultClient);
        SendMessageEventRequest sendMessageEventRequest = new SendMessageEventRequest(); // SendMessageEventRequest | 
        try {
            ResponseDTOMessageEventResponse result = apiInstance.publishMessage(sendMessageEventRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling Eventing#publishMessage");
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
| **sendMessageEventRequest** | [**SendMessageEventRequest**](SendMessageEventRequest.md)|  | |

### Return type

[**ResponseDTOMessageEventResponse**](ResponseDTOMessageEventResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## publishMessageWithHttpInfo

> ApiResponse<ResponseDTOMessageEventResponse> publishMessage publishMessageWithHttpInfo(sendMessageEventRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.Eventing;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        Eventing apiInstance = new Eventing(defaultClient);
        SendMessageEventRequest sendMessageEventRequest = new SendMessageEventRequest(); // SendMessageEventRequest | 
        try {
            ApiResponse<ResponseDTOMessageEventResponse> response = apiInstance.publishMessageWithHttpInfo(sendMessageEventRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling Eventing#publishMessage");
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
| **sendMessageEventRequest** | [**SendMessageEventRequest**](SendMessageEventRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTOMessageEventResponse**](ResponseDTOMessageEventResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## publishSignal

> ResponseDTOSignalEventResponse publishSignal(sendSignalEventRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.Eventing;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        Eventing apiInstance = new Eventing(defaultClient);
        SendSignalEventRequest sendSignalEventRequest = new SendSignalEventRequest(); // SendSignalEventRequest | 
        try {
            ResponseDTOSignalEventResponse result = apiInstance.publishSignal(sendSignalEventRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling Eventing#publishSignal");
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
| **sendSignalEventRequest** | [**SendSignalEventRequest**](SendSignalEventRequest.md)|  | |

### Return type

[**ResponseDTOSignalEventResponse**](ResponseDTOSignalEventResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## publishSignalWithHttpInfo

> ApiResponse<ResponseDTOSignalEventResponse> publishSignal publishSignalWithHttpInfo(sendSignalEventRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.Eventing;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        Eventing apiInstance = new Eventing(defaultClient);
        SendSignalEventRequest sendSignalEventRequest = new SendSignalEventRequest(); // SendSignalEventRequest | 
        try {
            ApiResponse<ResponseDTOSignalEventResponse> response = apiInstance.publishSignalWithHttpInfo(sendSignalEventRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling Eventing#publishSignal");
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
| **sendSignalEventRequest** | [**SendSignalEventRequest**](SendSignalEventRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTOSignalEventResponse**](ResponseDTOSignalEventResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

