# IncidentManagementApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getIncident**](IncidentManagementApi.md#getIncident) | **GET** /incidents/{processInstanceId} |  |
| [**getIncidentWithHttpInfo**](IncidentManagementApi.md#getIncidentWithHttpInfo) | **GET** /incidents/{processInstanceId} |  |
| [**getIncidents**](IncidentManagementApi.md#getIncidents) | **GET** /incidents |  |
| [**getIncidentsWithHttpInfo**](IncidentManagementApi.md#getIncidentsWithHttpInfo) | **GET** /incidents |  |
| [**resolveIncident1**](IncidentManagementApi.md#resolveIncident1) | **POST** /incidents/resolve/{processInstanceId} |  |
| [**resolveIncident1WithHttpInfo**](IncidentManagementApi.md#resolveIncident1WithHttpInfo) | **POST** /incidents/resolve/{processInstanceId} |  |
| [**resolveIncidentsBatch**](IncidentManagementApi.md#resolveIncidentsBatch) | **POST** /incidents/resolve |  |
| [**resolveIncidentsBatchWithHttpInfo**](IncidentManagementApi.md#resolveIncidentsBatchWithHttpInfo) | **POST** /incidents/resolve |  |



## getIncident

> ResponseDTOIncidentDTO getIncident(processInstanceId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.IncidentManagementApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        IncidentManagementApi apiInstance = new IncidentManagementApi(defaultClient);
        String processInstanceId = "processInstanceId_example"; // String | 
        try {
            ResponseDTOIncidentDTO result = apiInstance.getIncident(processInstanceId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling IncidentManagementApi#getIncident");
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

[**ResponseDTOIncidentDTO**](ResponseDTOIncidentDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getIncidentWithHttpInfo

> ApiResponse<ResponseDTOIncidentDTO> getIncident getIncidentWithHttpInfo(processInstanceId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.IncidentManagementApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        IncidentManagementApi apiInstance = new IncidentManagementApi(defaultClient);
        String processInstanceId = "processInstanceId_example"; // String | 
        try {
            ApiResponse<ResponseDTOIncidentDTO> response = apiInstance.getIncidentWithHttpInfo(processInstanceId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling IncidentManagementApi#getIncident");
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

ApiResponse<[**ResponseDTOIncidentDTO**](ResponseDTOIncidentDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getIncidents

> PageIncidentDTO getIncidents(processDefinitionId, version, searchText, from, to, page, size, sort)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.IncidentManagementApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        IncidentManagementApi apiInstance = new IncidentManagementApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        String searchText = "searchText_example"; // String | 
        OffsetDateTime from = OffsetDateTime.now(); // OffsetDateTime | 
        OffsetDateTime to = OffsetDateTime.now(); // OffsetDateTime | 
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        String sort = "-createdAt"; // String | 
        try {
            PageIncidentDTO result = apiInstance.getIncidents(processDefinitionId, version, searchText, from, to, page, size, sort);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling IncidentManagementApi#getIncidents");
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
| **processDefinitionId** | **String**|  | [optional] |
| **version** | **Integer**|  | [optional] |
| **searchText** | **String**|  | [optional] |
| **from** | **OffsetDateTime**|  | [optional] |
| **to** | **OffsetDateTime**|  | [optional] |
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sort** | **String**|  | [optional] [default to -createdAt] |

### Return type

[**PageIncidentDTO**](PageIncidentDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getIncidentsWithHttpInfo

> ApiResponse<PageIncidentDTO> getIncidents getIncidentsWithHttpInfo(processDefinitionId, version, searchText, from, to, page, size, sort)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.IncidentManagementApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        IncidentManagementApi apiInstance = new IncidentManagementApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        Integer version = 56; // Integer | 
        String searchText = "searchText_example"; // String | 
        OffsetDateTime from = OffsetDateTime.now(); // OffsetDateTime | 
        OffsetDateTime to = OffsetDateTime.now(); // OffsetDateTime | 
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        String sort = "-createdAt"; // String | 
        try {
            ApiResponse<PageIncidentDTO> response = apiInstance.getIncidentsWithHttpInfo(processDefinitionId, version, searchText, from, to, page, size, sort);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling IncidentManagementApi#getIncidents");
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
| **processDefinitionId** | **String**|  | [optional] |
| **version** | **Integer**|  | [optional] |
| **searchText** | **String**|  | [optional] |
| **from** | **OffsetDateTime**|  | [optional] |
| **to** | **OffsetDateTime**|  | [optional] |
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |
| **sort** | **String**|  | [optional] [default to -createdAt] |

### Return type

ApiResponse<[**PageIncidentDTO**](PageIncidentDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## resolveIncident1

> String resolveIncident1(processInstanceId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.IncidentManagementApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        IncidentManagementApi apiInstance = new IncidentManagementApi(defaultClient);
        String processInstanceId = "processInstanceId_example"; // String | 
        try {
            String result = apiInstance.resolveIncident1(processInstanceId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling IncidentManagementApi#resolveIncident1");
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

**String**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## resolveIncident1WithHttpInfo

> ApiResponse<String> resolveIncident1 resolveIncident1WithHttpInfo(processInstanceId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.IncidentManagementApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        IncidentManagementApi apiInstance = new IncidentManagementApi(defaultClient);
        String processInstanceId = "processInstanceId_example"; // String | 
        try {
            ApiResponse<String> response = apiInstance.resolveIncident1WithHttpInfo(processInstanceId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling IncidentManagementApi#resolveIncident1");
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

ApiResponse<**String**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## resolveIncidentsBatch

> String resolveIncidentsBatch(batchInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.IncidentManagementApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        IncidentManagementApi apiInstance = new IncidentManagementApi(defaultClient);
        BatchInstanceRequest batchInstanceRequest = new BatchInstanceRequest(); // BatchInstanceRequest | 
        try {
            String result = apiInstance.resolveIncidentsBatch(batchInstanceRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling IncidentManagementApi#resolveIncidentsBatch");
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
| **batchInstanceRequest** | [**BatchInstanceRequest**](BatchInstanceRequest.md)|  | |

### Return type

**String**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## resolveIncidentsBatchWithHttpInfo

> ApiResponse<String> resolveIncidentsBatch resolveIncidentsBatchWithHttpInfo(batchInstanceRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.IncidentManagementApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        IncidentManagementApi apiInstance = new IncidentManagementApi(defaultClient);
        BatchInstanceRequest batchInstanceRequest = new BatchInstanceRequest(); // BatchInstanceRequest | 
        try {
            ApiResponse<String> response = apiInstance.resolveIncidentsBatchWithHttpInfo(batchInstanceRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling IncidentManagementApi#resolveIncidentsBatch");
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
| **batchInstanceRequest** | [**BatchInstanceRequest**](BatchInstanceRequest.md)|  | |

### Return type

ApiResponse<**String**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

