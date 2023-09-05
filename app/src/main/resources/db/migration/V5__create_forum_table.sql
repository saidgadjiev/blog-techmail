create table if not exists forum (
    slug varchar(64) primary key,
    title varchar(512),
    author varchar(256) references users(nickname)
);