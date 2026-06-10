export default (key, name, type) => {
  if (!type) type = 'camunda';
  const TYPE_TARGET = {
    activiti: 'http://activiti.org/bpmn',
    camunda: 'http://bpmn.io/schema/bpmn',
    flowable: 'http://flowable.org/bpmn'
  };
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="diagram_${key}"
  targetNamespace="${TYPE_TARGET[type]}">
  <bpmn2:process id="${key}" name="${name}" isExecutable="true">
    <bpmn2:startEvent id="StartEvent_1" name="开始" />
    <bpmn2:endEvent id="EndEvent_1" name="结束" />
    <bpmn2:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="EndEvent_1" />
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${key}">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="150" y="120" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
        <dc:Bounds x="300" y="120" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1">
        <di:waypoint x="186" y="138" />
        <di:waypoint x="300" y="138" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn2:definitions>`;
};
