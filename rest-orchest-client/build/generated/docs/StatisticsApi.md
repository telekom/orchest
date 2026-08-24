# StatisticsApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**stats**](StatisticsApi.md#stats) | **GET** /stats |  |
| [**statsWithHttpInfo**](StatisticsApi.md#statsWithHttpInfo) | **GET** /stats |  |



## stats

> ResponseDTOStatsDTO stats(hardReload, from, to, tz)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.StatisticsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        StatisticsApi apiInstance = new StatisticsApi(defaultClient);
        Boolean hardReload = false; // Boolean | 
        OffsetDateTime from = OffsetDateTime.now(); // OffsetDateTime | Inclusive lower bound on createdAt/executedAt (ISO-8601 instant)
        OffsetDateTime to = OffsetDateTime.now(); // OffsetDateTime | Exclusive upper bound on createdAt/executedAt (ISO-8601 instant)
        String tz = "tz_example"; // String | IANA zone id used to render lastUpdatedAt; defaults to UTC
        try {
            ResponseDTOStatsDTO result = apiInstance.stats(hardReload, from, to, tz);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling StatisticsApi#stats");
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
| **hardReload** | **Boolean**|  | [optional] [default to false] |
| **from** | **OffsetDateTime**| Inclusive lower bound on createdAt/executedAt (ISO-8601 instant) | [optional] |
| **to** | **OffsetDateTime**| Exclusive upper bound on createdAt/executedAt (ISO-8601 instant) | [optional] |
| **tz** | **String**| IANA zone id used to render lastUpdatedAt; defaults to UTC | [optional] |

### Return type

[**ResponseDTOStatsDTO**](ResponseDTOStatsDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## statsWithHttpInfo

> ApiResponse<ResponseDTOStatsDTO> stats statsWithHttpInfo(hardReload, from, to, tz)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.StatisticsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        StatisticsApi apiInstance = new StatisticsApi(defaultClient);
        Boolean hardReload = false; // Boolean | 
        OffsetDateTime from = OffsetDateTime.now(); // OffsetDateTime | Inclusive lower bound on createdAt/executedAt (ISO-8601 instant)
        OffsetDateTime to = OffsetDateTime.now(); // OffsetDateTime | Exclusive upper bound on createdAt/executedAt (ISO-8601 instant)
        String tz = "tz_example"; // String | IANA zone id used to render lastUpdatedAt; defaults to UTC
        try {
            ApiResponse<ResponseDTOStatsDTO> response = apiInstance.statsWithHttpInfo(hardReload, from, to, tz);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling StatisticsApi#stats");
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
| **hardReload** | **Boolean**|  | [optional] [default to false] |
| **from** | **OffsetDateTime**| Inclusive lower bound on createdAt/executedAt (ISO-8601 instant) | [optional] |
| **to** | **OffsetDateTime**| Exclusive upper bound on createdAt/executedAt (ISO-8601 instant) | [optional] |
| **tz** | **String**| IANA zone id used to render lastUpdatedAt; defaults to UTC | [optional] |

### Return type

ApiResponse<[**ResponseDTOStatsDTO**](ResponseDTOStatsDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

