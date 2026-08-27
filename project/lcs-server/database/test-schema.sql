drop database if exists live_chat_support_test;
create database live_chat_support_test;
use live_chat_support_test;

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

delimiter //
create procedure set_known_good_state()
begin
    delete from message;
    alter table message auto_increment = 1;
    delete from chat;
    alter table chat auto_increment = 1;
    delete from time_record;
    alter table time_record auto_increment = 1;
    delete from problem;
    alter table problem auto_increment = 1;
    delete from user;
    alter table user auto_increment = 1;

    insert into user (full_name, username, password, role) values
        ('Alice Client', 'alice', 'password', 'CLIENT'),
        ('Bob Agent', 'bob', 'password', 'AGENT'),
        ('Carol Admin', 'carol', 'password', 'ADMIN');

    insert into problem (category, subcategory, description) values
        ('HARDWARE', 'LAPTOP', 'Laptop will not turn on.'),
        ('SOFTWARE', 'EMAIL', 'Cannot log in to email.');

    insert into time_record (created_at, closed_at) values
        ('2026-01-01 09:00:00', null),
        ('2026-01-02 10:00:00', null);

    insert into chat (client_id, agent_id, status, problem_id, time_id) values
        (1, 2, 'ACTIVE', 1, 1),
        (1, null, 'WAITING', 2, 2);

    insert into message (chat_id, sender_id, body, created_at) values
        (1, 1, 'My laptop will not turn on.', '2026-01-01 09:01:00'),
        (1, 2, 'Hi, I can help with that.', '2026-01-01 09:02:00');
end //
delimiter ;
