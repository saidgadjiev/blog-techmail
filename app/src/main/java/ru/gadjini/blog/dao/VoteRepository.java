package ru.gadjini.blog.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.blog.model.Vote;

import java.util.ArrayList;
import java.util.List;

@Repository
public class VoteRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public VoteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean create(String slugOrId, Vote vote) {
        StringBuilder insert = new StringBuilder();
        insert.append("INSERT INTO vote(voter, vote, thread_id)\n");
        List<Object> args = new ArrayList<>();

        boolean slugTypeString = false;
        Integer slugIntId = null;
        try {
            slugIntId = Integer.parseInt(slugOrId);
        } catch (NumberFormatException ex) {
            slugTypeString = true;
        }

        insert.append("select u.nickname, ?, th.id\n" +
                "from thread th, users u where u.lowercase_nickname = ?");

        args.add(vote.getVoice().getValue());
        args.add(vote.getNickname().toLowerCase());

        if (slugTypeString) {
            args.add(slugOrId.toLowerCase());
            insert.append(" and th.lowercase_slug = ?");
        } else {
            args.add(slugIntId);
            insert.append(" and th.id = ?");
        }
        insert.append("\n ON CONFLICT(voter, thread_id) DO UPDATE set vote = excluded.vote");
        int created = jdbcTemplate.update(insert.toString(), args.toArray(new Object[0]));

        return created > 0;
    }
}
