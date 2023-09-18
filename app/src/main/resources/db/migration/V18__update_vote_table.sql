alter table vote add column thread_id int not null references thread(id);
CREATE INDEX vote_thread ON vote USING btree (thread_id);