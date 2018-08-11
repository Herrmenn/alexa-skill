drop table pers_core_data;

CREATE TABLE pers_core_data (
id int NOT NULL AUTO_INCREMENT,
firstname varchar(25) ,
lastname varchar(25),
email varchar(50),
url varchar(255),
office varchar(255),
phone varchar(255),
PRIMARY KEY (id)
);



/* ---------------- */

CREATE TABLE module_courses (
id int NOT NULL AUTO_INCREMENT,
course varchar(255),
url varchar(255),
PRIMARY KEY (id)
);

CREATE TABLE module_lectures (
id int NOT NULL AUTO_INCREMENT,
course_id int,
lecture varchar(255),
url varchar(255),
PRIMARY KEY (id),
FOREIGN KEY (course_id) REFERENCES module_courses(id)
);

CREATE TABLE module_info (
id int NOT NULL AUTO_INCREMENT,
lecture_id int,
ects int,
semester varchar(25),
exam_type varchar(255),
lecturer varchar(255),
learning_goals varchar(65535),
content varchar(65535),
PRIMARY KEY (id),
FOREIGN KEY (lecture_id) REFERENCES module_lectures(id)
);
