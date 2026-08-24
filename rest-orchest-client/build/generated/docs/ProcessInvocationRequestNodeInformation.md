

# ProcessInvocationRequestNodeInformation

## oneOf schemas
* [ActivityNode](ActivityNode.md)
* [BusinessRuleTaskNode](BusinessRuleTaskNode.md)
* [CallActivityNode](CallActivityNode.md)
* [EventNode](EventNode.md)
* [GatewayNode](GatewayNode.md)
* [ManualTaskNode](ManualTaskNode.md)
* [ReceiveTaskNode](ReceiveTaskNode.md)
* [ScriptTaskNode](ScriptTaskNode.md)
* [SendTaskNode](SendTaskNode.md)
* [ServiceTaskNode](ServiceTaskNode.md)
* [SubProcessNode](SubProcessNode.md)
* [TaskNode](TaskNode.md)
* [UserTaskNode](UserTaskNode.md)

## Example
```java
// Import classes:
import io.telekom.orchest.client.model.ProcessInvocationRequestNodeInformation;
import io.telekom.orchest.client.model.ActivityNode;
import io.telekom.orchest.client.model.BusinessRuleTaskNode;
import io.telekom.orchest.client.model.CallActivityNode;
import io.telekom.orchest.client.model.EventNode;
import io.telekom.orchest.client.model.GatewayNode;
import io.telekom.orchest.client.model.ManualTaskNode;
import io.telekom.orchest.client.model.ReceiveTaskNode;
import io.telekom.orchest.client.model.ScriptTaskNode;
import io.telekom.orchest.client.model.SendTaskNode;
import io.telekom.orchest.client.model.ServiceTaskNode;
import io.telekom.orchest.client.model.SubProcessNode;
import io.telekom.orchest.client.model.TaskNode;
import io.telekom.orchest.client.model.UserTaskNode;

public class Example {
    public static void main(String[] args) {
        ProcessInvocationRequestNodeInformation exampleProcessInvocationRequestNodeInformation = new ProcessInvocationRequestNodeInformation();

        // create a new ActivityNode
        ActivityNode exampleActivityNode = new ActivityNode();
        // set ProcessInvocationRequestNodeInformation to ActivityNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleActivityNode);
        // to get back the ActivityNode set earlier
        ActivityNode testActivityNode = (ActivityNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new BusinessRuleTaskNode
        BusinessRuleTaskNode exampleBusinessRuleTaskNode = new BusinessRuleTaskNode();
        // set ProcessInvocationRequestNodeInformation to BusinessRuleTaskNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleBusinessRuleTaskNode);
        // to get back the BusinessRuleTaskNode set earlier
        BusinessRuleTaskNode testBusinessRuleTaskNode = (BusinessRuleTaskNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new CallActivityNode
        CallActivityNode exampleCallActivityNode = new CallActivityNode();
        // set ProcessInvocationRequestNodeInformation to CallActivityNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleCallActivityNode);
        // to get back the CallActivityNode set earlier
        CallActivityNode testCallActivityNode = (CallActivityNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new EventNode
        EventNode exampleEventNode = new EventNode();
        // set ProcessInvocationRequestNodeInformation to EventNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleEventNode);
        // to get back the EventNode set earlier
        EventNode testEventNode = (EventNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new GatewayNode
        GatewayNode exampleGatewayNode = new GatewayNode();
        // set ProcessInvocationRequestNodeInformation to GatewayNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleGatewayNode);
        // to get back the GatewayNode set earlier
        GatewayNode testGatewayNode = (GatewayNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new ManualTaskNode
        ManualTaskNode exampleManualTaskNode = new ManualTaskNode();
        // set ProcessInvocationRequestNodeInformation to ManualTaskNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleManualTaskNode);
        // to get back the ManualTaskNode set earlier
        ManualTaskNode testManualTaskNode = (ManualTaskNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new ReceiveTaskNode
        ReceiveTaskNode exampleReceiveTaskNode = new ReceiveTaskNode();
        // set ProcessInvocationRequestNodeInformation to ReceiveTaskNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleReceiveTaskNode);
        // to get back the ReceiveTaskNode set earlier
        ReceiveTaskNode testReceiveTaskNode = (ReceiveTaskNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new ScriptTaskNode
        ScriptTaskNode exampleScriptTaskNode = new ScriptTaskNode();
        // set ProcessInvocationRequestNodeInformation to ScriptTaskNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleScriptTaskNode);
        // to get back the ScriptTaskNode set earlier
        ScriptTaskNode testScriptTaskNode = (ScriptTaskNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new SendTaskNode
        SendTaskNode exampleSendTaskNode = new SendTaskNode();
        // set ProcessInvocationRequestNodeInformation to SendTaskNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleSendTaskNode);
        // to get back the SendTaskNode set earlier
        SendTaskNode testSendTaskNode = (SendTaskNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new ServiceTaskNode
        ServiceTaskNode exampleServiceTaskNode = new ServiceTaskNode();
        // set ProcessInvocationRequestNodeInformation to ServiceTaskNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleServiceTaskNode);
        // to get back the ServiceTaskNode set earlier
        ServiceTaskNode testServiceTaskNode = (ServiceTaskNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new SubProcessNode
        SubProcessNode exampleSubProcessNode = new SubProcessNode();
        // set ProcessInvocationRequestNodeInformation to SubProcessNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleSubProcessNode);
        // to get back the SubProcessNode set earlier
        SubProcessNode testSubProcessNode = (SubProcessNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new TaskNode
        TaskNode exampleTaskNode = new TaskNode();
        // set ProcessInvocationRequestNodeInformation to TaskNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleTaskNode);
        // to get back the TaskNode set earlier
        TaskNode testTaskNode = (TaskNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();

        // create a new UserTaskNode
        UserTaskNode exampleUserTaskNode = new UserTaskNode();
        // set ProcessInvocationRequestNodeInformation to UserTaskNode
        exampleProcessInvocationRequestNodeInformation.setActualInstance(exampleUserTaskNode);
        // to get back the UserTaskNode set earlier
        UserTaskNode testUserTaskNode = (UserTaskNode) exampleProcessInvocationRequestNodeInformation.getActualInstance();
    }
}
```


