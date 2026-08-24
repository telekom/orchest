package io.telekom.orchest.adapter.mongo.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OrchestMongoProperties} verifying default values and setter round-trips.
 */
class MongoModulePropertiesTest {

  @Test
  @DisplayName("defaults for timeouts, region, and auto index")
  void defaults() {
    OrchestMongoProperties p = new OrchestMongoProperties();
    assertFalse(p.isAutoIndexCreation());
    assertEquals(60, p.getConnectTimeoutSeconds());
    assertEquals(60, p.getReadTimeoutSeconds());
    assertEquals("eu-central-1", p.getAwsRegion());
  }

  @Test
  @DisplayName("setters round-trip")
  void setters() {
    OrchestMongoProperties p = new OrchestMongoProperties();
    p.setUri("mongodb://localhost");
    p.setDatabaseName("orchest");
    p.setIamRoleArn("role-1");
    p.setAutoIndexCreation(false);
    p.setConnectTimeoutSeconds(30);
    p.setReadTimeoutSeconds(45);
    p.setAwsRegion("us-east-1");

    assertEquals("mongodb://localhost", p.getUri());
    assertEquals("orchest", p.getDatabaseName());
    assertEquals("role-1", p.getIamRoleArn());
    assertFalse(p.isAutoIndexCreation());
    assertEquals(30, p.getConnectTimeoutSeconds());
    assertEquals(45, p.getReadTimeoutSeconds());
    assertEquals("us-east-1", p.getAwsRegion());
  }
}
