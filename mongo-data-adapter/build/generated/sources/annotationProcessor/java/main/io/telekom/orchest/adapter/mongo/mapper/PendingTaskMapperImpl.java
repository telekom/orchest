package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.PendingTask;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-24T17:08:50+0530",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.4 (Azul Systems, Inc.)"
)
@Component
public class PendingTaskMapperImpl implements PendingTaskMapper {

    @Override
    public PendingTask toDocument(io.telekom.orchest.api.core.adapters.data.model.PendingTask domain) {
        if ( domain == null ) {
            return null;
        }

        PendingTask.PendingTaskBuilder pendingTask = PendingTask.builder();

        pendingTask.id( domain.getId() );
        pendingTask.processDefinitionId( domain.getProcessDefinitionId() );
        pendingTask.processInstanceId( domain.getProcessInstanceId() );
        pendingTask.workerId( domain.getWorkerId() );
        pendingTask.workerEventRequest( domain.getWorkerEventRequest() );
        pendingTask.createdAt( domain.getCreatedAt() );
        pendingTask.completedAt( domain.getCompletedAt() );

        return pendingTask.build();
    }

    @Override
    public io.telekom.orchest.api.core.adapters.data.model.PendingTask toDomain(PendingTask document) {
        if ( document == null ) {
            return null;
        }

        io.telekom.orchest.api.core.adapters.data.model.PendingTask.PendingTaskBuilder pendingTask = io.telekom.orchest.api.core.adapters.data.model.PendingTask.builder();

        pendingTask.id( document.getId() );
        pendingTask.processDefinitionId( document.getProcessDefinitionId() );
        pendingTask.processInstanceId( document.getProcessInstanceId() );
        pendingTask.workerId( document.getWorkerId() );
        pendingTask.workerEventRequest( document.getWorkerEventRequest() );
        pendingTask.createdAt( document.getCreatedAt() );
        pendingTask.completedAt( document.getCompletedAt() );

        return pendingTask.build();
    }
}
