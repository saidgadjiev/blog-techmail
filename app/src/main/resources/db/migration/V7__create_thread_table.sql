create table if not exists thread (
    id serial primary key,
    title varchar(512) not null,
    author varchar(256) not null references users(nickname),
    forum varchar(64) references forum(slug),
    message text not null,
    slug varchar(64) unique,
    created timestamp not null default now()
);

alter table forum alter column author set not null;
alter table forum alter column title set not null;