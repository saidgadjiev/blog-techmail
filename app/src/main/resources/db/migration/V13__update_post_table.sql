alter table post add column forum varchar(64) references forum(slug);