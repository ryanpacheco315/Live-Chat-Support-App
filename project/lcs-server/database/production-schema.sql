drop database if exists live_chat_support;
create database live_chat_support;
use live_chat_support;

create table user (
                      id int primary key auto_increment,
                      full_name varchar(120) not null,
                      username varchar(50) not null unique,
                      password text not null,
                      role varchar(20) not null
);

create table chat (
                      id int primary key auto_increment,
                      client_id int not null,
                      agent_id int,
                      status varchar(20) not null,
                      problem_id int,
                      time_id int
);

create table problem (
                         id int primary key auto_increment,
                         category varchar(20) not null,
                         subcategory varchar(20),
                         description text not null
);

create table time_record (
                             id int primary key auto_increment,
                             created_at timestamp not null,
                             closed_at timestamp
);

create table message (
                         id int primary key auto_increment,
                         chat_id int not null,
                         sender_id int,
                         body text not null,
                         created_at timestamp not null
);

alter table chat add constraint fk_chat_client foreign key (client_id) references user(id);
alter table chat add constraint fk_chat_agent foreign key (agent_id) references user(id);
alter table chat add constraint fk_chat_problem foreign key (problem_id) references problem(id);
alter table chat add constraint fk_chat_time foreign key (time_id) references time_record(id);
alter table message add constraint fk_message_chat foreign key (chat_id) references chat(id);
alter table message add constraint fk_message_sender foreign key (sender_id) references user(id);
