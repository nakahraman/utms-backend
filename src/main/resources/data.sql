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
 requires_all_courses_passed, requires_portfolio,
 quota, waitlist_quota)
VALUES
(1, 1, 'Computer Engineering', 2.50, 30000, TRUE, FALSE, 5, 3),
(2, 1, 'Electrical-Electronics Engineering', 2.70, 25000, TRUE, FALSE, 5, 3),
(3, 2, 'Mathematics', 3.00, 20000, TRUE, FALSE, 4, 2),
(4, 2, 'Physics', 3.00, 18000, TRUE, FALSE, 4, 2),
(5, 3, 'Architecture', 2.80, 35000, FALSE, TRUE, 3, 2),
(6, 3, 'Urban and Regional Planning', 2.60, 40000, FALSE, FALSE, 3, 2);


-------------------------------------------------------
-- USERS
-------------------------------------------------------
INSERT INTO users
(user_id, role, username, password_hash, faculty_id, user_source, email, name)
VALUES
(1, 'STUDENT', 'std1', '$2a$10$hashstudent1', NULL, 'UBYS', 'std1@iyte.edu.tr', 'Ali Veli'),
(2, 'STUDENT', 'std2', '$2a$10$hashstudent2', NULL, 'UBYS', 'std2@iyte.edu.tr', 'Ayşe Demir'),
(3, 'STUDENT', 'std3', '$2a$10$hashstudent3', NULL, 'EXTERNAL', 'std3@gmail.com', 'Mehmet Yılmaz'),
(4, 'STUDENT', 'std4', '$2a$10$hashstudent4', NULL, 'UBYS', 'std4@iyte.edu.tr', 'Zeynep Kaya'),

(5, 'FACULTY', 'fac1', '$2a$10$hashfaculty1', 1, 'UBYS', 'fac1@iyte.edu.tr', 'Dr. Ali Kaya'),
(6, 'FACULTY', 'fac2', '$2a$10$hashfaculty2', 2, 'UBYS', 'fac2@iyte.edu.tr', 'Dr. Ayşe Yıldız'),
(7, 'YGK',     'ygk1', '$2a$10$hashygk1',     1, 'UBYS', 'ygk1@iyte.edu.tr', 'Prof. Mehmet Demir'),
(8, 'YGK',     'ygk2', '$2a$10$hashygk2',     3, 'UBYS', 'ygk2@iyte.edu.tr', 'Prof. Zeynep Akın'),
(9, 'YDYO',    'ydyo1','$2a$10$hashydyo1',    NULL,'UBYS', 'ydyo1@iyte.edu.tr', 'Öğr. Gör. Hasan Can'),
(10,'OIDB',    'oidb1','$2a$10$hashoidb1',    NULL,'UBYS', 'oidb1@iyte.edu.tr', 'Murat Ersoy');


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
(student_id, dept_id, gpa, exam_score, success_rank, student_type, user_id)
VALUES
(1, 1, 3.05, 78.0, 15000, 'INTERNAL', 1),
(2, 2, 3.40, 85.0, 12000, 'INTERNAL', 2),
(3, 3, 2.95, 72.0, 35000, 'EXTERNAL', 3),
(4, 5, 3.60, 90.0, 8000,  'INTERNAL', 4);





