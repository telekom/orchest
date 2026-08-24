package io.telekom.orchest.adapter.mongo.listners;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;

/**
 * Interface for listening to MongoDB Change Streams. Implementations define how to process change
 * events from watched collections.
 *
 * @param <T> The type of the document being watched.
 */
public interface ChangeStreamListener<T> {
  /**
   * Handles a change stream document event.
   *
   * @param changeStreamDocument The document containing details about the change (insert, update,
   *     delete, etc.).
   */
  void listen(ChangeStreamDocument<T> changeStreamDocument, OperationType operationType);
}
