import { apiClient } from './client';
import type { DashboardData, ShiftRequest } from '../types/schedule';

export async function fetchDashboard(department: string) {
  const { data } = await apiClient.get<DashboardData>('/dashboard', { params: { department } });
  return data;
}

export async function decideShiftRequest(id: number, action: 'approve' | 'reject') {
  const { data } = await apiClient.post<ShiftRequest>(`/requests/${id}/decision`, { action });
  return data;
}
