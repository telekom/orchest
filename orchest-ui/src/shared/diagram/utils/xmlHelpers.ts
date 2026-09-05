export function extractDefinitionId(xml: string, type: 'bpmn' | 'dmn'): string | null {
  if (!xml?.trim()) return null;

  try {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xml, 'text/xml');

    if (xmlDoc.querySelector('parsererror')) return null;

    const elementName = type === 'bpmn' ? 'process' : 'decision';
    const allElements = xmlDoc.getElementsByTagName('*');

    for (let i = 0; i < allElements.length; i++) {
      const element = allElements[i];
      const localName = element.localName || element.tagName.split(':').pop();

      if (localName === elementName && element.hasAttribute('id')) {
        const id = element.getAttribute('id')?.trim();
        if (id) return id;
      }
    }

    return null;
  } catch {
    return null;
  }
}
