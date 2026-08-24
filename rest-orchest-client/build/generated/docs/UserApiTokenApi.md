# UserApiTokenApi

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createToken**](UserApiTokenApi.md#createToken) | **POST** /apiTokens |  |
| [**createTokenWithHttpInfo**](UserApiTokenApi.md#createTokenWithHttpInfo) | **POST** /apiTokens |  |
| [**getToken**](UserApiTokenApi.md#getToken) | **GET** /apiTokens/{tokenId} |  |
| [**getTokenWithHttpInfo**](UserApiTokenApi.md#getTokenWithHttpInfo) | **GET** /apiTokens/{tokenId} |  |
| [**listTokens**](UserApiTokenApi.md#listTokens) | **GET** /apiTokens |  |
| [**listTokensWithHttpInfo**](UserApiTokenApi.md#listTokensWithHttpInfo) | **GET** /apiTokens |  |
| [**revokeAllTokens**](UserApiTokenApi.md#revokeAllTokens) | **DELETE** /apiTokens |  |
| [**revokeAllTokensWithHttpInfo**](UserApiTokenApi.md#revokeAllTokensWithHttpInfo) | **DELETE** /apiTokens |  |
| [**revokeToken**](UserApiTokenApi.md#revokeToken) | **DELETE** /apiTokens/{tokenId} |  |
| [**revokeTokenWithHttpInfo**](UserApiTokenApi.md#revokeTokenWithHttpInfo) | **DELETE** /apiTokens/{tokenId} |  |



## createToken

> ResponseDTOCreateApiTokenResponse createToken(createApiTokenRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserApiTokenApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserApiTokenApi apiInstance = new UserApiTokenApi(defaultClient);
        CreateApiTokenRequest createApiTokenRequest = new CreateApiTokenRequest(); // CreateApiTokenRequest | 
        try {
            ResponseDTOCreateApiTokenResponse result = apiInstance.createToken(createApiTokenRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserApiTokenApi#createToken");
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
| **createApiTokenRequest** | [**CreateApiTokenRequest**](CreateApiTokenRequest.md)|  | |

### Return type

[**ResponseDTOCreateApiTokenResponse**](ResponseDTOCreateApiTokenResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## createTokenWithHttpInfo

> ApiResponse<ResponseDTOCreateApiTokenResponse> createToken createTokenWithHttpInfo(createApiTokenRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserApiTokenApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserApiTokenApi apiInstance = new UserApiTokenApi(defaultClient);
        CreateApiTokenRequest createApiTokenRequest = new CreateApiTokenRequest(); // CreateApiTokenRequest | 
        try {
            ApiResponse<ResponseDTOCreateApiTokenResponse> response = apiInstance.createTokenWithHttpInfo(createApiTokenRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserApiTokenApi#createToken");
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
| **createApiTokenRequest** | [**CreateApiTokenRequest**](CreateApiTokenRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTOCreateApiTokenResponse**](ResponseDTOCreateApiTokenResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getToken

> ResponseDTOApiTokenResponse getToken(tokenId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserApiTokenApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserApiTokenApi apiInstance = new UserApiTokenApi(defaultClient);
        String tokenId = "tokenId_example"; // String | 
        try {
            ResponseDTOApiTokenResponse result = apiInstance.getToken(tokenId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserApiTokenApi#getToken");
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
| **tokenId** | **String**|  | |

### Return type

[**ResponseDTOApiTokenResponse**](ResponseDTOApiTokenResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getTokenWithHttpInfo

> ApiResponse<ResponseDTOApiTokenResponse> getToken getTokenWithHttpInfo(tokenId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserApiTokenApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserApiTokenApi apiInstance = new UserApiTokenApi(defaultClient);
        String tokenId = "tokenId_example"; // String | 
        try {
            ApiResponse<ResponseDTOApiTokenResponse> response = apiInstance.getTokenWithHttpInfo(tokenId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserApiTokenApi#getToken");
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
| **tokenId** | **String**|  | |

### Return type

ApiResponse<[**ResponseDTOApiTokenResponse**](ResponseDTOApiTokenResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## listTokens

> ResponseDTOListApiTokenResponse listTokens()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserApiTokenApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserApiTokenApi apiInstance = new UserApiTokenApi(defaultClient);
        try {
            ResponseDTOListApiTokenResponse result = apiInstance.listTokens();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserApiTokenApi#listTokens");
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

[**ResponseDTOListApiTokenResponse**](ResponseDTOListApiTokenResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## listTokensWithHttpInfo

> ApiResponse<ResponseDTOListApiTokenResponse> listTokens listTokensWithHttpInfo()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserApiTokenApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserApiTokenApi apiInstance = new UserApiTokenApi(defaultClient);
        try {
            ApiResponse<ResponseDTOListApiTokenResponse> response = apiInstance.listTokensWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserApiTokenApi#listTokens");
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

ApiResponse<[**ResponseDTOListApiTokenResponse**](ResponseDTOListApiTokenResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## revokeAllTokens

> ResponseDTOString revokeAllTokens()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserApiTokenApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserApiTokenApi apiInstance = new UserApiTokenApi(defaultClient);
        try {
            ResponseDTOString result = apiInstance.revokeAllTokens();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserApiTokenApi#revokeAllTokens");
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

[**ResponseDTOString**](ResponseDTOString.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## revokeAllTokensWithHttpInfo

> ApiResponse<ResponseDTOString> revokeAllTokens revokeAllTokensWithHttpInfo()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserApiTokenApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserApiTokenApi apiInstance = new UserApiTokenApi(defaultClient);
        try {
            ApiResponse<ResponseDTOString> response = apiInstance.revokeAllTokensWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserApiTokenApi#revokeAllTokens");
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

ApiResponse<[**ResponseDTOString**](ResponseDTOString.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## revokeToken

> ResponseDTOString revokeToken(tokenId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserApiTokenApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserApiTokenApi apiInstance = new UserApiTokenApi(defaultClient);
        String tokenId = "tokenId_example"; // String | 
        try {
            ResponseDTOString result = apiInstance.revokeToken(tokenId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserApiTokenApi#revokeToken");
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
| **tokenId** | **String**|  | |

### Return type

[**ResponseDTOString**](ResponseDTOString.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## revokeTokenWithHttpInfo

> ApiResponse<ResponseDTOString> revokeToken revokeTokenWithHttpInfo(tokenId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.UserApiTokenApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        UserApiTokenApi apiInstance = new UserApiTokenApi(defaultClient);
        String tokenId = "tokenId_example"; // String | 
        try {
            ApiResponse<ResponseDTOString> response = apiInstance.revokeTokenWithHttpInfo(tokenId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling UserApiTokenApi#revokeToken");
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
| **tokenId** | **String**|  | |

### Return type

ApiResponse<[**ResponseDTOString**](ResponseDTOString.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

