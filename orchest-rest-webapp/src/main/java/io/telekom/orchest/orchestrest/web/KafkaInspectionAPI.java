package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.ClusterInfo;
import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.ProcessHealth;
import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.WorkerInfo;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.service.KafkaInspectionService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IKafkaInspectionAPI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for Kafka topic and consumer-group inspection. */
@Component
@RequiredArgsConstructor
public class KafkaInspectionAPI implements IKafkaInspectionAPI {

  private final KafkaInspectionService kafkaInspectionService;

  @Override
  public Mono<ResponseDTO<WorkerInfo>> getWorkerInfo(String processDefinitionId) {
    return Mono.fromCallable(() -> kafkaInspectionService.getProcessInfo(processDefinitionId))
        .subscribeOn(Schedulers.boundedElastic())
        .map(RestUtils::success);
  }

  @Override
  public Mono<ResponseDTO<ClusterInfo>> getClusterInfo() {
    return Mono.fromCallable(kafkaInspectionService::getClusterInfo)
        .subscribeOn(Schedulers.boundedElastic())
        .map(RestUtils::success);
  }

  @Override
  public Mono<ResponseDTO<List<ProcessHealth>>> getProcessHealth() {
    return Mono.fromCallable(kafkaInspectionService::getProcessHealth)
        .subscribeOn(Schedulers.boundedElastic())
        .map(RestUtils::success);
  }
}
