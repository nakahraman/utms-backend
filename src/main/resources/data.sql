-------------------------------------------------------
-- CLEAN TABLES (FK sırasına göre)
-------------------------------------------------------
DELETE FROM users;
DELETE FROM students;
DELETE FROM department_allowed_semesters;
DELETE FROM departments;
DELETE FROM faculties;

-------------------------------------------------------
-- FACULTIES
-------------------------------------------------------
INSERT INTO faculties (faculty_id, faculty_name) VALUES
(1, 'Engineering Faculty'),
(2, 'Science Faculty'),
(3, 'Architecture Faculty');

-------------------------------------------------------
-- DEPARTMENTS
-------------------------------------------------------
INSERT INTO departments
(dept_id, faculty_id, dept_name,
 min_gpa, max_success_rank,
 requires_all_courses_passed, requires_portfolio)
VALUES
(1, 1, 'Computer Engineering', 2.50, 30000, TRUE, FALSE),
(2, 1, 'Electrical-Electronics Engineering', 2.70, 25000, TRUE, FALSE),
(3, 2, 'Mathematics', 3.00, 20000, TRUE, FALSE),
(4, 2, 'Physics', 3.00, 18000, TRUE, FALSE),
(5, 3, 'Architecture', 2.80, 35000, FALSE, TRUE),
(6, 3, 'Urban and Regional Planning', 2.60, 40000, FALSE, FALSE);

-------------------------------------------------------
-- USERS
-------------------------------------------------------
INSERT INTO users
(user_id, role, username, password_hash, faculty_id, user_source)
VALUES
(1, 'STUDENT', 'std1', '$2a$10$hashstudent1', NULL, 'UBYS'),
(2, 'FACULTY', 'fac1', '$2a$10$hashfaculty1', 1,    'UBYS'),
(3, 'YGK',     'ygk1',     '$2a$10$hashygk1',      1,    'UBYS'),
(4, 'YDYO',    'ydyo1',    '$2a$10$hashydyo1',     NULL, 'UBYS'),
(5, 'OIDB',    'oidb1',    '$2a$10$hashoidb1',     NULL, 'UBYS'),
(6, 'FACULTY', 'fac2', '$2a$10$hashfaculty2', 2,    'UBYS'),
(7, 'YGK',     'ygk2',     '$2a$10$hashygk2',      3,    'UBYS');

-------------------------------------------------------
-- DEPARTMENT ALLOWED SEMESTERS
-------------------------------------------------------
INSERT INTO department_allowed_semesters (dept_id, semester) VALUES
(1, 3),(1, 4),(1, 5),
(2, 3),(2, 4),(2, 5),
(3, 5),(3, 6),
(4, 5),(4, 6),
(5, 3),(5, 4),
(6, 3),(6, 4);

-------------------------------------------------------
-- STUDENTS
INSERT INTO students
(student_id, username, name, dept_id, gpa, exam_score, email, student_type, user_id)
VALUES
(1, 'std1', 'Ali Veli',      1, 3.05, 78.0, 'ali@iyte.edu.tr',   'INTERNAL', 1),
(2, 'std2', 'Ayşe Demir',    2, 3.40, 85.0, 'ayse@iyte.edu.tr',  'INTERNAL', 2),
(3, 'std3', 'Mehmet Yılmaz', 3, 2.95, 72.0, 'mehmet@iyte.edu.tr','EXTERNAL', 3),
(4, 'std4', 'Zeynep Kaya',   5, 3.60, 90.0, 'zeynep@iyte.edu.tr','INTERNAL', 4);



