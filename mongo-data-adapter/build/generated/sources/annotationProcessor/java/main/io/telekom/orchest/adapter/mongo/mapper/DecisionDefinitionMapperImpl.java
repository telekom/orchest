package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.DecisionDefinition;
import io.telekom.orchest.api.core.model.dmn.Decision;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-24T17:08:50+0530",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.4 (Azul Systems, Inc.)"
)
@Component
public class DecisionDefinitionMapperImpl implements DecisionDefinitionMapper {

    @Override
    public DecisionDefinition toDocument(io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition domain) {
        if ( domain == null ) {
            return null;
        }

        DecisionDefinition.DecisionDefinitionBuilder decisionDefinition = DecisionDefinition.builder();

        decisionDefinition.id( domain.getId() );
        decisionDefinition.definitionId( domain.getDefinitionId() );
        List<String> list = domain.getDecisionIds();
        if ( list != null ) {
            decisionDefinition.decisionIds( new ArrayList<String>( list ) );
        }
        decisionDefinition.version( domain.getVersion() );
        decisionDefinition.name( domain.getName() );
        decisionDefinition.namespace( domain.getNamespace() );
        decisionDefinition.definitionXML( domain.getDefinitionXML() );
        Map<String, Decision> map = domain.getDecisions();
        if ( map != null ) {
            decisionDefinition.decisions( new LinkedHashMap<String, Decision>( map ) );
        }
        decisionDefinition.createdAt( domain.getCreatedAt() );

        return decisionDefinition.build();
    }

    @Override
    public io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition toDomain(DecisionDefinition document) {
        if ( document == null ) {
            return null;
        }

        io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition decisionDefinition = new io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition();

        decisionDefinition.setId( document.getId() );
        decisionDefinition.setDefinitionId( document.getDefinitionId() );
        List<String> list = document.getDecisionIds();
        if ( list != null ) {
            decisionDefinition.setDecisionIds( new ArrayList<String>( list ) );
        }
        decisionDefinition.setVersion( document.getVersion() );
        decisionDefinition.setName( document.getName() );
        decisionDefinition.setNamespace( document.getNamespace() );
        decisionDefinition.setDefinitionXML( document.getDefinitionXML() );
        Map<String, Decision> map = document.getDecisions();
        if ( map != null ) {
            decisionDefinition.setDecisions( new LinkedHashMap<String, Decision>( map ) );
        }
        decisionDefinition.setCreatedAt( document.getCreatedAt() );

        return decisionDefinition;
    }
}
