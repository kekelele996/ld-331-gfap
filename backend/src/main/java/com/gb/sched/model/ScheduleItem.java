package com.gb.sched.model;

public record ScheduleItem(String date, String department, String position, String staffName, String shift, boolean holiday, String color) {}
