-- 1. Departments (deptId -> dept_id, deptName -> dept_name)
MERGE INTO departments (dept_id, faculty_id, dept_name, criteria)
KEY(dept_id)
VALUES (1, 1, 'Computer Engineering', 'Standard Criteria');

-- 2. Users (userId -> user_id, passwordHash -> password_hash)
MERGE INTO users (user_id, username, password_hash, role)
KEY(user_id)
VALUES (1, 'student1', '{noop}1234', 'STUDENT');

MERGE INTO users (user_id, username, password_hash, role)
KEY(user_id)
VALUES (2, 'faculty1', '{noop}1234', 'FACULTY');

MERGE INTO users (user_id, username, password_hash, role)
KEY(user_id)
VALUES (3, 'ygk1', '{noop}1234', 'YGK');

MERGE INTO users (user_id, username, password_hash, role)
KEY(user_id)
VALUES (4, 'oidb1', '{noop}1234', 'OIDB');

-- 3. Students (studentId -> student_id, examScore -> exam_score)
MERGE INTO students (student_id, name, department, gpa, exam_score, email)
KEY(student_id)
VALUES (1, 'Ali Veli', 'Computer Engineering', 3.05, 78.0, 'ali@iyte.edu.tr');