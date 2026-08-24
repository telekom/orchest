package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.api.core.adapters.data.model.SignalEvent;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-24T17:18:18+0530",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.4 (Azul Systems, Inc.)"
)
@Component
public class SignalEventMapperImpl implements SignalEventMapper {

    @Override
    public io.telekom.orchest.adapter.mongo.model.SignalEvent toDocument(SignalEvent domain) {
        if ( domain == null ) {
            return null;
        }

        io.telekom.orchest.adapter.mongo.model.SignalEvent.SignalEventBuilder signalEvent = io.telekom.orchest.adapter.mongo.model.SignalEvent.builder();

        signalEvent.id( domain.getId() );
        signalEvent.signalId( domain.getSignalId() );
        signalEvent.signalName( domain.getSignalName() );
        signalEvent.state( domain.getState() );
        signalEvent.processInstanceId( domain.getProcessInstanceId() );
        signalEvent.processDefinitionId( domain.getProcessDefinitionId() );
        signalEvent.nodeInformation( domain.getNodeInformation() );
        signalEvent.isStartEvent( domain.getIsStartEvent() );
        signalEvent.linkedEventId( domain.getLinkedEventId() );
        signalEvent.createdAt( domain.getCreatedAt() );

        return signalEvent.build();
    }

    @Override
    public SignalEvent toDomain(io.telekom.orchest.adapter.mongo.model.SignalEvent document) {
        if ( document == null ) {
            return null;
        }

        SignalEvent.SignalEventBuilder signalEvent = SignalEvent.builder();

        signalEvent.id( document.getId() );
        signalEvent.signalId( document.getSignalId() );
        signalEvent.signalName( document.getSignalName() );
        signalEvent.state( document.getState() );
        signalEvent.processInstanceId( document.getProcessInstanceId() );
        signalEvent.processDefinitionId( document.getProcessDefinitionId() );
        signalEvent.nodeInformation( document.getNodeInformation() );
        signalEvent.isStartEvent( document.getIsStartEvent() );
        signalEvent.linkedEventId( document.getLinkedEventId() );
        signalEvent.createdAt( document.getCreatedAt() );

        return signalEvent.build();
    }
}
