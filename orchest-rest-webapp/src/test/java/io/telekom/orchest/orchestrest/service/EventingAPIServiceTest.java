package io.telekom.orchest.orchestrest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.telekom.orchest.api.core.request.MessageEventRequest;
import io.telekom.orchest.api.core.request.SignalEventRequest;
import io.telekom.orchest.api.core.response.MessageEventResponse;
import io.telekom.orchest.api.core.response.SignalEventResponse;
import io.telekom.orchest.orchestrest.api.request.SendMessageEventRequest;
import io.telekom.orchest.orchestrest.api.request.SendSignalEventRequest;
import io.telekom.orchest.orchestrest.event.EventProducer;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link io.telekom.orchest.orchestrest.service.EventingAPIService}. */
@ExtendWith(MockitoExtension.class)
class EventingAPIServiceTest {

  @Mock private EventProducer eventProducer;

  @InjectMocks private EventingAPIService service;

  @Nested
  @DisplayName("publishMessage")
  class PublishMessage {

    @Test
    @DisplayName("should send MessageEventRequest to Kafka and return response metadata")
    void publishMessage_ok() {
      SendMessageEventRequest req = new SendMessageEventRequest();
      req.setMessageName("ORDER_PAID");
      req.setCorrelationKey("ord-1");
      req.setVariables(Map.of("amount", 99));

      MessageEventResponse response = service.publishMessage(req);

      assertThat(response.getMessageName()).isEqualTo("ORDER_PAID");
      assertThat(response.getCorrelationKey()).isEqualTo("ord-1");
      assertThat(response.getId()).isNotBlank();

      ArgumentCaptor<MessageEventRequest> captor =
          ArgumentCaptor.forClass(MessageEventRequest.class);
      verify(eventProducer).sendMessageEvent(captor.capture());
      assertThat(captor.getValue().getMessageName()).isEqualTo("ORDER_PAID");
      assertThat(captor.getValue().getCorrelationKey()).isEqualTo("ord-1");
      assertThat(captor.getValue().getVariables().getVariables()).containsEntry("amount", 99);
    }

    @Test
    @DisplayName("should use empty variables map when null")
    void publishMessage_nullVariables() {
      SendMessageEventRequest req = new SendMessageEventRequest();
      req.setMessageName("M");
      req.setCorrelationKey("c");

      service.publishMessage(req);

      ArgumentCaptor<MessageEventRequest> captor =
          ArgumentCaptor.forClass(MessageEventRequest.class);
      verify(eventProducer).sendMessageEvent(captor.capture());
      assertThat(captor.getValue().getVariables().getVariables()).isEmpty();
    }
  }

  @Nested
  @DisplayName("publishSignal")
  class PublishSignal {

    @Test
    @DisplayName("should send SignalEventRequest to Kafka and return response metadata")
    void publishSignal_ok() {
      SendSignalEventRequest req = new SendSignalEventRequest();
      req.setSignalName("INVENTORY_UPDATED");
      req.setVariables(Map.of("sku", "x"));

      SignalEventResponse response = service.publishSignal(req);

      assertThat(response.getSignalName()).isEqualTo("INVENTORY_UPDATED");
      assertThat(response.getId()).isNotBlank();

      ArgumentCaptor<SignalEventRequest> captor = ArgumentCaptor.forClass(SignalEventRequest.class);
      verify(eventProducer).sendSignalEvent(captor.capture());
      assertThat(captor.getValue().getSignalName()).isEqualTo("INVENTORY_UPDATED");
      assertThat(captor.getValue().getVariables().getVariables()).containsEntry("sku", "x");
    }

    @Test
    @DisplayName("should use empty variables map when null")
    void publishSignal_nullVariables() {
      SendSignalEventRequest req = new SendSignalEventRequest();
      req.setSignalName("S");

      service.publishSignal(req);

      ArgumentCaptor<SignalEventRequest> captor = ArgumentCaptor.forClass(SignalEventRequest.class);
      verify(eventProducer).sendSignalEvent(captor.capture());
      assertThat(captor.getValue().getVariables().getVariables()).isEmpty();
    }
  }
}
