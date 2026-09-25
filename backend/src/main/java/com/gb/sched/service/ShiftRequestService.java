package com.gb.sched.service;

import static com.gb.sched.config.AppConstants.STATUS_APPROVED;
import static com.gb.sched.config.AppConstants.STATUS_PENDING;
import static com.gb.sched.config.AppConstants.STATUS_REJECTED;

import com.gb.sched.model.ShiftRequest;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ShiftRequestService {
  private final ScheduleRuleService scheduleRuleService;

  private final Map<Long, ShiftRequest> requests = new ConcurrentHashMap<>();
  // 每条申请一把锁，保证主管连点或页面重试时只有首次处理会生效。
  private final Map<Long, Object> locks = new ConcurrentHashMap<>();

  public ShiftRequestService(ScheduleRuleService scheduleRuleService) {
    this.scheduleRuleService = scheduleRuleService;
    LocalDate first = LocalDate.now().withDayOfMonth(1);
    save(new ShiftRequest(1, "急诊科", "周护士", "赵护士", first.plusDays(1).toString(), "家庭事务需换班", STATUS_PENDING));
    save(new ShiftRequest(2, "急诊科", "林医生", "陈医生", first.plusDays(3).toString(), "参加院内培训", STATUS_PENDING));
    save(new ShiftRequest(3, "急诊科", "王护士", "周护士", first.plusDays(5).toString(), "身体不适申请替班", STATUS_REJECTED));
  }

  public List<ShiftRequest> listRequests() {
    return requests.values().stream().sorted(Comparator.comparingLong(ShiftRequest::id)).toList();
  }

  /**
   * 主管审批调班申请。
   *
   * <p>同意：将申请人与替班人在申请当天的班次互换，申请标记为已通过。
   * 驳回：申请标记为已驳回，原排班保持不变。
   * 审批结果以首次提交为准：重复点击或页面重试不会再次互换班次，更不会把班次换回去，
   * 始终返回首次处理后的申请状态。
   */
  public ShiftRequest decide(long id, boolean approved) {
    ShiftRequest current = requests.get(id);
    if (current == null) {
      throw new RequestNotFoundException(id);
    }
    synchronized (locks.computeIfAbsent(id, key -> new Object())) {
      current = requests.get(id);
      if (!STATUS_PENDING.equals(current.status())) {
        return current;
      }
      String nextStatus = approved ? STATUS_APPROVED : STATUS_REJECTED;
      if (approved) {
        scheduleRuleService.swapShifts(current.department(), current.date(), current.applicant(), current.replacement());
      }
      current = new ShiftRequest(current.id(), current.department(), current.applicant(), current.replacement(),
          current.date(), current.reason(), nextStatus);
      return save(current);
    }
  }

  private ShiftRequest save(ShiftRequest request) {
    requests.put(request.id(), request);
    return request;
  }

  public static class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(long id) {
      super("调班申请不存在: " + id);
    }
  }
}
