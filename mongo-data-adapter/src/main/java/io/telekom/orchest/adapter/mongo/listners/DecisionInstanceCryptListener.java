package io.telekom.orchest.adapter.mongo.listners;

import com.fasterxml.jackson.core.type.TypeReference;
import io.telekom.orchest.adapter.mongo.model.DecisionInstance;
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
 * MongoDB event listener that encrypts decision instance input/output variables before persistence
 * and decrypts them after reads and saves, ensuring variables are stored encrypted at rest.
 */
@Slf4j
@RequiredArgsConstructor
public class DecisionInstanceCryptListener extends AbstractMongoEventListener<DecisionInstance> {

  private final ICipherService cipherService;

  // Triggers before save
  @Override
  public void onBeforeConvert(BeforeConvertEvent<DecisionInstance> event) {
    Map<String, Object> inputVariables = event.getSource().getInputVariables();
    Map<String, Object> outputVariables = event.getSource().getOutputVariables();
    try {
      String inputVariableString = JsonMapper.writeToJson(inputVariables);
      String outputVariableString = JsonMapper.writeToJson(outputVariables);
      event
          .getSource()
          .setEncInputVariables(cipherService.cipher(CipherType.ENCRYPT, inputVariableString));
      event
          .getSource()
          .setEncOutputVariables(cipherService.cipher(CipherType.ENCRYPT, outputVariableString));
      event.getSource().setInputVariables(null);
      event.getSource().setOutputVariables(null);
    } catch (Exception e) {
      log.error(
          "failed to encrypt processInstance: {}", event.getSource().getProcessInstanceId(), e);
    }
  }

  @Override
  public void onAfterSave(AfterSaveEvent<DecisionInstance> event) {
    try {
      event
          .getSource()
          .setInputVariables(
              JsonMapper.readFromJson(
                  cipherService.cipher(
                      CipherType.DECRYPT, event.getSource().getEncInputVariables()),
                  new TypeReference<>() {}));
      event
          .getSource()
          .setOutputVariables(
              JsonMapper.readFromJson(
                  cipherService.cipher(
                      CipherType.DECRYPT, event.getSource().getEncOutputVariables()),
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
            .setInputVariables(
                JsonMapper.readFromJson(
                    cipherService.cipher(
                        CipherType.DECRYPT, event.getSource().getEncInputVariables()),
                    new TypeReference<>() {}));
        event
            .getSource()
            .setOutputVariables(
                JsonMapper.readFromJson(
                    cipherService.cipher(
                        CipherType.DECRYPT, event.getSource().getEncOutputVariables()),
                    new TypeReference<>() {}));
      } catch (Exception e1) {
        log.error(
            "failed to decrypt decisionInstance after save in retry: {}",
            event.getSource().getProcessInstanceId(),
            e1);
      }
    }
  }

  // Triggers on Read
  @Override
  public void onAfterConvert(AfterConvertEvent<DecisionInstance> event) {
    String encInputVariables = event.getSource().getEncInputVariables();
    String encOutputVariables = event.getSource().getEncOutputVariables();
    if (encInputVariables == null) return;
    try {
      event
          .getSource()
          .setInputVariables(
              JsonMapper.readFromJson(
                  cipherService.cipher(CipherType.DECRYPT, encInputVariables),
                  new TypeReference<>() {}));
      event
          .getSource()
          .setOutputVariables(
              JsonMapper.readFromJson(
                  cipherService.cipher(CipherType.DECRYPT, encOutputVariables),
                  new TypeReference<>() {}));
    } catch (Exception e) {
      // retry
      log.error(
          "failed to decrypt processInstance: {}", event.getSource().getProcessInstanceId(), e);
      try {
        event
            .getSource()
            .setInputVariables(
                JsonMapper.readFromJson(
                    cipherService.cipher(CipherType.DECRYPT, encInputVariables),
                    new TypeReference<>() {}));
        event
            .getSource()
            .setOutputVariables(
                JsonMapper.readFromJson(
                    cipherService.cipher(CipherType.DECRYPT, encOutputVariables),
                    new TypeReference<>() {}));
      } catch (Exception e1) {
        log.error(
            "failed to decrypt decisionInstance in retry: {}",
            event.getSource().getProcessInstanceId(),
            e1);
        throw e1;
      }
    }
  }
}
