import { lazy, ComponentType } from 'react';
import { ModelerTypes, ModelerType } from '../constants';

export interface BaseModelerProps {
  onSave: (xml: string) => void;
  activeModeler?: string;
  onModelerTypeChange?: (value: string) => void;
}

export const MODELER_REGISTRY: Record<ModelerType, ComponentType<BaseModelerProps>> = {
  [ModelerTypes.BPMN]: lazy(() => import('../components/BpmnModeler/BpmnModeler')),
  [ModelerTypes.DMN]: lazy(() => import('../components/DmnModeler/DmnModeler')),
  [ModelerTypes.FORM]: lazy(() => import('../components/FormModeler/FormModeler')),
};
