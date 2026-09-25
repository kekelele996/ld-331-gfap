import { apiClient } from './client';
import type { DashboardData, ShiftRequest } from '../types/schedule';

export async function fetchDashboard(department: string) {
  const { data } = await apiClient.get<DashboardData>('/dashboard', { params: { department } });
  return data;
}

export async function decideShiftRequest(id: number, approved: boolean) {
  const { data } = await apiClient.post<ShiftRequest>(`/shift-requests/${id}/decision`, { approved });
  return data;
}
