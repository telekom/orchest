package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.UserTaskInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between the domain UserTaskInstance and the MongoDB document
 * model.
 */
@Mapper(componentModel = "spring")
public interface UserTaskInstanceMapper {

  @Mapping(
      target = "state",
      expression = "java(domain.getState() != null ? domain.getState().name() : null)")
  UserTaskInstance toDocument(
      io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance domain);

  @Mapping(
      target = "state",
      expression =
          "java(document.getState() != null ? io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance.TaskState.valueOf(document.getState()) : null)")
  io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance toDomain(
      UserTaskInstance document);
}
