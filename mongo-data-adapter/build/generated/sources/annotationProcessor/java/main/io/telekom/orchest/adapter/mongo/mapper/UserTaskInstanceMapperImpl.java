package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-24T17:01:48+0530",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.4 (Azul Systems, Inc.)"
)
@Component
public class UserTaskInstanceMapperImpl implements UserTaskInstanceMapper {

    @Override
    public io.telekom.orchest.adapter.mongo.model.UserTaskInstance toDocument(UserTaskInstance domain) {
        if ( domain == null ) {
            return null;
        }

        io.telekom.orchest.adapter.mongo.model.UserTaskInstance.UserTaskInstanceBuilder userTaskInstance = io.telekom.orchest.adapter.mongo.model.UserTaskInstance.builder();

        userTaskInstance.id( domain.getId() );
        userTaskInstance.taskId( domain.getTaskId() );
        userTaskInstance.processInstanceId( domain.getProcessInstanceId() );
        userTaskInstance.processDefinitionId( domain.getProcessDefinitionId() );
        userTaskInstance.activityId( domain.getActivityId() );
        userTaskInstance.taskName( domain.getTaskName() );
        userTaskInstance.assignee( domain.getAssignee() );
        List<String> list = domain.getCandidateUsers();
        if ( list != null ) {
            userTaskInstance.candidateUsers( new ArrayList<String>( list ) );
        }
        List<String> list1 = domain.getCandidateGroups();
        if ( list1 != null ) {
            userTaskInstance.candidateGroups( new ArrayList<String>( list1 ) );
        }
        userTaskInstance.claimedBy( domain.getClaimedBy() );
        userTaskInstance.dueDate( domain.getDueDate() );
        userTaskInstance.followUpDate( domain.getFollowUpDate() );
        userTaskInstance.formKey( domain.getFormKey() );
        Map<String, Object> map = domain.getVariables();
        if ( map != null ) {
            userTaskInstance.variables( new LinkedHashMap<String, Object>( map ) );
        }
        userTaskInstance.createdAt( domain.getCreatedAt() );
        userTaskInstance.claimedAt( domain.getClaimedAt() );
        userTaskInstance.completedAt( domain.getCompletedAt() );

        userTaskInstance.state( domain.getState() != null ? domain.getState().name() : null );

        return userTaskInstance.build();
    }

    @Override
    public UserTaskInstance toDomain(io.telekom.orchest.adapter.mongo.model.UserTaskInstance document) {
        if ( document == null ) {
            return null;
        }

        UserTaskInstance.UserTaskInstanceBuilder userTaskInstance = UserTaskInstance.builder();

        userTaskInstance.id( document.getId() );
        userTaskInstance.taskId( document.getTaskId() );
        userTaskInstance.processInstanceId( document.getProcessInstanceId() );
        userTaskInstance.processDefinitionId( document.getProcessDefinitionId() );
        userTaskInstance.activityId( document.getActivityId() );
        userTaskInstance.taskName( document.getTaskName() );
        userTaskInstance.assignee( document.getAssignee() );
        List<String> list = document.getCandidateUsers();
        if ( list != null ) {
            userTaskInstance.candidateUsers( new ArrayList<String>( list ) );
        }
        List<String> list1 = document.getCandidateGroups();
        if ( list1 != null ) {
            userTaskInstance.candidateGroups( new ArrayList<String>( list1 ) );
        }
        userTaskInstance.claimedBy( document.getClaimedBy() );
        userTaskInstance.dueDate( document.getDueDate() );
        userTaskInstance.followUpDate( document.getFollowUpDate() );
        userTaskInstance.formKey( document.getFormKey() );
        Map<String, Object> map = document.getVariables();
        if ( map != null ) {
            userTaskInstance.variables( new LinkedHashMap<String, Object>( map ) );
        }
        userTaskInstance.createdAt( document.getCreatedAt() );
        userTaskInstance.claimedAt( document.getClaimedAt() );
        userTaskInstance.completedAt( document.getCompletedAt() );

        userTaskInstance.state( document.getState() != null ? io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance.TaskState.valueOf(document.getState()) : null );

        return userTaskInstance.build();
    }
}
