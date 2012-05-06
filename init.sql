create table users (id identity, name varchar(100), password varchar(50), is_admin boolean primary key (id));
create table institutes (id identity, name varchar(100), primary key (id));
create table players (id identity, name varchar(100), institute_id bigint,primary key (id),foreign key (institute_id) references institutes(id));

insert into institutes (name) values ('顺德职业技术学院');
insert into institutes (name) values ('深圳职业技术学院');
insert into institutes (name) values ('清远职业技术学院');
insert into institutes (name) values ('韶关学院医学院');
insert into institutes (name) values ('嘉应学院医学院');
insert into institutes (name) values ('广州医学院护理学院');
insert into institutes (name) values ('广州医学院从化学院');
insert into institutes (name) values ('肇庆医学高等专科学校');

create table projects (id identity, name varchar(100), primary key (id));
create table scores (id identity, player_id bigint, project_id bigint, score decimal(10,2), primary key (id), foreign key (player_id) references players(lottery), foreign key (project_id) references projects(id));
create index pl_prj_idx on scores (player_id,project_id);

insert into projects (name) values ('临床案例分析');
insert into projects (name) values ('单人徒手心肺复苏术');
insert into projects (name) values ('心电检测技术');
insert into projects (name) values ('密闭式静脉输液技术');

