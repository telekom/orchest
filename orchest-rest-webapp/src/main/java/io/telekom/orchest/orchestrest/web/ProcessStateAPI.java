package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.api.core.adapters.data.model.ProcessState;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessStateRepository;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller for managing process state configurations. */
@RestController
@RequestMapping("/processStates")
@RequiredArgsConstructor
public class ProcessStateAPI {

  private final ProcessStateRepository processStateRepository;

  @GetMapping
  public Mono<List<ProcessState>> getProcessStates() {
    return Mono.fromCallable(processStateRepository::findAll)
        .subscribeOn(Schedulers.boundedElastic());
  }

  @GetMapping("/{processDefinitionId}")
  public Mono<ProcessState> getProcessStateForProcessId(@PathVariable String processDefinitionId) {
    requireProcessId(processDefinitionId);
    return Mono.fromCallable(() -> processStateRepository.findByProcessId(processDefinitionId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            opt ->
                opt.map(Mono::just)
                    .orElseGet(
                        () ->
                            Mono.error(
                                new RestExceptions(
                                    "No process state found for: " + processDefinitionId, 404))));
  }

  /**
   * Creates a process state with {@code status=true}. Returns 409 if {@code processId} already
   * exists.
   */
  @PostMapping
  public Mono<ProcessState> addProcessState(@RequestBody ProcessState processState) {
    requireProcessStateBody(processState);
    String processId = processState.getProcessId();
    return Mono.fromCallable(() -> processStateRepository.findByProcessId(processId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            existing ->
                existing.isPresent()
                    ? Mono.error(
                        new RestExceptions("Process state already exists for: " + processId, 409))
                    : Mono.fromCallable(
                            () -> {
                              processState.setId(null);
                              processState.setStatus(true);
                              return processStateRepository.save(processState);
                            })
                        .subscribeOn(Schedulers.boundedElastic()));
  }

  @PutMapping
  public Mono<ProcessState> upsertProcessState(@RequestBody ProcessState processState) {
    requireProcessStateBody(processState);
    return Mono.fromCallable(() -> processStateRepository.save(processState))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @DeleteMapping("/{processDefinitionId}")
  public Mono<ProcessState> removeProcessState(@PathVariable String processDefinitionId) {
    requireProcessId(processDefinitionId);
    return Mono.fromCallable(() -> processStateRepository.deleteByProcessId(processDefinitionId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            opt ->
                opt.map(Mono::just)
                    .orElseGet(
                        () ->
                            Mono.error(
                                new RestExceptions(
                                    "No process state found for: " + processDefinitionId, 404))));
  }

  private static void requireProcessId(String processId) {
    if (!StringUtils.hasText(processId)) {
      throw new RestExceptions("processId is required", 400);
    }
  }

  private static void requireProcessStateBody(ProcessState processState) {
    if (processState == null || !StringUtils.hasText(processState.getProcessId())) {
      throw new RestExceptions("processId is required", 400);
    }
  }
}
