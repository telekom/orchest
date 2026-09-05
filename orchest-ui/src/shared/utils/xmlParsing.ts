import { logger } from './logger';

interface DefinitionInfo {
  definitionId: string;
  definitionName: string;
  diagramType: 'bpmn' | 'dmn' | 'unknown';
}

const BPMN_SELECTORS = ['process', 'bpmn\\:process', 'bpmn2\\:process'];
const DMN_SELECTORS = ['decision', 'dmn\\:decision', 'dmn11\\:decision'];

const extractAttributesFromElement = (
  element: Element | null,
  defaultId: string
): { id: string; name: string } => {
  if (!element) return { id: defaultId, name: defaultId };
  const id = element.getAttribute('id') || defaultId;
  const name = element.getAttribute('name') || id;
  return { id, name };
};

const parseDefinitionFromXML = (
  xmlDoc: Document,
  selectors: string[],
  defaultId: string,
  diagramType: 'bpmn' | 'dmn'
): DefinitionInfo | null => {
  const element = xmlDoc.querySelector(selectors.join(', '));
  if (!element) return null;

  const { id, name } = extractAttributesFromElement(element, defaultId);
  return { definitionId: id, definitionName: name, diagramType };
};

/**
 * Extracts definition information from BPMN or DMN XML string
 * @param xmlString - The XML content as a string
 * @returns Definition information including ID, name, and diagram type
 */
export const extractDefinitionInfo = (xmlString: string): DefinitionInfo => {
  try {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlString, 'text/xml');

    const bpmnInfo = parseDefinitionFromXML(xmlDoc, BPMN_SELECTORS, 'Unknown Process', 'bpmn');
    if (bpmnInfo) return bpmnInfo;

    const dmnInfo = parseDefinitionFromXML(xmlDoc, DMN_SELECTORS, 'Unknown Decision', 'dmn');
    if (dmnInfo) return dmnInfo;

    return { definitionId: 'Unknown', definitionName: 'Unknown', diagramType: 'unknown' };
  } catch (error) {
    logger.error('Error parsing XML:', error);
    return { definitionId: 'Error parsing', definitionName: 'Error parsing', diagramType: 'unknown' };
  }
};
