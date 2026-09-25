CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  actor VARCHAR(80) NOT NULL,
  action VARCHAR(120) NOT NULL,
  target VARCHAR(120) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS schedule_snapshot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  department VARCHAR(80) NOT NULL,
  schedule_date DATE NOT NULL,
  shift_name VARCHAR(40) NOT NULL,
  staff_name VARCHAR(80) NOT NULL
);

INSERT INTO schedule_snapshot (department, schedule_date, shift_name, staff_name)
VALUES ('急诊科', CURDATE(), '白班', '陈医生'), ('急诊科', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '夜班', '周护士');
