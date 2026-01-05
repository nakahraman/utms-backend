MERGE INTO faculties (faculty_id, faculty_name)
KEY (faculty_id)
VALUES (1, 'Engineering Faculty');

MERGE INTO departments (dept_id, faculty_id, dept_name, criteria)
KEY (dept_id)
VALUES (1, 1, 'Computer Engineering', 'Standard Criteria');

MERGE INTO students (student_id, name, dept_id, gpa, exam_score, email, student_type)
KEY (student_id)
VALUES
(1, 'Ali Veli', 1, 3.05, 78.0, 'ali@iyte.edu.tr', 'INTERNAL');

MERGE INTO users (user_id, role, username, password_hash, faculty_id, user_source)
KEY(user_id)
VALUES
(1, 'STUDENT',   'student1',   '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', NULL, 'UBYS'),
(2, 'FACULTY',   'faculty1',   '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', 1,    'UBYS'),
(3, 'YGK',       'ygk1',       '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', 1,    'UBYS'),
(4, 'YDYO',      'ydyo1',      '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', NULL,    'UBYS'),
(5, 'REGISTRAR', 'registrar1', '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', NULL, 'UBYS');
