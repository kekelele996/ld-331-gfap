package com.gb.sched.controller;

import com.gb.sched.model.Department;
import com.gb.sched.model.ScheduleItem;
import com.gb.sched.model.ShiftRequest;
import com.gb.sched.service.DepartmentService;
import com.gb.sched.service.ScheduleRuleService;
import com.gb.sched.service.ShiftRequestService;
import com.gb.sched.service.StatsService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ScheduleController {
  private final DepartmentService departmentService;
  private final ScheduleRuleService scheduleRuleService;
  private final ShiftRequestService shiftRequestService;
  private final StatsService statsService;

  public ScheduleController(DepartmentService departmentService, ScheduleRuleService scheduleRuleService, ShiftRequestService shiftRequestService, StatsService statsService) {
    this.departmentService = departmentService;
    this.scheduleRuleService = scheduleRuleService;
    this.shiftRequestService = shiftRequestService;
    this.statsService = statsService;
  }

  @GetMapping("/dashboard")
  public Map<String, Object> dashboard(@RequestParam(name = "department", defaultValue = "急诊科") String department) {
    List<ScheduleItem> schedule = scheduleRuleService.generateMonthlySchedule(department);
    return Map.of(
        "rules", List.of("连续工作不超过 5 天", "周末轮循", "夜班后不接白班", "节假日按优先级排班"),
        "schedule", schedule,
        "conflicts", scheduleRuleService.detectConflicts(schedule),
        "requests", shiftRequestService.listRequests(),
        "stats", statsService.monthlyStats());
  }

  @GetMapping("/departments")
  public List<Department> departments() {
    return departmentService.listDepartments();
  }

  /**
   * 主管审批调班申请：action=approve 同意并互换当天班次，action=reject 驳回且排班不变。
   * 接口幂等，重复提交返回首次处理结果。
   */
  @PostMapping("/requests/{id}/decision")
  public ShiftRequest decideRequest(@PathVariable long id, @RequestBody Map<String, String> body) {
    String action = body.getOrDefault("action", "");
    return switch (action) {
      case "approve" -> shiftRequestService.decide(id, true);
      case "reject" -> shiftRequestService.decide(id, false);
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action 必须是 approve 或 reject");
    };
  }
}
