package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.ProcessDefinition;
import io.telekom.orchest.api.core.model.bpmn.SequenceFlow;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-24T17:08:50+0530",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.4 (Azul Systems, Inc.)"
)
@Component
public class ProcessDefinitionMapperImpl implements ProcessDefinitionMapper {

    @Override
    public ProcessDefinition toDocument(io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition domain) {
        if ( domain == null ) {
            return null;
        }

        ProcessDefinition.ProcessDefinitionBuilder<?, ?> processDefinition = ProcessDefinition.builder();

        processDefinition.id( domain.getId() );
        processDefinition.definitionId( domain.getDefinitionId() );
        processDefinition.version( domain.getVersion() );
        processDefinition.name( domain.getName() );
        Map<String, BaseNode> map = domain.getNodes();
        if ( map != null ) {
            processDefinition.nodes( new LinkedHashMap<String, BaseNode>( map ) );
        }
        Map<String, SequenceFlow> map1 = domain.getSequenceFlows();
        if ( map1 != null ) {
            processDefinition.sequenceFlows( new LinkedHashMap<String, SequenceFlow>( map1 ) );
        }
        processDefinition.definitionXML( domain.getDefinitionXML() );
        processDefinition.startNodeId( domain.getStartNodeId() );
        processDefinition.isExecutable( domain.getIsExecutable() );
        processDefinition.createdAt( domain.getCreatedAt() );

        return processDefinition.build();
    }

    @Override
    public io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition toDomain(ProcessDefinition document) {
        if ( document == null ) {
            return null;
        }

        io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition.ProcessDefinitionBuilder<?, ?> processDefinition = io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition.builder();

        processDefinition.id( document.getId() );
        processDefinition.definitionId( document.getDefinitionId() );
        processDefinition.version( document.getVersion() );
        processDefinition.name( document.getName() );
        Map<String, BaseNode> map = document.getNodes();
        if ( map != null ) {
            processDefinition.nodes( new LinkedHashMap<String, BaseNode>( map ) );
        }
        Map<String, SequenceFlow> map1 = document.getSequenceFlows();
        if ( map1 != null ) {
            processDefinition.sequenceFlows( new LinkedHashMap<String, SequenceFlow>( map1 ) );
        }
        processDefinition.definitionXML( document.getDefinitionXML() );
        processDefinition.startNodeId( document.getStartNodeId() );
        processDefinition.isExecutable( document.getIsExecutable() );
        processDefinition.createdAt( document.getCreatedAt() );

        return processDefinition.build();
    }
}
