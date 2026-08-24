package io.telekom.orchest.runners;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;

/**
 * Abstract base for one-shot command-line tasks that report success/failure via exit code.
 * Subclasses implement {@link #startTask()}; exceptions set exit code to 1.
 */
@Slf4j
public abstract class StartupTaskRunner implements CommandLineRunner, ExitCodeGenerator {

  private int exitCode = 0;

  /** Executes the task logic. Implementations should throw on failure. */
  public abstract void startTask();

  /**
   * Executes {@link #startTask()} and sets exit code to 1 on failure.
   *
   * @param args command-line arguments (unused)
   */
  @Override
  public void run(String @NonNull ... args) {
    try {
      startTask();
    } catch (Exception e) {
      log.error("failed to run the runner ", e);
      exitCode = 1;
    }
  }

  /**
   * Returns the exit code: 0 for success, 1 for failure.
   *
   * @return exit code
   */
  @Override
  public int getExitCode() {
    return exitCode;
  }
}
