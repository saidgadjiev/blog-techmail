create table if not exists post (
    id serial primary key,
    created timestamptz not null default now(),
    author varchar(256) not null references users(nickname),
    forum varchar(64) references forum(slug),
    is_edited boolean not null default false,
    message text not null,
    parent int not null default 0 references post(id),
    thread int not null references thread(id)
);