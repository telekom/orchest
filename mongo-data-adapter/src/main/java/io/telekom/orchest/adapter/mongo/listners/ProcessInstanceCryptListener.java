package io.telekom.orchest.adapter.mongo.listners;

import com.fasterxml.jackson.core.type.TypeReference;
import io.telekom.orchest.adapter.mongo.config.StringHashingObjectMapper;
import io.telekom.orchest.adapter.mongo.model.ProcessInstance;
import io.telekom.orchest.api.core.adapters.cipher.CipherType;
import io.telekom.orchest.api.core.adapters.cipher.ICipherService;
import io.telekom.orchest.api.core.utils.JsonMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

/**
 * MongoDB event listener that encrypts process instance variables before persistence and decrypts
 * them after reads and saves, ensuring variables are stored encrypted at rest.
 */
@Slf4j
@RequiredArgsConstructor
public class ProcessInstanceCryptListener extends AbstractMongoEventListener<ProcessInstance> {

  private final ICipherService cipherService;
  private final StringHashingObjectMapper stringHashingObjectMapper;

  // Triggers before save
  @Override
  public void onBeforeConvert(BeforeConvertEvent<ProcessInstance> event) {
    Map<String, Object> variables = event.getSource().getVariables();
    try {
      String variableString = JsonMapper.writeToJson(variables);
      event.getSource().setEncVariables(cipherService.cipher(CipherType.ENCRYPT, variableString));
      //
      // event.getSource().setSearchableVariables(stringHashingObjectMapper.getMapper().writeValueAsString(variables));
      event.getSource().setVariables(null);
    } catch (Exception e) {
      log.error(
          "failed to encrypt processInstance: {}", event.getSource().getProcessInstanceId(), e);
    }
  }

  @Override
  public void onAfterSave(AfterSaveEvent<ProcessInstance> event) {
    try {
      event
          .getSource()
          .setVariables(
              JsonMapper.readFromJson(
                  cipherService.cipher(CipherType.DECRYPT, event.getSource().getEncVariables()),
                  new TypeReference<>() {}));
    } catch (Exception e) {
      log.error(
          "failed to decrypt processInstance after save: {}",
          event.getSource().getProcessInstanceId(),
          e);
      // retry
      try {
        event
            .getSource()
            .setVariables(
                JsonMapper.readFromJson(
                    cipherService.cipher(CipherType.DECRYPT, event.getSource().getEncVariables()),
                    new TypeReference<>() {}));
      } catch (Exception e1) {
        log.error(
            "failed to decrypt processInstance after save in retry: {}",
            event.getSource().getProcessInstanceId(),
            e1);
      }
    }
  }

  // Triggers on Read
  @Override
  public void onAfterConvert(AfterConvertEvent<ProcessInstance> event) {
    String encVariables = event.getSource().getEncVariables();
    if (encVariables == null) return;
    try {
      event
          .getSource()
          .setVariables(
              JsonMapper.readFromJson(
                  cipherService.cipher(CipherType.DECRYPT, encVariables),
                  new TypeReference<>() {}));
    } catch (Exception e) {
      // retry
      log.error(
          "failed to decrypt processInstance: {}", event.getSource().getProcessInstanceId(), e);
      try {
        event
            .getSource()
            .setVariables(
                JsonMapper.readFromJson(
                    cipherService.cipher(CipherType.DECRYPT, encVariables),
                    new TypeReference<>() {}));
      } catch (Exception e1) {
        log.error(
            "failed to decrypt processInstance in retry: {}",
            event.getSource().getProcessInstanceId(),
            e1);
        throw e1;
      }
    }
  }
}
