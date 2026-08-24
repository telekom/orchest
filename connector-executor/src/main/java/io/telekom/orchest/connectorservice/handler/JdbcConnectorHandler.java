package io.telekom.orchest.connectorservice.handler;

import static io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils.getDataMappingValue;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ConnectorException;
import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.enginecore.bpmn.utils.VariablesUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JDBC connector handler. Executes a SQL statement against a relational database.
 *
 * <p>Note: the matching JDBC driver must be present on the runtime classpath (e.g. PostgreSQL,
 * MySQL, MariaDB, MSSQL, Oracle).
 */
@Slf4j
@Component
public class JdbcConnectorHandler implements ConnectorHandler {

  private static final String DEFAULT_RESULT_VARIABLE = "jdbcResponse";

  private static final Map<String, String> JDBC_SCHEME =
      Map.of(
          "MARIADB", "mariadb",
          "MSSQL", "sqlserver",
          "MYSQL", "mysql",
          "POSTGRESQL", "postgresql",
          "ORACLE", "oracle:thin");

  @Override
  public String connectorType() {
    return "io.orchest.connector-jdbc:1";
  }

  @Override
  public String errorCode() {
    return "JDBC_CONNECTOR_ERROR";
  }

  @Override
  public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
    List<DataMapping> inputMappings = node.getInputMappings();
    List<DataMapping> outputMappings = node.getOutputMappings();

    String query = evaluate("data.query", inputMappings, instance);
    if (query == null || query.isBlank()) {
      throw new ConnectorException("SQL query is required for JDBC connector");
    }
    boolean returnResults =
        Boolean.parseBoolean(
            getDataMappingValue("data.returnResults", inputMappings)
                .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
                .map(Object::toString)
                .orElse("false"));
    Object variables =
        getDataMappingValue("data.variables", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .orElse(null);

    String resultVariable =
        getDataMappingValue("resultVariable", outputMappings).orElse(DEFAULT_RESULT_VARIABLE);

    String jdbcUrl = resolveJdbcUrl(inputMappings, instance);
    String username = evaluate("connection.username", inputMappings, instance);
    String password = evaluate("connection.password", inputMappings, instance);

    try (Connection connection = openConnection(jdbcUrl, username, password);
        PreparedStatement statement = connection.prepareStatement(query)) {

      bindParameters(statement, variables);

      Map<String, Object> responseData = new HashMap<>();
      if (returnResults) {
        try (ResultSet resultSet = statement.executeQuery()) {
          List<Map<String, Object>> rows = mapResultSet(resultSet);
          responseData.put("resultSet", rows);
          responseData.put("rowCount", rows.size());
        }
      } else {
        int modifiedRows = statement.executeUpdate();
        responseData.put("modifiedRows", modifiedRows);
      }
      log.info("JDBC connector executed successfully for node: {}", node.getName());
      return Map.of(resultVariable, responseData);
    } catch (SQLException e) {
      throw new ConnectorException("JDBC connector execution failed: " + e.getMessage(), e);
    }
  }

  private String resolveJdbcUrl(List<DataMapping> inputMappings, ProcessInstance instance) {
    String authType =
        getDataMappingValue("connection.authType", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse("uri");

    if ("detailed".equalsIgnoreCase(authType)) {
      String database = evaluate("database", inputMappings, instance);
      String host = evaluate("connection.host", inputMappings, instance);
      String port = evaluate("connection.port", inputMappings, instance);
      String databaseName = evaluate("connection.databaseName", inputMappings, instance);
      if (database == null || host == null) {
        throw new ConnectorException(
            "database and connection.host are required for detailed JDBC connection");
      }
      String scheme = JDBC_SCHEME.getOrDefault(database.toUpperCase(), database.toLowerCase());
      StringBuilder url = new StringBuilder("jdbc:").append(scheme).append("://").append(host);
      if (port != null && !port.isBlank()) {
        url.append(':').append(port);
      }
      if (databaseName != null && !databaseName.isBlank()) {
        url.append('/').append(databaseName);
      }
      return url.toString();
    }

    String uri = evaluate("connection.uri", inputMappings, instance);
    if (uri == null || uri.isBlank()) {
      throw new ConnectorException("connection.uri is required for JDBC connector");
    }
    return uri;
  }

  private Connection openConnection(String jdbcUrl, String username, String password)
      throws SQLException {
    Properties props = new Properties();
    if (username != null) {
      props.put("user", username);
    }
    if (password != null) {
      props.put("password", password);
    }
    return DriverManager.getConnection(jdbcUrl, props);
  }

  private void bindParameters(PreparedStatement statement, Object variables) throws SQLException {
    if (variables instanceof List<?> positional) {
      for (int i = 0; i < positional.size(); i++) {
        statement.setObject(i + 1, positional.get(i));
      }
    } else if (variables instanceof Map<?, ?>) {
      // Named parameters are not supported by plain JDBC PreparedStatement positional binding.
      log.debug("Named JDBC parameters provided; expected positional list. Skipping binding.");
    }
  }

  private List<Map<String, Object>> mapResultSet(ResultSet resultSet) throws SQLException {
    List<Map<String, Object>> rows = new ArrayList<>();
    ResultSetMetaData metaData = resultSet.getMetaData();
    int columnCount = metaData.getColumnCount();
    while (resultSet.next()) {
      Map<String, Object> row = new HashMap<>();
      for (int i = 1; i <= columnCount; i++) {
        row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
      }
      rows.add(row);
    }
    return rows;
  }

  private String evaluate(String key, List<DataMapping> inputMappings, ProcessInstance instance) {
    return getDataMappingValue(key, inputMappings)
        .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
        .map(Object::toString)
        .orElse(null);
  }
}
