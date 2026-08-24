# DeploymentApprovals

All URIs are relative to *http://localhost:6200/orchest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addApprover**](DeploymentApprovals.md#addApprover) | **POST** /deploymentApprovals/addApprovers |  |
| [**addApproverWithHttpInfo**](DeploymentApprovals.md#addApproverWithHttpInfo) | **POST** /deploymentApprovals/addApprovers |  |
| [**deleteApprover**](DeploymentApprovals.md#deleteApprover) | **DELETE** /deploymentApprovals/removeApprover |  |
| [**deleteApproverWithHttpInfo**](DeploymentApprovals.md#deleteApproverWithHttpInfo) | **DELETE** /deploymentApprovals/removeApprover |  |
| [**getApprovals**](DeploymentApprovals.md#getApprovals) | **GET** /deploymentApprovals |  |
| [**getApprovalsWithHttpInfo**](DeploymentApprovals.md#getApprovalsWithHttpInfo) | **GET** /deploymentApprovals |  |
| [**getApprovalsByReviewer**](DeploymentApprovals.md#getApprovalsByReviewer) | **GET** /deploymentApprovals/reviews |  |
| [**getApprovalsByReviewerWithHttpInfo**](DeploymentApprovals.md#getApprovalsByReviewerWithHttpInfo) | **GET** /deploymentApprovals/reviews |  |
| [**getApprover**](DeploymentApprovals.md#getApprover) | **GET** /deploymentApprovals/getApprovers |  |
| [**getApproverWithHttpInfo**](DeploymentApprovals.md#getApproverWithHttpInfo) | **GET** /deploymentApprovals/getApprovers |  |
| [**getDefinitionsForUser**](DeploymentApprovals.md#getDefinitionsForUser) | **GET** /deploymentApprovals/getDefinitions |  |
| [**getDefinitionsForUserWithHttpInfo**](DeploymentApprovals.md#getDefinitionsForUserWithHttpInfo) | **GET** /deploymentApprovals/getDefinitions |  |
| [**requestApproval**](DeploymentApprovals.md#requestApproval) | **POST** /deploymentApprovals/requestApproval |  |
| [**requestApprovalWithHttpInfo**](DeploymentApprovals.md#requestApprovalWithHttpInfo) | **POST** /deploymentApprovals/requestApproval |  |
| [**submitApproval**](DeploymentApprovals.md#submitApproval) | **POST** /deploymentApprovals/submitApproval |  |
| [**submitApprovalWithHttpInfo**](DeploymentApprovals.md#submitApprovalWithHttpInfo) | **POST** /deploymentApprovals/submitApproval |  |



## addApprover

> ResponseDTOApprover addApprover(approver)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        Approver approver = new Approver(); // Approver | 
        try {
            ResponseDTOApprover result = apiInstance.addApprover(approver);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#addApprover");
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
| **approver** | [**Approver**](Approver.md)|  | |

### Return type

[**ResponseDTOApprover**](ResponseDTOApprover.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## addApproverWithHttpInfo

> ApiResponse<ResponseDTOApprover> addApprover addApproverWithHttpInfo(approver)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        Approver approver = new Approver(); // Approver | 
        try {
            ApiResponse<ResponseDTOApprover> response = apiInstance.addApproverWithHttpInfo(approver);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#addApprover");
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
| **approver** | [**Approver**](Approver.md)|  | |

### Return type

ApiResponse<[**ResponseDTOApprover**](ResponseDTOApprover.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## deleteApprover

> ResponseDTOApprover deleteApprover(approverId, approverEmail)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        String approverId = "approverId_example"; // String | 
        String approverEmail = "approverEmail_example"; // String | 
        try {
            ResponseDTOApprover result = apiInstance.deleteApprover(approverId, approverEmail);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#deleteApprover");
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
| **approverId** | **String**|  | |
| **approverEmail** | **String**|  | |

### Return type

[**ResponseDTOApprover**](ResponseDTOApprover.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## deleteApproverWithHttpInfo

> ApiResponse<ResponseDTOApprover> deleteApprover deleteApproverWithHttpInfo(approverId, approverEmail)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        String approverId = "approverId_example"; // String | 
        String approverEmail = "approverEmail_example"; // String | 
        try {
            ApiResponse<ResponseDTOApprover> response = apiInstance.deleteApproverWithHttpInfo(approverId, approverEmail);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#deleteApprover");
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
| **approverId** | **String**|  | |
| **approverEmail** | **String**|  | |

### Return type

ApiResponse<[**ResponseDTOApprover**](ResponseDTOApprover.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getApprovals

> PageDeploymentApproval getApprovals(approvedState, page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        String approvedState = "REQUESTED"; // String | 
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            PageDeploymentApproval result = apiInstance.getApprovals(approvedState, page, size);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#getApprovals");
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
| **approvedState** | **String**|  | [enum: REQUESTED, ACCEPTED, REJECTED] |
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |

### Return type

[**PageDeploymentApproval**](PageDeploymentApproval.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getApprovalsWithHttpInfo

> ApiResponse<PageDeploymentApproval> getApprovals getApprovalsWithHttpInfo(approvedState, page, size)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        String approvedState = "REQUESTED"; // String | 
        Integer page = 0; // Integer | 
        Integer size = 10; // Integer | 
        try {
            ApiResponse<PageDeploymentApproval> response = apiInstance.getApprovalsWithHttpInfo(approvedState, page, size);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#getApprovals");
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
| **approvedState** | **String**|  | [enum: REQUESTED, ACCEPTED, REJECTED] |
| **page** | **Integer**|  | [optional] [default to 0] |
| **size** | **Integer**|  | [optional] [default to 10] |

### Return type

ApiResponse<[**PageDeploymentApproval**](PageDeploymentApproval.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getApprovalsByReviewer

> ResponseDTOListDeploymentApproval getApprovalsByReviewer(stateCSV)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        String stateCSV = "REQUESTED"; // String | 
        try {
            ResponseDTOListDeploymentApproval result = apiInstance.getApprovalsByReviewer(stateCSV);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#getApprovalsByReviewer");
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
| **stateCSV** | **String**|  | [optional] [default to REQUESTED] |

### Return type

[**ResponseDTOListDeploymentApproval**](ResponseDTOListDeploymentApproval.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getApprovalsByReviewerWithHttpInfo

> ApiResponse<ResponseDTOListDeploymentApproval> getApprovalsByReviewer getApprovalsByReviewerWithHttpInfo(stateCSV)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        String stateCSV = "REQUESTED"; // String | 
        try {
            ApiResponse<ResponseDTOListDeploymentApproval> response = apiInstance.getApprovalsByReviewerWithHttpInfo(stateCSV);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#getApprovalsByReviewer");
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
| **stateCSV** | **String**|  | [optional] [default to REQUESTED] |

### Return type

ApiResponse<[**ResponseDTOListDeploymentApproval**](ResponseDTOListDeploymentApproval.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getApprover

> ResponseDTOApprover getApprover(processId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        String processId = "processId_example"; // String | 
        try {
            ResponseDTOApprover result = apiInstance.getApprover(processId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#getApprover");
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
| **processId** | **String**|  | |

### Return type

[**ResponseDTOApprover**](ResponseDTOApprover.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getApproverWithHttpInfo

> ApiResponse<ResponseDTOApprover> getApprover getApproverWithHttpInfo(processId)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        String processId = "processId_example"; // String | 
        try {
            ApiResponse<ResponseDTOApprover> response = apiInstance.getApproverWithHttpInfo(processId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#getApprover");
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
| **processId** | **String**|  | |

### Return type

ApiResponse<[**ResponseDTOApprover**](ResponseDTOApprover.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getDefinitionsForUser

> ResponseDTOListApprover getDefinitionsForUser()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        try {
            ResponseDTOListApprover result = apiInstance.getDefinitionsForUser();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#getDefinitionsForUser");
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

[**ResponseDTOListApprover**](ResponseDTOListApprover.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getDefinitionsForUserWithHttpInfo

> ApiResponse<ResponseDTOListApprover> getDefinitionsForUser getDefinitionsForUserWithHttpInfo()



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        try {
            ApiResponse<ResponseDTOListApprover> response = apiInstance.getDefinitionsForUserWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#getDefinitionsForUser");
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

ApiResponse<[**ResponseDTOListApprover**](ResponseDTOListApprover.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## requestApproval

> ResponseDTODeploymentApproval requestApproval(deploymentApprovalRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        DeploymentApprovalRequest deploymentApprovalRequest = new DeploymentApprovalRequest(); // DeploymentApprovalRequest | 
        try {
            ResponseDTODeploymentApproval result = apiInstance.requestApproval(deploymentApprovalRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#requestApproval");
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
| **deploymentApprovalRequest** | [**DeploymentApprovalRequest**](DeploymentApprovalRequest.md)|  | |

### Return type

[**ResponseDTODeploymentApproval**](ResponseDTODeploymentApproval.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## requestApprovalWithHttpInfo

> ApiResponse<ResponseDTODeploymentApproval> requestApproval requestApprovalWithHttpInfo(deploymentApprovalRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        DeploymentApprovalRequest deploymentApprovalRequest = new DeploymentApprovalRequest(); // DeploymentApprovalRequest | 
        try {
            ApiResponse<ResponseDTODeploymentApproval> response = apiInstance.requestApprovalWithHttpInfo(deploymentApprovalRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#requestApproval");
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
| **deploymentApprovalRequest** | [**DeploymentApprovalRequest**](DeploymentApprovalRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTODeploymentApproval**](ResponseDTODeploymentApproval.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## submitApproval

> ResponseDTODeploymentApproval submitApproval(deploymentActionRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        DeploymentActionRequest deploymentActionRequest = new DeploymentActionRequest(); // DeploymentActionRequest | 
        try {
            ResponseDTODeploymentApproval result = apiInstance.submitApproval(deploymentActionRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#submitApproval");
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
| **deploymentActionRequest** | [**DeploymentActionRequest**](DeploymentActionRequest.md)|  | |

### Return type

[**ResponseDTODeploymentApproval**](ResponseDTODeploymentApproval.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## submitApprovalWithHttpInfo

> ApiResponse<ResponseDTODeploymentApproval> submitApproval submitApprovalWithHttpInfo(deploymentActionRequest)



### Example

```java
// Import classes:
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.ApiException;
import io.telekom.orchest.client.invoker.ApiResponse;
import io.telekom.orchest.client.invoker.Configuration;
import io.telekom.orchest.client.invoker.models.*;
import io.telekom.orchest.client.api.DeploymentApprovals;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:6200/orchest");

        DeploymentApprovals apiInstance = new DeploymentApprovals(defaultClient);
        DeploymentActionRequest deploymentActionRequest = new DeploymentActionRequest(); // DeploymentActionRequest | 
        try {
            ApiResponse<ResponseDTODeploymentApproval> response = apiInstance.submitApprovalWithHttpInfo(deploymentActionRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DeploymentApprovals#submitApproval");
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
| **deploymentActionRequest** | [**DeploymentActionRequest**](DeploymentActionRequest.md)|  | |

### Return type

ApiResponse<[**ResponseDTODeploymentApproval**](ResponseDTODeploymentApproval.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

