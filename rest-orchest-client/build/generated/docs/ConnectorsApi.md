# ConnectorsApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getConnector**](ConnectorsApi.md#getConnector) | **GET** /connectors/{name} |  |
| [**getConnectorWithHttpInfo**](ConnectorsApi.md#getConnectorWithHttpInfo) | **GET** /connectors/{name} |  |
| [**getConnectors**](ConnectorsApi.md#getConnectors) | **GET** /connectors |  |
| [**getConnectorsWithHttpInfo**](ConnectorsApi.md#getConnectorsWithHttpInfo) | **GET** /connectors |  |
| [**uploadConnector**](ConnectorsApi.md#uploadConnector) | **POST** /connectors/upload |  |
| [**uploadConnectorWithHttpInfo**](ConnectorsApi.md#uploadConnectorWithHttpInfo) | **POST** /connectors/upload |  |



## getConnector

> ResponseDTOConnector getConnector(name)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ConnectorsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ConnectorsApi apiInstance = new ConnectorsApi(defaultClient);
        String name = "name_example"; // String | 
        try {
            ResponseDTOConnector result = apiInstance.getConnector(name);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ConnectorsApi#getConnector");
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
| **name** | **String**|  | |

### Return type

[**ResponseDTOConnector**](ResponseDTOConnector.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getConnectorWithHttpInfo

> ApiResponse<ResponseDTOConnector> getConnector getConnectorWithHttpInfo(name)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ConnectorsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ConnectorsApi apiInstance = new ConnectorsApi(defaultClient);
        String name = "name_example"; // String | 
        try {
            ApiResponse<ResponseDTOConnector> response = apiInstance.getConnectorWithHttpInfo(name);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ConnectorsApi#getConnector");
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
| **name** | **String**|  | |

### Return type

ApiResponse<[**ResponseDTOConnector**](ResponseDTOConnector.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getConnectors

> PageConnector getConnectors(page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ConnectorsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ConnectorsApi apiInstance = new ConnectorsApi(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            PageConnector result = apiInstance.getConnectors(page, size);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ConnectorsApi#getConnectors");
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

[**PageConnector**](PageConnector.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getConnectorsWithHttpInfo

> ApiResponse<PageConnector> getConnectors getConnectorsWithHttpInfo(page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ConnectorsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ConnectorsApi apiInstance = new ConnectorsApi(defaultClient);
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            ApiResponse<PageConnector> response = apiInstance.getConnectorsWithHttpInfo(page, size);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ConnectorsApi#getConnectors");
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

ApiResponse<[**PageConnector**](PageConnector.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## uploadConnector

> ResponseDTOListConnector uploadConnector(connectorsData)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ConnectorsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ConnectorsApi apiInstance = new ConnectorsApi(defaultClient);
        List<Map<String, Object>> connectorsData = Arrays.asList(new HashMap<>()); // List<Map<String, Object>> | 
        try {
            ResponseDTOListConnector result = apiInstance.uploadConnector(connectorsData);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ConnectorsApi#uploadConnector");
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
| **connectorsData** | [**List&lt;Map&lt;String, Object&gt;&gt;**](Map&lt;String, Object&gt;.md)|  | |

### Return type

[**ResponseDTOListConnector**](ResponseDTOListConnector.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## uploadConnectorWithHttpInfo

> ApiResponse<ResponseDTOListConnector> uploadConnector uploadConnectorWithHttpInfo(connectorsData)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.ConnectorsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        ConnectorsApi apiInstance = new ConnectorsApi(defaultClient);
        List<Map<String, Object>> connectorsData = Arrays.asList(new HashMap<>()); // List<Map<String, Object>> | 
        try {
            ApiResponse<ResponseDTOListConnector> response = apiInstance.uploadConnectorWithHttpInfo(connectorsData);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling ConnectorsApi#uploadConnector");
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
| **connectorsData** | [**List&lt;Map&lt;String, Object&gt;&gt;**](Map&lt;String, Object&gt;.md)|  | |

### Return type

ApiResponse<[**ResponseDTOListConnector**](ResponseDTOListConnector.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

