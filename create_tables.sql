create table student (
	stu_id  INTEGER PRIMARY KEY AUTO_INCREMENT,
	f_name VARCHAR(50),
	l_name VARCHAR(50),
	username VARCHAR(50)
);

create table class (
	c_id  INTEGER PRIMARY KEY AUTO_INCREMENT,
	desc VARCHAR(50)
);

create table section (
	sec_id  INTEGER PRIMARY KEY AUTO_INCREMENT,
	number INTEGER NOT NULL,
	last_name VARCHAR(50),
	start_date DATE,
	end_date DATE,
	start_time VARCHAR(50),
	end_time VARCHAR(50),
	c_id INTEGER NOT NULL REFERENCES class
);

create table enrolled (
	e_id  INTEGER PRIMARY KEY AUTO_INCREMENT,
	stu_id INTEGER NOT NULL REFERENCES student,
	sec_id INTEGER NOT NULL REFERENCES section
);

create table type (
	t_id  INTEGER PRIMARY KEY AUTO_INCREMENT,
	type VARCHAR(12),
	weight DECIMAL(2,1),
	sec_id INTEGER NOT NULL REFERENCES section
);

create table assignment (
	a_id  INTEGER PRIMARY KEY AUTO_INCREMENT,
	desc VARCHAR(50),
	title VARCHAR(50),
	tot_points INTEGER NOT NULL,
	rec_points INTEGER,
	stu_id INTEGER NOT NULL REFERENCES student,
	t_id INTEGER NOT NULL REFERENCES type
);