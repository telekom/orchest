# OrchesT REST Client

Pure Java client library for the [OrchesT](https://github.com/platform/orchest-v2) REST API. Built with **OkHttp** and **Gson**; no Spring or other framework dependencies. Use it from plain Java, Spring Boot, Ktor, or any JVM environment.

## Features

- **Framework-agnostic**: No Spring, Feign, or Servlet dependency
- **OkHttp + Gson**: Minimal, well-understood stack
- **Full API coverage**: All operations from the OrchesT OpenAPI spec (process instances, user tasks, variables, rate limits, deployment approvals, decisions, connectors, etc.)
- **Java 21+** with Jakarta EE annotations

## Dependency

### Gradle (Kotlin DSL)

```kotlin
implementation("io.telekom.orchest:rest-orchest-client:2.0.7.x")
```

### Gradle (Groovy)

```groovy
implementation 'io.telekom.orchest:rest-orchest-client:2.0.7.x'
```

### Maven

```xml
<dependency>
    <groupId>io.telekom.orchest</groupId>
    <artifactId>rest-orchest-client</artifactId>
    <version>2.0.7.x</version>
</dependency>
```

---

## Usage

### Plain Java

```java
import io.telekom.orchest.client.OrchesTClient;
import io.telekom.orchest.client.api.ProcessInstanceApi;
import io.telekom.orchest.client.model.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String baseUrl = "https://api-orchest.example.com/orchest";
        OrchesTClient client = OrchesTClient.create(baseUrl);

        // Create a process instance
        ProcessInvocationRequest request = new ProcessInvocationRequest();
        request.setProcessDefinitionId("my-process");
        request.setVariables(Map.of("key", "value"));

        ResponseDTOProcessInvocationResponse response =
            client.getProcessInstanceApi().createProcessInstance(request);
        String processInstanceId = response.getData().getProcessInstanceId();

        // Get user tasks, variables, etc.
        var userTasks = client.getUserTasksApi().getMyTasks();
        var variables = client.getVariablesApi().getVariables(processInstanceId);
    }
}
```

With a custom OkHttpClient (timeouts, logging, auth):

```java
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

OkHttpClient httpClient = new OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build();

OrchesTClient client = OrchesTClient.create(httpClient, "https://api-orchest.example.com/orchest");
```

**Token auth injector (all APIs):** Use `TokenAuthInjector` to add a Bearer token or custom header to every request:

```java
import io.telekom.orchest.client.OrchesTClient;
import io.telekom.orchest.client.TokenAuthInjector;

// Bearer token (static)
OrchesTClient client = OrchesTClient.create(
    "https://api-orchest.example.com/orchest",
    TokenAuthInjector.bearer("your-jwt-token")
);

// Bearer token from supplier (e.g. refreshable OAuth token)
OrchesTClient client = OrchesTClient.create(
    baseUrl,
    TokenAuthInjector.bearer(() -> tokenService.getAccessToken())
);

// Custom header (e.g. X-Api-Key)
OrchesTClient client = OrchesTClient.create(
    baseUrl,
    TokenAuthInjector.header("X-Api-Key", apiKey)
);
```

Using the low-level `ApiClient` for auth (e.g. Bearer token):

```java
import io.telekom.orchest.client.invoker.ApiClient;
import io.telekom.orchest.client.invoker.auth.HttpBearerAuth;

ApiClient apiClient = new ApiClient();
apiClient.setBasePath("https://api-orchest.example.com/orchest");
HttpBearerAuth auth = (HttpBearerAuth) apiClient.getAuthentication("bearerAuth");
auth.setBearerToken("your-jwt-token");

OrchesTClient client = OrchesTClient.create(apiClient);
```

---

### Spring Boot

Add the dependency and create a bean. No Spring-specific code inside the client library; you just wire it in your configuration.

```java
import io.telekom.orchest.client.OrchesTClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrchesTClientConfig {

    @Value("${orchest.api.base-url:http://localhost:6200/orchest}")
    private String orchestBaseUrl;

    @Bean
    public OrchesTClient orchesTClient() {
        return OrchesTClient.create(orchestBaseUrl);
    }
}
```

Then inject and use:

```java
@Service
public class MyProcessService {

    private final OrchesTClient orchesTClient;

    public MyProcessService(OrchesTClient orchesTClient) {
        this.orchesTClient = orchesTClient;
    }

    public void startProcess(String processId, Map<String, Object> variables) throws Exception {
        ProcessInvocationRequest req = new ProcessInvocationRequest();
        req.setProcessDefinitionId(processId);
        req.setVariables(variables);
        orchesTClient.getProcessInstanceApi().createProcessInstance(req);
    }
}
```

Optional: custom OkHttpClient with Spring-managed timeouts:

```java
@Bean
public OrchesTClient orchesTClient(
        @Value("${orchest.api.base-url}") String baseUrl,
        @Value("${orchest.api.connect-timeout-sec:10}") int connectTimeout,
        @Value("${orchest.api.read-timeout-sec:30}") int readTimeout) {
    OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
        .readTimeout(readTimeout, TimeUnit.SECONDS)
        .build();
    return OrchesTClient.create(httpClient, baseUrl);
}
```

---

### Ktor

Use the client inside Ktor application or in a Ktor-based service. No Ktor dependency inside the library; just add `rest-orchest-client` and use it.

```kotlin
import io.telekom.orchest.client.OrchesTClient

fun Application.configureOrchest() {
    val baseUrl = environment.config.property("orchest.baseUrl").getString()
    val orchesTClient = OrchesTClient.create(baseUrl)

    routing {
        get("/start-process") {
            val processId = call.request.queryParameters["processId"] ?: return@get
            val request = ProcessInvocationRequest().apply {
                processDefinitionId = processId
                variables = emptyMap()
            }
            val response = orchesTClient.processInstanceApi.createProcessInstance(request)
            call.respond(response.data.processInstanceId)
        }
    }
}
```

In Kotlin you can use the same Java API; for overloads and nullable types the generated models follow Java conventions.

---

## API surface

The client exposes one facade and per-domain API classes:

| Accessor | Description |
|----------|-------------|
| `getApiClient()` | Low-level OkHttp-backed `ApiClient` (base URL, auth, interceptors) |
| `getProcessInstanceApi()` | Create, cancel, retry, list process instances |
| `getProcessDefinitionApi()` | Process definitions, deploy, list IDs |
| `getProcessDefinitionEnvironmentApi()` | Process env variables (get/add/update/delete) |
| `getUserTasksApi()` | User tasks: list, get, claim, complete, unclaim, reassign |
| `getVariablesApi()` | Get/modify process variables |
| `getRateLimitApi()` | Rate limit CRUD and list |
| `getDeploymentApprovalsApi()` | Deployment approvals and approvers |
| `getDecisionDefinitionApi()` | Decision definitions and evaluate |
| `getDecisionInstanceApi()` | Decision instances |
| `getConnectorsApi()` | Connectors |
| `getTestControllerApi()` | Test endpoint (if available) |

All methods throw `io.telekom.orchest.client.invoker.ApiException` on HTTP errors; check status code and response body as needed.

---

## Building from source

```bash
./gradlew :rest-orchest-client:build
```

Generated code is produced by [OpenAPI Generator](https://openapi-generator.tech) (OkHttp + Gson) from `src/main/resources/orchest-api.yml` and is included in the build output.

---

## License

Proprietary – Deutsche Telekom AG.
