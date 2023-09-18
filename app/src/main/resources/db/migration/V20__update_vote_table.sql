TRUNCATE table vote;
ALTER TABLE vote DROP CONSTRAINT voter_unique_key;
ALTER TABLE vote ADD CONSTRAINT voter_unique_key UNIQUE (voter);