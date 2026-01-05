MERGE INTO faculties (faculty_id, faculty_name)
KEY (faculty_id)
VALUES
(1, 'Engineering Faculty'),
(2, 'Science Faculty'),
(3, 'Architecture Faculty');


MERGE INTO departments (dept_id, faculty_id, dept_name, criteria)
KEY (dept_id)
VALUES
(1, 1, 'Computer Engineering', 'Standard Criteria'),
(2, 1, 'Electrical-Electronics Engineering', 'Standard Criteria'),
(3, 2, 'Mathematics', 'High GPA Required'),
(4, 2, 'Physics', 'High GPA Required'),
(5, 3, 'Architecture', 'Portfolio Review'),
(6, 3, 'Urban and Regional Planning', 'Interview Required');


MERGE INTO students (student_id, name, dept_id, gpa, exam_score, email, student_type)
KEY (student_id)
VALUES
(1, 'Ali Veli',      1, 3.05, 78.0, 'ali@iyte.edu.tr',   'INTERNAL'),
(2, 'Ayşe Demir',    2, 3.40, 85.0, 'ayse@iyte.edu.tr',  'INTERNAL'),
(3, 'Mehmet Yılmaz', 3, 2.95, 72.0, 'mehmet@iyte.edu.tr','EXTERNAL'),
(4, 'Zeynep Kaya',   5, 3.60, 90.0, 'zeynep@iyte.edu.tr','INTERNAL');


MERGE INTO users (user_id, role, username, password_hash, faculty_id, user_source)
KEY(user_id)
VALUES
(1, 'STUDENT',   'student1',   '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', NULL, 'UBYS'),
(2, 'FACULTY',   'faculty1',   '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', 1,    'UBYS'),
(3, 'YGK',       'ygk1',       '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', 1,    'UBYS'),
(4, 'YDYO',      'ydyo1',      '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', NULL, 'UBYS'),
(5, 'REGISTRAR', 'registrar1', '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', NULL, 'UBYS'),
(6, 'FACULTY',   'faculty2',   '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', 2,    'UBYS'),
(7, 'YGK',       'ygk2',       '$2a$10$wH5P6zC/UzRjsN3FhD3kQOFIClzYrzs8xUQWnA8QO3fnYzUXkQF12', 3,    'UBYS');
