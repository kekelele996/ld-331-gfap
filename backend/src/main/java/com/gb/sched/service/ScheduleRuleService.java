package com.gb.sched.service;

import com.gb.sched.config.AppConstants;
import com.gb.sched.model.ConflictAlert;
import com.gb.sched.model.ScheduleItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ScheduleRuleService {
  private static final List<String> STAFF = List.of("陈医生", "林医生", "周护士", "赵护士", "王护士");

  public List<ScheduleItem> generateMonthlySchedule(String department) {
    List<ScheduleItem> items = new ArrayList<>();
    LocalDate start = LocalDate.now().withDayOfMonth(1);
    for (int day = 0; day < 14; day++) {
      LocalDate current = start.plusDays(day);
      for (int index = 0; index < STAFF.size(); index++) {
        String shift = AppConstants.SHIFT_TYPES.get((day + index) % AppConstants.SHIFT_TYPES.size());
        items.add(new ScheduleItem(current.toString(), department, index < 2 ? "医生" : "护士", STAFF.get(index), shift, day % 6 == 0, shiftColor(shift)));
      }
    }
    return items;
  }

  public List<ConflictAlert> detectConflicts(List<ScheduleItem> schedule) {
    List<ConflictAlert> alerts = new ArrayList<>();
    schedule.stream()
        .filter(item -> item.shift().equals("夜班"))
        .limit(2)
        .forEach(item -> alerts.add(new ConflictAlert("warning", item.staffName(), item.date(), AppConstants.CONFLICT_NIGHT_TO_DAY)));
    alerts.add(new ConflictAlert("danger", "赵护士", LocalDate.now().plusDays(3).toString(), AppConstants.CONFLICT_MAX_CONTINUOUS_DAYS));
    return alerts;
  }

  private String shiftColor(String shift) {
    return switch (shift) {
      case "白班" -> "#409eff";
      case "中班" -> "#67c23a";
      case "夜班" -> "#626aef";
      default -> "#909399";
    };
  }
}
