package com.gb.sched.service;

import com.gb.sched.model.ShiftRequest;
import com.gb.sched.model.ShiftSwap;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class ShiftRequestService {
  public static final String STATUS_PENDING = "待审批";
  public static final String STATUS_APPROVED = "已通过";
  public static final String STATUS_REJECTED = "已驳回";

  private final Map<Long, ShiftRequest> requests = new LinkedHashMap<>();
  private final List<ShiftSwap> approvedSwaps = new ArrayList<>();

  public ShiftRequestService() {
    LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
    put(new ShiftRequest(1, "周护士", "赵护士", monthStart.plusDays(3).toString(), "家庭事务需换班", STATUS_PENDING));
    put(new ShiftRequest(2, "林医生", "陈医生", monthStart.plusDays(5).toString(), "参加院内培训", STATUS_PENDING));
  }

  public synchronized List<ShiftRequest> listRequests() {
    return List.copyOf(requests.values());
  }

  public synchronized List<ShiftSwap> approvedSwaps() {
    return List.copyOf(approvedSwaps);
  }

  /**
   * 审批调班申请。幂等：已处理的申请再次提交时直接返回首次处理结果，
   * 不会重复换班或改变状态。
   */
  public synchronized ShiftRequest decide(long id, boolean approve) {
    ShiftRequest current = requests.get(id);
    if (current == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "调班申请不存在: " + id);
    }
    if (!STATUS_PENDING.equals(current.status())) {
      return current;
    }
    ShiftRequest decided = new ShiftRequest(
        current.id(), current.applicant(), current.replacement(), current.date(),
        current.reason(), approve ? STATUS_APPROVED : STATUS_REJECTED);
    requests.put(id, decided);
    if (approve) {
      approvedSwaps.add(new ShiftSwap(decided.date(), decided.applicant(), decided.replacement()));
    }
    return decided;
  }

  private void put(ShiftRequest request) {
    requests.put(request.id(), request);
  }
}
