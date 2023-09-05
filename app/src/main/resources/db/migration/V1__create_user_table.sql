create table if not exists users (
    id serial primary key,
    nickname varchar(256) not null unique,
    about text,
    fullname varchar(512),
    email varchar(512) unique
);