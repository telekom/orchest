/**
 * Parses Server-Sent Events (SSE) stream into typed data objects
 */
export async function* parseSSEStream<T = unknown>(
  reader: ReadableStreamDefaultReader<Uint8Array>
): AsyncGenerator<T, void, unknown> {
  const decoder = new TextDecoder();

  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value);
      const lines = chunk.split('\n');

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          try {
            const data = JSON.parse(line.slice(6));
            yield data;
          } catch {
            // Ignore invalid JSON chunks
          }
        }
      }
    }
  } finally {
    reader.releaseLock();
  }
}
