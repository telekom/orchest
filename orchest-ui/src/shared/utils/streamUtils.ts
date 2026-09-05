/**
 * Utility functions for working with ReadableStream
 */

/**
 * Reads a ReadableStream<Uint8Array> to a complete string
 */
export async function readStreamToString(stream: ReadableStream<Uint8Array>): Promise<string> {
  const reader = stream.getReader();
  const decoder = new TextDecoder();
  let result = "";

  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      result += decoder.decode(value, { stream: true });
    }
    // Final decode call with stream: false
    result += decoder.decode();
    return result;
  } finally {
    reader.releaseLock();
  }
}

/**
 * Reads a ReadableStream<Uint8Array> chunk by chunk, calling onChunk for each piece
 */
export async function readStreamWithCallback(
  stream: ReadableStream<Uint8Array>,
  onChunk: (chunk: string) => void
): Promise<void> {
  const reader = stream.getReader();
  const decoder = new TextDecoder();

  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      const chunk = decoder.decode(value, { stream: true });
      onChunk(chunk);
    }
  } finally {
    reader.releaseLock();
  }
}
