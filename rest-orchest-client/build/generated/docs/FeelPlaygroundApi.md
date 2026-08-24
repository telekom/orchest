# FeelPlaygroundApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**evaluate**](FeelPlaygroundApi.md#evaluate) | **POST** /feelPlayground/evaluate |  |
| [**evaluateWithHttpInfo**](FeelPlaygroundApi.md#evaluateWithHttpInfo) | **POST** /feelPlayground/evaluate |  |
| [**validate**](FeelPlaygroundApi.md#validate) | **POST** /feelPlayground/validate |  |
| [**validateWithHttpInfo**](FeelPlaygroundApi.md#validateWithHttpInfo) | **POST** /feelPlayground/validate |  |



## evaluate

> ResponseDTOFeelEvaluationDTO evaluate(feelEvaluateRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.FeelPlaygroundApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        FeelPlaygroundApi apiInstance = new FeelPlaygroundApi(defaultClient);
        FeelEvaluateRequest feelEvaluateRequest = new FeelEvaluateRequest(); // FeelEvaluateRequest | 
        try {
            ResponseDTOFeelEvaluationDTO result = apiInstance.evaluate(feelEvaluateRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling FeelPlaygroundApi#evaluate");
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
| **feelEvaluateRequest** | [**FeelEvaluateRequest**](FeelEvaluateRequest.md)|  | |

### Return type

[**ResponseDTOFeelEvaluationDTO**](ResponseDTOFeelEvaluationDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## evaluateWithHttpInfo

> ApiResponse<ResponseDTOFeelEvaluationDTO> evaluate evaluateWithHttpInfo(feelEvaluateRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.FeelPlaygroundApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        FeelPlaygroundApi apiInstance = new FeelPlaygroundApi(defaultClient);
        FeelEvaluateRequest feelEvaluateRequest = new FeelEvaluateRequest(); // FeelEvaluateRequest | 
        try {
            ApiResponse<ResponseDTOFeelEvaluationDTO> response = apiInstance.evaluateWithHttpInfo(feelEvaluateRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling FeelPlaygroundApi#evaluate");
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
| **feelEvaluateRequest** | [**FeelEvaluateRequest**](FeelEvaluateRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTOFeelEvaluationDTO**](ResponseDTOFeelEvaluationDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## validate

> ResponseDTOFeelValidationDTO validate(feelValidateRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.FeelPlaygroundApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        FeelPlaygroundApi apiInstance = new FeelPlaygroundApi(defaultClient);
        FeelValidateRequest feelValidateRequest = new FeelValidateRequest(); // FeelValidateRequest | 
        try {
            ResponseDTOFeelValidationDTO result = apiInstance.validate(feelValidateRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling FeelPlaygroundApi#validate");
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
| **feelValidateRequest** | [**FeelValidateRequest**](FeelValidateRequest.md)|  | |

### Return type

[**ResponseDTOFeelValidationDTO**](ResponseDTOFeelValidationDTO.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## validateWithHttpInfo

> ApiResponse<ResponseDTOFeelValidationDTO> validate validateWithHttpInfo(feelValidateRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.FeelPlaygroundApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        FeelPlaygroundApi apiInstance = new FeelPlaygroundApi(defaultClient);
        FeelValidateRequest feelValidateRequest = new FeelValidateRequest(); // FeelValidateRequest | 
        try {
            ApiResponse<ResponseDTOFeelValidationDTO> response = apiInstance.validateWithHttpInfo(feelValidateRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling FeelPlaygroundApi#validate");
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
| **feelValidateRequest** | [**FeelValidateRequest**](FeelValidateRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTOFeelValidationDTO**](ResponseDTOFeelValidationDTO.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

