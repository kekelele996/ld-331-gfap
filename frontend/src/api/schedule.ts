import { apiClient } from './client';
import type { DashboardData } from '../types/schedule';

export async function fetchDashboard(department: string) {
  const { data } = await apiClient.get<DashboardData>('/dashboard', { params: { department } });
  return data;
}
