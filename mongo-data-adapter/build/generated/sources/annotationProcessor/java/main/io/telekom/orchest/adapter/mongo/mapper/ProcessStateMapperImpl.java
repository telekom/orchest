package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.api.core.adapters.data.model.ProcessState;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-24T17:18:18+0530",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.4 (Azul Systems, Inc.)"
)
@Component
public class ProcessStateMapperImpl implements ProcessStateMapper {

    @Override
    public io.telekom.orchest.adapter.mongo.model.ProcessState toDocument(ProcessState domain) {
        if ( domain == null ) {
            return null;
        }

        io.telekom.orchest.adapter.mongo.model.ProcessState processState = new io.telekom.orchest.adapter.mongo.model.ProcessState();

        processState.setId( domain.getId() );
        processState.setProcessId( domain.getProcessId() );
        processState.setStatus( domain.isStatus() );

        return processState;
    }

    @Override
    public ProcessState toDomain(io.telekom.orchest.adapter.mongo.model.ProcessState document) {
        if ( document == null ) {
            return null;
        }

        ProcessState processState = new ProcessState();

        processState.setId( document.getId() );
        processState.setProcessId( document.getProcessId() );
        processState.setStatus( document.isStatus() );

        return processState;
    }
}
