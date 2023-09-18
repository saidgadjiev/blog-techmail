create table if not exists vote(
    id bigserial primary key,
    voter varchar(256) not null references users(nickname),
    vote int not null
);