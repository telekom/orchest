package io.telekom.orchest.client.annotations;

import java.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Stereotype annotation to mark a class as an OrchesT worker bean. Classes annotated with this are
 * scanned for {@link JobWorker}-annotated methods at startup.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Worker {}
