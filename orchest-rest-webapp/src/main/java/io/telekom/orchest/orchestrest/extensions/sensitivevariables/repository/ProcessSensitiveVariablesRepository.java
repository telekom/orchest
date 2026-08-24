package io.telekom.orchest.orchestrest.extensions.sensitivevariables.repository;

import io.telekom.orchest.orchestrest.extensions.sensitivevariables.model.ProcessSensitiveVariables;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

/** Spring Data MongoDB repository for process sensitive variable configurations. */
@Component
public interface ProcessSensitiveVariablesRepository
    extends MongoRepository<ProcessSensitiveVariables, String> {

  Optional<ProcessSensitiveVariables> findByProcessDefinitionId(String processDefinitionId);
}
