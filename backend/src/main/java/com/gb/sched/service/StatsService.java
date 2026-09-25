package com.gb.sched.service;

import com.gb.sched.model.WorkStats;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
  public List<WorkStats> monthlyStats() {
    return List.of(
        new WorkStats("陈医生", 12, 3, 4, 6, 8),
        new WorkStats("周护士", 10, 5, 6, 5, 12),
        new WorkStats("赵护士", 11, 4, 5, 6, 10));
  }
}
