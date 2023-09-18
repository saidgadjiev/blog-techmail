truncate table vote;
ALTER TABLE vote ADD CONSTRAINT voter_unique_key UNIQUE (voter, vote);