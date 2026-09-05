import { httpClient } from '@/api/core';

export interface TopicInfo {
  topic: string;
  consumerGroup: string;
  topicExists: boolean;
  consumerGroupExists: boolean;
}

export interface WorkerInfo {
  processDefinitionId: string;
  clientWorkerEvent: TopicInfo;
  clientConsumerGroups: string[];
  clientConsumerStatus: string;
  serverWorkerEvent: TopicInfo;
}

export interface ResourceStatus {
  name: string;
  exists: boolean;
}

export interface ProcessHealth {
  processDefinitionId: string;
  clientWorkerEventTopic: ResourceStatus;
  clientConsumerGroups: string[];
  serverWorkerEventTopic: ResourceStatus;
  serverWorkerEventConsumerGroup: ResourceStatus;
  healthy: boolean;
}

export interface ClusterInfo {
  topics: string[];
  consumerGroups: string[];
}

interface ResponseDTO<T> {
  meta?: unknown;
  data: T;
}

const BASE = '/orchest/kafka';

export const kafkaService = {
  getWorkerInfo: (processDefinitionId: string) =>
    httpClient.get<ResponseDTO<WorkerInfo>>(`${BASE}/worker-info`, { params: { processDefinitionId } }),

  getProcessHealth: () =>
    httpClient.get<ResponseDTO<ProcessHealth[]>>(`${BASE}/process-health`),

  getClusterInfo: () =>
    httpClient.get<ResponseDTO<ClusterInfo>>(`${BASE}/cluster-info`),
};
