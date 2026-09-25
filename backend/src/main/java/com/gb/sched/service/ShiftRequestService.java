package com.gb.sched.service;

import com.gb.sched.model.ShiftRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ShiftRequestService {
  public List<ShiftRequest> listRequests() {
    return List.of(
        new ShiftRequest(1, "周护士", "赵护士", "2026-06-08", "家庭事务需换班", "主管审批中"),
        new ShiftRequest(2, "林医生", "陈医生", "2026-06-10", "参加院内培训", "已通过"));
  }
}
