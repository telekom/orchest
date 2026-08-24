package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.ClusterInfo;
import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.ProcessHealth;
import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.WorkerInfo;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/kafka")
@Tag(
    name = "Kafka Inspection",
    description =
        "Inspect Kafka topics, consumer groups, and process-level health for the orchestration cluster")
/** REST API interface for Kafka cluster inspection and process health checks. */
public interface IKafkaInspectionAPI {

  @GetMapping("/worker-info")
  @Operation(
      summary = "Get worker info for a process",
      description =
          "Returns computed topic/consumer-group names for a process definition and their live existence status on the cluster")
  Mono<ResponseDTO<WorkerInfo>> getWorkerInfo(
      @Parameter(description = "Process definition ID to inspect") @RequestParam
          String processDefinitionId);

  @GetMapping("/cluster-info")
  @Operation(
      summary = "Get cluster info",
      description =
          "Lists all ORCHEST-prefixed topics and consumer groups currently registered on the Kafka cluster")
  Mono<ResponseDTO<ClusterInfo>> getClusterInfo();

  @GetMapping("/process-health")
  @Operation(
      summary = "Get process health overview",
      description =
          "For every stored process definition (latest version per ID), returns required topics/groups and whether they exist on the cluster")
  Mono<ResponseDTO<List<ProcessHealth>>> getProcessHealth();
}
