package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-24T17:08:50+0530",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.4 (Azul Systems, Inc.)"
)
@Component
public class ProcessInstanceMapperImpl implements ProcessInstanceMapper {

    @Override
    public ProcessInstance toDocument(io.telekom.orchest.api.core.adapters.data.model.ProcessInstance domain) {
        if ( domain == null ) {
            return null;
        }

        ProcessInstance.ProcessInstanceBuilder processInstance = ProcessInstance.builder();

        processInstance.id( domain.getId() );
        processInstance.processInstanceId( domain.getProcessInstanceId() );
        processInstance.processDefinitionId( domain.getProcessDefinitionId() );
        processInstance.version( domain.getVersion() );
        Map<String, Object> map = domain.getVariables();
        if ( map != null ) {
            processInstance.variables( new LinkedHashMap<String, Object>( map ) );
        }
        Set<String> set = domain.getActiveNodeIds();
        if ( set != null ) {
            processInstance.activeNodeIds( new LinkedHashSet<String>( set ) );
        }
        Map<String, ExecutionLogEntry> map1 = domain.getExecutionHistory();
        if ( map1 != null ) {
            processInstance.executionHistory( new LinkedHashMap<String, ExecutionLogEntry>( map1 ) );
        }
        Map<String, Object> map2 = domain.getExecutionState();
        if ( map2 != null ) {
            processInstance.executionState( new LinkedHashMap<String, Object>( map2 ) );
        }
        processInstance.hasIncident( domain.isHasIncident() );
        processInstance.dynamicFlow( domain.isDynamicFlow() );
        processInstance.incidentMessage( domain.getIncidentMessage() );
        processInstance.incidentSourceInstanceId( domain.getIncidentSourceInstanceId() );
        processInstance.completed( domain.isCompleted() );
        processInstance.parentProcesActivity( domain.getParentProcesActivity() );
        processInstance.state( domain.getState() );
        List<String> list = domain.getCorrelationIds();
        if ( list != null ) {
            processInstance.correlationIds( new ArrayList<String>( list ) );
        }
        processInstance.createdAt( domain.getCreatedAt() );
        processInstance.completedAt( domain.getCompletedAt() );
        processInstance.lastModifiedAt( domain.getLastModifiedAt() );

        return processInstance.build();
    }

    @Override
    public io.telekom.orchest.api.core.adapters.data.model.ProcessInstance toDomain(ProcessInstance document) {
        if ( document == null ) {
            return null;
        }

        String processInstanceId = null;
        String processDefinitionId = null;
        Integer version = null;

        processInstanceId = document.getProcessInstanceId();
        processDefinitionId = document.getProcessDefinitionId();
        version = document.getVersion();

        io.telekom.orchest.api.core.adapters.data.model.ProcessInstance processInstance = new io.telekom.orchest.api.core.adapters.data.model.ProcessInstance( processInstanceId, processDefinitionId, version );

        processInstance.setId( document.getId() );
        Map<String, Object> map = document.getVariables();
        if ( map != null ) {
            processInstance.setVariables( new LinkedHashMap<String, Object>( map ) );
        }
        Set<String> set = document.getActiveNodeIds();
        if ( set != null ) {
            processInstance.setActiveNodeIds( new LinkedHashSet<String>( set ) );
        }
        Map<String, ExecutionLogEntry> map1 = document.getExecutionHistory();
        if ( map1 != null ) {
            processInstance.setExecutionHistory( new LinkedHashMap<String, ExecutionLogEntry>( map1 ) );
        }
        Map<String, Object> map2 = document.getExecutionState();
        if ( map2 != null ) {
            processInstance.setExecutionState( new LinkedHashMap<String, Object>( map2 ) );
        }
        processInstance.setHasIncident( document.isHasIncident() );
        processInstance.setIncidentMessage( document.getIncidentMessage() );
        processInstance.setIncidentSourceInstanceId( document.getIncidentSourceInstanceId() );
        processInstance.setCompleted( document.isCompleted() );
        processInstance.setState( document.getState() );
        List<String> list = document.getCorrelationIds();
        if ( list != null ) {
            processInstance.setCorrelationIds( new ArrayList<String>( list ) );
        }
        processInstance.setParentProcesActivity( document.getParentProcesActivity() );
        processInstance.setDynamicFlow( document.isDynamicFlow() );
        processInstance.setCreatedAt( document.getCreatedAt() );
        processInstance.setCompletedAt( document.getCompletedAt() );
        processInstance.setLastModifiedAt( document.getLastModifiedAt() );

        return processInstance;
    }
}
