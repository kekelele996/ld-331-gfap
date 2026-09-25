package com.gb.sched.service;

import com.gb.sched.model.Department;
import com.gb.sched.model.Position;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService {
  public List<Department> listDepartments() {
    return List.of(
        new Department("急诊科", List.of(
            new Position("主任医师", 1, List.of("急救", "质控")),
            new Position("护士", 8, List.of("分诊", "抢救")))),
        new Department("心内科", List.of(
            new Position("主治医师", 4, List.of("介入", "心电")),
            new Position("护士长", 1, List.of("病区管理")))));
  }
}
