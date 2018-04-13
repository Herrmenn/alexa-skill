drop table pers_core_data;

CREATE TABLE pers_core_data (
id int NOT NULL AUTO_INCREMENT,
firstname varchar(25),
lastname varchar(25),
email varchar(50) NOT NULL,
url varchar(255) NOT NULL,
office varchar(255),
PRIMARY KEY (id)
);
