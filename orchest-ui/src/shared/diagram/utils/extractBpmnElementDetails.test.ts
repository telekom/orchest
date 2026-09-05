import { describe, expect, it } from 'vitest';
import {
  extractBpmnElementDetails,
  type BpmnBusinessObject,
} from './extractBpmnElementDetails';

describe('extractBpmnElementDetails', () => {
  it('returns empty for null / undefined', () => {
    expect(extractBpmnElementDetails(null)).toEqual([]);
    expect(extractBpmnElementDetails(undefined)).toEqual([]);
  });

  it('returns empty for a bare start event with only $type', () => {
    const bo: BpmnBusinessObject = { $type: 'bpmn:StartEvent' };
    expect(extractBpmnElementDetails(bo)).toEqual([]);
  });

  it('extracts service task definition and I/O mappings', () => {
    const bo: BpmnBusinessObject = {
      $type: 'bpmn:ServiceTask',
      extensionElements: {
        values: [
          {
            $type: 'zeebe:TaskDefinition',
            type: 'order-enricher',
            retries: '3',
          },
          {
            $type: 'zeebe:IoMapping',
            inputParameters: [
              { $type: 'zeebe:Input', source: '=order.id', target: 'orderId' },
              { $type: 'zeebe:Input', source: '=customer', target: 'customer' },
            ],
            outputParameters: [
              { $type: 'zeebe:Output', source: '=response', target: 'enriched' },
            ],
          },
          {
            $type: 'zeebe:TaskHeaders',
            values: [{ $type: 'zeebe:Header', key: 'method', value: 'POST' }],
          },
        ],
      },
    };

    const sections = extractBpmnElementDetails(bo);
    expect(sections[0]).toEqual({
      title: 'Element',
      fields: [{ label: 'BPMN type', value: 'bpmn:ServiceTask' }],
    });

    const byTitle = Object.fromEntries(sections.map((s) => [s.title, s.fields]));
    expect(byTitle['Task definition']).toEqual(
      expect.arrayContaining([
        { label: 'Type', value: 'order-enricher' },
        { label: 'Retries', value: '3' },
      ])
    );
    expect(byTitle['Input mappings']).toEqual([
      { label: 'orderId', value: '=order.id' },
      { label: 'customer', value: '=customer' },
    ]);
    expect(byTitle['Output mappings']).toEqual([
      { label: 'enriched', value: '=response' },
    ]);
    expect(byTitle.Headers).toEqual([{ label: 'method', value: 'POST' }]);
  });

  it('extracts call activity called element processId', () => {
    const bo: BpmnBusinessObject = {
      $type: 'bpmn:CallActivity',
      extensionElements: {
        values: [
          {
            $type: 'zeebe:CalledElement',
            processId: 'child-process',
            bindingType: 'latest',
            propagateAllChildVariables: true,
          },
        ],
      },
    };

    const sections = extractBpmnElementDetails(bo);
    const called = sections.find((s) => s.title === 'Called element');
    expect(called?.fields).toEqual(
      expect.arrayContaining([
        { label: 'Process Id', value: 'child-process' },
        { label: 'Binding Type', value: 'latest' },
        { label: 'Propagate All Child Variables', value: 'true' },
      ])
    );
  });

  it('returns empty instead of throwing on malformed objects', () => {
    const bo = {
      $type: 'bpmn:ServiceTask',
      get extensionElements() {
        throw new Error('boom');
      },
    } as unknown as BpmnBusinessObject;

    expect(extractBpmnElementDetails(bo)).toEqual([]);
  });

  it('omits empty input/output mapping sections when IoMapping has no parameters', () => {
    const bo: BpmnBusinessObject = {
      $type: 'bpmn:ServiceTask',
      extensionElements: {
        values: [
          { $type: 'zeebe:TaskDefinition', type: 'worker' },
          {
            $type: 'zeebe:IoMapping',
            inputParameters: [],
            outputParameters: [],
          },
        ],
      },
    };

    const sections = extractBpmnElementDetails(bo);
    const titles = sections.map((s) => s.title);
    expect(titles).not.toContain('Input mappings');
    expect(titles).not.toContain('Output mappings');
    expect(titles).toContain('Task definition');
  });

  it('omits only the empty mapping side when one list has entries', () => {
    const bo: BpmnBusinessObject = {
      $type: 'bpmn:ServiceTask',
      extensionElements: {
        values: [
          { $type: 'zeebe:TaskDefinition', type: 'worker' },
          {
            $type: 'zeebe:IoMapping',
            inputParameters: [
              { $type: 'zeebe:Input', source: '=a', target: 'b' },
            ],
            outputParameters: [],
          },
          {
            $type: 'zeebe:TaskHeaders',
            values: [],
          },
        ],
      },
    };

    const sections = extractBpmnElementDetails(bo);
    const titles = sections.map((s) => s.title);
    expect(titles).toContain('Input mappings');
    expect(titles).not.toContain('Output mappings');
    expect(titles).not.toContain('Headers');
  });

  it('shows correlationId for Message IntermediateCatchEvent', () => {
    const bo: BpmnBusinessObject = {
      $type: 'bpmn:IntermediateCatchEvent',
      eventDefinitions: [
        {
          $type: 'bpmn:MessageEventDefinition',
          messageRef: { name: 'OrderPaid', id: 'Message_OrderPaid' },
        },
      ],
      extensionElements: {
        values: [
          {
            $type: 'zeebe:Subscription',
            correlationKey: '=orderId',
          },
        ],
      },
    };

    const sections = extractBpmnElementDetails(bo);
    const event = sections.find((s) => s.title === 'Event');
    expect(event?.fields).toEqual(
      expect.arrayContaining([
        { label: 'Message', value: 'OrderPaid' },
        { label: 'correlationId', value: '=orderId' },
      ])
    );
    expect(sections.map((s) => s.title)).not.toContain('Subscription');
  });

  it('shows correlationId when subscription is nested under messageEventDefinition', () => {
    const bo: BpmnBusinessObject = {
      $type: 'bpmn:IntermediateCatchEvent',
      eventDefinitions: [
        {
          $type: 'bpmn:MessageEventDefinition',
          messageRef: { name: 'CAMUNDA_TO_ORCHEST_RESUME_EVENT' },
          extensionElements: {
            values: [
              {
                $type: 'zeebe:Subscription',
                correlationKey: '=processInstanceKey',
              },
            ],
          },
        },
      ],
    };

    const sections = extractBpmnElementDetails(bo);
    const event = sections.find((s) => s.title === 'Event');
    expect(event?.fields).toEqual(
      expect.arrayContaining([
        { label: 'Message', value: 'CAMUNDA_TO_ORCHEST_RESUME_EVENT' },
        { label: 'correlationId', value: '=processInstanceKey' },
      ])
    );
  });

  it('extracts sequence flow from/to and condition expression', () => {
    const bo: BpmnBusinessObject = {
      $type: 'bpmn:SequenceFlow',
      id: 'Flow_1',
      sourceRef: { id: 'Gateway_1', name: 'Split' },
      targetRef: { id: 'Task_A', name: 'Path A' },
      conditionExpression: { $type: 'bpmn:FormalExpression', body: '=status = "ok"' },
    };

    const sections = extractBpmnElementDetails(bo);
    const titles = sections.map((s) => s.title);
    expect(titles).toContain('Flow');
    expect(titles).toContain('Condition');

    const flow = sections.find((s) => s.title === 'Flow');
    expect(flow?.fields).toEqual(
      expect.arrayContaining([
        { label: 'From', value: 'Split' },
        { label: 'To', value: 'Path A' },
      ])
    );
    expect(sections.find((s) => s.title === 'Condition')?.fields).toEqual([
      { label: 'Expression', value: '=status = "ok"' },
    ]);
  });
});
