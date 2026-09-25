package com.gb.sched.model;

public record ShiftRequest(long id, String department, String applicant, String replacement, String date, String reason, String status) {}
