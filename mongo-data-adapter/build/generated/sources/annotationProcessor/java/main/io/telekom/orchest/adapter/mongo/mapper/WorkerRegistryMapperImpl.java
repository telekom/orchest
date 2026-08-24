package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.WorkerRegistry;
import io.telekom.orchest.api.core.request.WorkerRegistryRequest;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-24T17:18:18+0530",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.4 (Azul Systems, Inc.)"
)
@Component
public class WorkerRegistryMapperImpl implements WorkerRegistryMapper {

    @Override
    public WorkerRegistry toDocument(io.telekom.orchest.api.core.adapters.data.model.WorkerRegistry domain) {
        if ( domain == null ) {
            return null;
        }

        WorkerRegistry.WorkerRegistryBuilder workerRegistry = WorkerRegistry.builder();

        workerRegistry.id( domain.getId() );
        workerRegistry.nameSpace( domain.getNameSpace() );
        workerRegistry.processDefinitionId( domain.getProcessDefinitionId() );
        workerRegistry.lastRegisteredAt( domain.getLastRegisteredAt() );
        Set<WorkerRegistryRequest.WorkerInfo> set = domain.getWorkerInfos();
        if ( set != null ) {
            workerRegistry.workerInfos( new LinkedHashSet<WorkerRegistryRequest.WorkerInfo>( set ) );
        }

        return workerRegistry.build();
    }

    @Override
    public io.telekom.orchest.api.core.adapters.data.model.WorkerRegistry toDomain(WorkerRegistry document) {
        if ( document == null ) {
            return null;
        }

        io.telekom.orchest.api.core.adapters.data.model.WorkerRegistry.WorkerRegistryBuilder workerRegistry = io.telekom.orchest.api.core.adapters.data.model.WorkerRegistry.builder();

        workerRegistry.id( document.getId() );
        workerRegistry.nameSpace( document.getNameSpace() );
        workerRegistry.processDefinitionId( document.getProcessDefinitionId() );
        workerRegistry.lastRegisteredAt( document.getLastRegisteredAt() );
        Set<WorkerRegistryRequest.WorkerInfo> set = document.getWorkerInfos();
        if ( set != null ) {
            workerRegistry.workerInfos( new LinkedHashSet<WorkerRegistryRequest.WorkerInfo>( set ) );
        }

        return workerRegistry.build();
    }
}
