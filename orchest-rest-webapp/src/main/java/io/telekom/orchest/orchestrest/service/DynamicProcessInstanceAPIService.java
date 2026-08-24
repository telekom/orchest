package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.api.core.response.DynamicProcessInvocationResponse;
import io.telekom.orchest.api.core.utils.IDGenerator;
import io.telekom.orchest.orchestrest.event.EventProducer;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Service for creating process instances from dynamic (ad-hoc) BPMN definitions. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicProcessInstanceAPIService {

  private final EventProducer eventProducer;

  public DynamicProcessInvocationResponse createProcessInstance(
      DynamicProcessInvocationRequest processInvocationRequest) {
    if (processInvocationRequest.getProcessInstanceId() == null)
      processInvocationRequest.setProcessInstanceId(IDGenerator.generate());
    eventProducer.sendDynamicProcessInvocationEvent(processInvocationRequest);
    return new DynamicProcessInvocationResponse(processInvocationRequest.getProcessInstanceId());
  }
}
