package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.api.core.adapters.data.model.ActivityState;
import io.telekom.orchest.api.core.adapters.data.repository.ActivityStateRepository;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller for querying BPMN activity state data per process definition. */
@RestController
@RequestMapping("/activityStates")
@RequiredArgsConstructor
public class ActivityStateAPI {

  private final ActivityStateRepository activityStateRepository;

  @GetMapping
  public Mono<List<ActivityState>> getAllActivityStates() {
    return Mono.fromCallable(activityStateRepository::findAll)
        .subscribeOn(Schedulers.boundedElastic());
  }

  @GetMapping("/{processDefinitionId}")
  public Mono<List<ActivityState>> getByProcessDefinitionId(
      @PathVariable String processDefinitionId) {
    requireProcessDefinitionId(processDefinitionId);
    return Mono.fromCallable(
            () -> activityStateRepository.findByProcessDefinitionId(processDefinitionId))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @GetMapping("/{processDefinitionId}/{version}")
  public Mono<List<ActivityState>> getByProcessDefinitionIdAndVersion(
      @PathVariable String processDefinitionId, @PathVariable Integer version) {
    requireProcessDefinitionId(processDefinitionId);
    return Mono.fromCallable(
            () ->
                activityStateRepository.findByProcessDefinitionIdAndVersion(
                    processDefinitionId, version))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @PutMapping
  public Mono<ActivityState> upsertActivityState(@RequestBody ActivityState activityState) {
    requireActivityStateBody(activityState);
    return Mono.fromCallable(() -> activityStateRepository.save(activityState))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @DeleteMapping("/{processDefinitionId}/{version}/{activityId}")
  public Mono<ActivityState> removeActivityState(
      @PathVariable String processDefinitionId,
      @PathVariable Integer version,
      @PathVariable String activityId) {
    requireProcessDefinitionId(processDefinitionId);
    return Mono.fromCallable(
            () ->
                activityStateRepository.deleteByProcessDefinitionIdAndVersionAndActivityId(
                    processDefinitionId, version, activityId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            opt ->
                opt.map(Mono::just)
                    .orElseGet(
                        () ->
                            Mono.error(
                                new RestExceptions(
                                    "No activity state found for: "
                                        + processDefinitionId
                                        + "/"
                                        + version
                                        + "/"
                                        + activityId,
                                    404))));
  }

  private static void requireProcessDefinitionId(String processDefinitionId) {
    if (!StringUtils.hasText(processDefinitionId)) {
      throw new RestExceptions("processDefinitionId is required", 400);
    }
  }

  private static void requireActivityStateBody(ActivityState activityState) {
    if (activityState == null
        || !StringUtils.hasText(activityState.getProcessDefinitionId())
        || activityState.getVersion() == null
        || !StringUtils.hasText(activityState.getActivityId())) {
      throw new RestExceptions("processDefinitionId, version, and activityId are required", 400);
    }
  }
}
