package com.gb.sched.config;

import java.util.List;

public final class AppConstants {
  private AppConstants() {}

  public static final List<String> SHIFT_TYPES = List.of("白班", "中班", "夜班", "休息");
  public static final List<String> ROLES = List.of("管理员", "主管", "医护人员");
  public static final String STATUS_PENDING = "主管审批中";
  public static final String STATUS_APPROVED = "已通过";
  public static final String STATUS_REJECTED = "已驳回";
  public static final String CONFLICT_NIGHT_TO_DAY = "夜班后不能直接接白班";
  public static final String CONFLICT_MAX_CONTINUOUS_DAYS = "连续工作天数超过上限";
}
