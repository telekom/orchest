package de.telekom.solutions.kmsclient.api.utils;

import com.fasterxml.jackson.databind.SerializerProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.CollectionUtils;

public class SerDerUtils {

  private SerDerUtils() {
    throw new IllegalStateException("Serialization & Deserialization Utility  class");
  }

  public static boolean pathMatched(
      Object currentClass,
      SerializerProvider provider,
      Map<String, Map<String, String>> encryptionPathMap) {
    String classFullName = currentClass.getClass().getName();
    String currentPropertyName = provider.getGenerator().getOutputContext().getCurrentName();
    return pathMatched(classFullName, currentPropertyName, encryptionPathMap);
  }

  public static boolean pathMatched(
      String classFullName,
      String currentPropertyName,
      Map<String, Map<String, String>> encryptionPathMap) {
    String currentClassField = classFullName + ":" + currentPropertyName;
    String encryptAllField = classFullName + ":" + "*";
    return encryptionPathMap != null
        && (encryptionPathMap.containsKey(currentClassField)
            || encryptionPathMap.containsKey(encryptAllField));
  }

  public static Map<String, Map<String, String>> cipherPathsMap(List<String> maskedFieldPaths) {
    if (CollectionUtils.isEmpty(maskedFieldPaths)) return Collections.emptyMap();

    // "{package}.{class}:{feild_to_mask}?{query}"
    // eg.
    // de.telekom.pom.businessprocess.acquisition.api.model.productorder.ProductOfferingConfigRef:secret?id=10&type=DEBIT
    Map<String, Map<String, String>> cipherPathMap = new HashMap<>();
    maskedFieldPaths.forEach(
        path -> {
          String[] splits = path.split("\\?");
          if (splits.length == 2) {
            Map<String, String> queryMap = new HashMap<>();
            String[] querySplits = splits[1].split("&"); //  quer1, quer2, queryN...
            Arrays.stream(querySplits)
                .forEach(
                    query -> {
                      String[] queryKeyValue = query.split("=");
                      queryMap.put(queryKeyValue[0], queryKeyValue[1]);
                    });
            cipherPathMap.put(splits[0], queryMap);
          } else cipherPathMap.put(splits[0], null);
        });
    return cipherPathMap;
  }
}
