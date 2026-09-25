package com.gb.sched.service;

import com.gb.sched.config.AppConstants;
import com.gb.sched.model.ConflictAlert;
import com.gb.sched.model.ScheduleItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ScheduleRuleService {
  private static final List<String> STAFF = List.of("陈医生", "林医生", "周护士", "赵护士", "王护士");

  // 已生成的排班表按科室缓存，调班审批通过后直接在缓存上互换班次。
  private final Map<String, List<ScheduleItem>> scheduleCache = new ConcurrentHashMap<>();

  public List<ScheduleItem> generateMonthlySchedule(String department) {
    return List.copyOf(scheduleCache.computeIfAbsent(department, this::buildMonthlySchedule));
  }

  private List<ScheduleItem> buildMonthlySchedule(String department) {
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

  /**
   * 将指定日期申请人与替班人的班次（含班次名称与颜色）互换。
   * 整个互换过程加锁，保证并发审批不会产生脏数据。
   */
  public void swapShifts(String department, String date, String applicant, String replacement) {
    List<ScheduleItem> items = scheduleCache.computeIfAbsent(department, this::buildMonthlySchedule);
    synchronized (items) {
      ScheduleItem applicantItem = findItem(items, date, applicant);
      ScheduleItem replacementItem = findItem(items, date, replacement);
      if (applicantItem == null || replacementItem == null || applicantItem == replacementItem) {
        throw new IllegalArgumentException("当天排班中找不到申请人或替班人，无法互换班次");
      }
      int applicantIndex = items.indexOf(applicantItem);
      int replacementIndex = items.indexOf(replacementItem);
      // 先记录双方原始班次再写入，避免先写后读导致换回去。
      String applicantShift = applicantItem.shift();
      String applicantColor = applicantItem.color();
      String replacementShift = replacementItem.shift();
      String replacementColor = replacementItem.color();
      items.set(applicantIndex, copyWithShift(applicantItem, replacementShift, replacementColor));
      items.set(replacementIndex, copyWithShift(replacementItem, applicantShift, applicantColor));
    }
  }

  private ScheduleItem findItem(List<ScheduleItem> items, String date, String staffName) {
    return items.stream()
        .filter(item -> item.date().equals(date) && item.staffName().equals(staffName))
        .findFirst()
        .orElse(null);
  }

  private ScheduleItem copyWithShift(ScheduleItem item, String shift, String color) {
    return new ScheduleItem(item.date(), item.department(), item.position(), item.staffName(), shift, item.holiday(), color);
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
