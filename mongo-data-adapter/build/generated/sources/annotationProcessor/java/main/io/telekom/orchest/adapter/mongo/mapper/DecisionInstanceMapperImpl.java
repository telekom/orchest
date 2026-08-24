package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.DecisionInstance;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-24T17:03:00+0530",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.4 (Azul Systems, Inc.)"
)
@Component
public class DecisionInstanceMapperImpl implements DecisionInstanceMapper {

    @Override
    public DecisionInstance toDocument(io.telekom.orchest.api.core.adapters.data.model.DecisionInstance domain) {
        if ( domain == null ) {
            return null;
        }

        DecisionInstance.DecisionInstanceBuilder decisionInstance = DecisionInstance.builder();

        decisionInstance.decisionInstanceId( domain.getDecisionInstanceId() );
        decisionInstance.definitionId( domain.getDefinitionId() );
        decisionInstance.processInstanceId( domain.getProcessInstanceId() );
        decisionInstance.version( domain.getVersion() );
        decisionInstance.resourceXMLUTF8String( domain.getResourceXMLUTF8String() );
        Map<String, Object> map = domain.getInputVariables();
        if ( map != null ) {
            decisionInstance.inputVariables( new LinkedHashMap<String, Object>( map ) );
        }
        Map<String, Object> map1 = domain.getOutputVariables();
        if ( map1 != null ) {
            decisionInstance.outputVariables( new LinkedHashMap<String, Object>( map1 ) );
        }
        List<String> list = domain.getMatchedRuleIds();
        if ( list != null ) {
            decisionInstance.matchedRuleIds( new ArrayList<String>( list ) );
        }
        decisionInstance.encInputVariables( domain.getEncInputVariables() );
        decisionInstance.encOutputVariables( domain.getEncOutputVariables() );
        decisionInstance.state( domain.getState() );
        decisionInstance.executedAt( domain.getExecutedAt() );

        return decisionInstance.build();
    }

    @Override
    public io.telekom.orchest.api.core.adapters.data.model.DecisionInstance toDomain(DecisionInstance document) {
        if ( document == null ) {
            return null;
        }

        io.telekom.orchest.api.core.adapters.data.model.DecisionInstance.DecisionInstanceBuilder decisionInstance = io.telekom.orchest.api.core.adapters.data.model.DecisionInstance.builder();

        decisionInstance.decisionInstanceId( document.getDecisionInstanceId() );
        decisionInstance.definitionId( document.getDefinitionId() );
        decisionInstance.processInstanceId( document.getProcessInstanceId() );
        decisionInstance.version( document.getVersion() );
        decisionInstance.resourceXMLUTF8String( document.getResourceXMLUTF8String() );
        Map<String, Object> map = document.getInputVariables();
        if ( map != null ) {
            decisionInstance.inputVariables( new LinkedHashMap<String, Object>( map ) );
        }
        Map<String, Object> map1 = document.getOutputVariables();
        if ( map1 != null ) {
            decisionInstance.outputVariables( new LinkedHashMap<String, Object>( map1 ) );
        }
        decisionInstance.encInputVariables( document.getEncInputVariables() );
        decisionInstance.encOutputVariables( document.getEncOutputVariables() );
        List<String> list = document.getMatchedRuleIds();
        if ( list != null ) {
            decisionInstance.matchedRuleIds( new ArrayList<String>( list ) );
        }
        decisionInstance.state( document.getState() );
        decisionInstance.executedAt( document.getExecutedAt() );

        return decisionInstance.build();
    }
}
