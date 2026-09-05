/**
 * Download text content as a file
 * @param content - File content as string
 * @param filename - Name of the file to download
 * @param mimeType - MIME type of the file (default: text/xml)
 */
export function downloadTextFile(
  content: string,
  filename: string,
  mimeType: string = 'text/xml'
): void {
  const blob = new Blob([content], { type: mimeType });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  window.URL.revokeObjectURL(url);
}

/**
 * Download BPMN XML as a file
 * @param xml - BPMN XML content
 * @param filename - Name of the file (default: diagram.bpmn)
 */
export function downloadBpmnXml(xml: string, filename: string = 'diagram.bpmn'): void {
  downloadTextFile(xml, filename, 'text/xml');
}

/**
 * Download DMN XML as a file
 * @param xml - DMN XML content
 * @param filename - Name of the file (default: decision.dmn)
 */
export function downloadDmnXml(xml: string, filename: string = 'decision.dmn'): void {
  downloadTextFile(xml, filename, 'text/xml');
}

/**
 * Read a file and return its content as text
 * @param file - File object to read
 * @returns Promise resolving to file content as string
 */
export function readFileAsText(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const content = e.target?.result as string;
      resolve(content);
    };
    reader.onerror = () => {
      reject(new Error('Failed to read file'));
    };
    reader.readAsText(file);
  });
}

/**
 * Download JSON data as a file
 * @param data - Data to serialize to JSON
 * @param filename - Name of the file to download
 */
export function downloadJson(data: unknown, filename: string): void {
  const jsonString = JSON.stringify(data, null, 2);
  downloadTextFile(jsonString, filename, 'application/json');
}
