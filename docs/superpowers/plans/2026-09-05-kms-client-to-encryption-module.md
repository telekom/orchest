# KMS Client → Encryption Module Migration Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the external `de.telekom.solutions:kms-client:1.0.1` Artifactory dependency with a local `encryption` module containing the same source code.

**Architecture:** Copy the 14 source files from the external KMS client into a new `encryption` submodule, keeping the original `de.telekom.solutions.kmsclient` package name so that zero Java imports need to change across the 15 consuming files in 6 modules. The module gets wired as a project dependency in place of the external JAR.

**Tech Stack:** Java 21, Spring Boot (BOM-managed), AWS Encryption SDK, AWS KMS SDK, Jackson, Lombok, Gradle multi-module

**Spec:** User request — inline this conversation

## Global Constraints

- Package name stays `de.telekom.solutions.kmsclient` — zero import changes in consumers
- Java 21, UTF-8
- Follow orchest Gradle conventions (java-library plugin, Spring Boot BOM, no standalone version properties file)
- Root `build.gradle` subprojects block already provides: Lombok, spring-boot-configuration-processor, JUnit, Mockito, Spotless, JaCoCo

---

### Task 1: Create the `encryption` module

**Files:**
- Create: `encryption/build.gradle`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/EnableKMS.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/KMSProperties.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/KMSConfiguration.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/CryptoService.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/StaticCryptoService.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/KMSClientBuilderService.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/api/CipherType.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/api/annotations/Cipher.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/api/utils/AWSBuilderUtils.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/api/utils/SerDerUtils.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/api/json/serializers/AnnotationCipherSerializer.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/api/json/serializers/JsonPathCipherSerializer.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/api/json/deserializers/AnnotationCipherDeserializer.java`
- Create: `encryption/src/main/java/de/telekom/solutions/kmsclient/api/json/deserializers/JsonPathCipherDeserializer.java`
- Create: `encryption/src/main/resources/META-INF/spring.factories`

**Interfaces:**
- Produces: Gradle module `:encryption` exposing `api "software.amazon.awssdk:kms"` and all classes under `de.telekom.solutions.kmsclient`

- [ ] **Step 1: Create `encryption/build.gradle`**

```groovy
apply plugin: 'java-library'
apply plugin: 'io.spring.dependency-management'

dependencyManagement {
    imports {
        mavenBom "org.springframework.boot:spring-boot-dependencies:${springBootVersion}"
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'

    implementation 'com.amazonaws:aws-encryption-sdk-java:2.4.1'
    api 'software.amazon.awssdk:kms:2.24.0'
    implementation 'software.amazon.awssdk:sts:2.24.0'

    implementation 'com.fasterxml.jackson.core:jackson-core'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
}
```

Note: Lombok, spring-boot-configuration-processor, and test deps are inherited from root `build.gradle` subprojects block.

- [ ] **Step 2: Copy all 14 source files from KMS client**

Copy every file from `/Users/mohitsingh/Documents/DT/alfa/Alfa commons/kms-client/src/` into `encryption/src/`, preserving the full directory structure. No modifications — the package name and all code stays identical.

Source → Destination mapping:
```
{KMS}/src/main/java/de/telekom/solutions/kmsclient/*.java      → encryption/src/main/java/de/telekom/solutions/kmsclient/*.java
{KMS}/src/main/java/de/telekom/solutions/kmsclient/api/**/*.java → encryption/src/main/java/de/telekom/solutions/kmsclient/api/**/*.java
{KMS}/src/main/resources/META-INF/spring.factories              → encryption/src/main/resources/META-INF/spring.factories
```

- [ ] **Step 3: Verify directory structure**

Run: `find encryption/src -type f | sort`

Expected: 14 `.java` files + 1 `spring.factories` = 15 files total.

---

### Task 2: Wire the module into the project

**Files:**
- Modify: `settings.gradle` (add 1 line)
- Modify: `kafka-event-adapter/build.gradle:30` (replace 1 line)
- Modify: `spring-orchest-client/build.gradle:36` (replace 1 line)

**Interfaces:**
- Consumes: `:encryption` module from Task 1

- [ ] **Step 1: Add module to `settings.gradle`**

Add `include 'encryption'` under the `// Core Modules` section:

```
// Core Modules
include 'core'
include 'engine-core'
include 'encryption'       ← add this line
include 'telemetry'
```

- [ ] **Step 2: Update `kafka-event-adapter/build.gradle`**

Replace line 30:
```groovy
// OLD:
api "de.telekom.solutions:kms-client:1.0.1"

// NEW:
api project(':encryption')
```

- [ ] **Step 3: Update `spring-orchest-client/build.gradle`**

Replace line 36:
```groovy
// OLD:
api "de.telekom.solutions:kms-client:1.0.1"

// NEW:
api project(':encryption')
```

- [ ] **Step 4: Commit**

```bash
git add encryption/ settings.gradle kafka-event-adapter/build.gradle spring-orchest-client/build.gradle
git commit -m "feat(encryption): inline kms-client as local module

Replace external de.telekom.solutions:kms-client:1.0.1 Artifactory dependency
with a local encryption module containing the same source code.
Package name preserved — zero import changes in consuming modules."
```

---

### Task 3: Build verification

- [ ] **Step 1: Compile the full project**

Run: `./gradlew compileJava`

Expected: BUILD SUCCESSFUL — all 6 consuming modules (kafka-event-adapter, spring-orchest-client, orchest-engine, orchest-rest-webapp, connector-executor, sentinel) resolve imports from the local `:encryption` module.

- [ ] **Step 2: Run tests**

Run: `./gradlew test`

Expected: All existing tests pass. The orchest-engine and orchest-rest-webapp test files that import KMS classes should compile and run unchanged.

- [ ] **Step 3: Verify no residual external dependency**

Run: `grep -r "de.telekom.solutions:kms-client" --include="*.gradle" .`

Expected: Zero matches — the external JAR reference is fully removed.

---

## Migration summary

| What | Count |
|------|-------|
| New files created | 16 (1 build.gradle + 14 Java + 1 spring.factories) |
| Files modified | 3 (settings.gradle + 2 build.gradle) |
| Java files requiring import changes | **0** (package name preserved) |
| External dependency references removed | 2 |
