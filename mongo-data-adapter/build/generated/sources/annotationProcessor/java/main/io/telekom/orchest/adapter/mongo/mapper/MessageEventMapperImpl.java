package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.MessageEventStore;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-24T17:08:50+0530",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.4 (Azul Systems, Inc.)"
)
@Component
public class MessageEventMapperImpl implements MessageEventMapper {

    @Override
    public MessageEventStore toDocument(io.telekom.orchest.api.core.adapters.data.model.MessageEventStore domain) {
        if ( domain == null ) {
            return null;
        }

        MessageEventStore.MessageEventStoreBuilder messageEventStore = MessageEventStore.builder();

        messageEventStore.id( domain.getId() );
        messageEventStore.correlationKey( domain.getCorrelationKey() );
        messageEventStore.messageName( domain.getMessageName() );
        messageEventStore.state( domain.getState() );
        messageEventStore.processInstanceId( domain.getProcessInstanceId() );
        messageEventStore.processDefinitionId( domain.getProcessDefinitionId() );
        messageEventStore.nodeInformation( domain.getNodeInformation() );
        messageEventStore.isStartEvent( domain.getIsStartEvent() );
        messageEventStore.linkedEventId( domain.getLinkedEventId() );
        messageEventStore.createdAt( domain.getCreatedAt() );

        return messageEventStore.build();
    }

    @Override
    public io.telekom.orchest.api.core.adapters.data.model.MessageEventStore toDomain(MessageEventStore document) {
        if ( document == null ) {
            return null;
        }

        io.telekom.orchest.api.core.adapters.data.model.MessageEventStore.MessageEventStoreBuilder messageEventStore = io.telekom.orchest.api.core.adapters.data.model.MessageEventStore.builder();

        messageEventStore.id( document.getId() );
        messageEventStore.correlationKey( document.getCorrelationKey() );
        messageEventStore.messageName( document.getMessageName() );
        messageEventStore.state( document.getState() );
        messageEventStore.processInstanceId( document.getProcessInstanceId() );
        messageEventStore.processDefinitionId( document.getProcessDefinitionId() );
        messageEventStore.nodeInformation( document.getNodeInformation() );
        messageEventStore.isStartEvent( document.getIsStartEvent() );
        messageEventStore.linkedEventId( document.getLinkedEventId() );
        messageEventStore.createdAt( document.getCreatedAt() );

        return messageEventStore.build();
    }
}
