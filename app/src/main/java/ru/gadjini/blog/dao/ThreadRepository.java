package ru.gadjini.blog.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.blog.model.Thread;

import java.sql.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ThreadRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ThreadRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Thread create(Thread thread) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con
                    .prepareStatement("INSERT INTO thread(title, author, forum, message, slug, created)\n" +
                                    "select ?, u.nickname, f.slug, ?, ?, ?\n" +
                                    "from forum f, users u where f.lowercase_slug = ? and u.lowercase_nickname = ?\n" +
                                    "ON CONFLICT DO NOTHING\n" +
                                    "RETURNING *",
                            Statement.RETURN_GENERATED_KEYS);

            new ArgumentPreparedStatementSetter(new Object[]{thread.getTitle(), thread.getMessage(), thread.getSlug(),
                    thread.getCreated(), thread.getForum().toLowerCase(), thread.getAuthor().toLowerCase()})
                    .setValues(preparedStatement);

            return preparedStatement;
        }, generatedKeyHolder);

        return map(generatedKeyHolder);
    }

    public List<Thread> getThreads(String forum, Integer limit, OffsetDateTime since, Boolean desc) {
        List<Object> args = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query
                .append("SELECT t.* FROM thread t inner join forum f on t.forum = f.slug\n")
                .append("WHERE f.lowercase_slug = ?");
        args.add(forum.toLowerCase());
        desc = desc != null && desc;
        if (since != null) {
            if (desc) {
                query.append(" and created <= ?");
            } else {
                query.append(" and created >= ?");
            }
            args.add(since);
        }

        query.append("\norder by created ").append(desc ? "desc": "asc").append("\n")
                .append("limit ").append(limit);

        return jdbcTemplate.query(query.toString(), (rs, rowNum) -> map(rs), args.toArray(Object[]::new));
    }

    private Thread map(ResultSet resultSet) throws SQLException {
        Thread thread = new Thread();
        thread.setId(resultSet.getInt("id"));
        thread.setTitle(resultSet.getString("title"));
        thread.setMessage(resultSet.getString("message"));
        thread.setSlug(resultSet.getString("slug"));
        thread.setForum(resultSet.getString("forum"));
        thread.setAuthor(resultSet.getString("author"));

        Timestamp created = resultSet.getTimestamp("created");
        if (created != null) {
            long millisSinceEpoch = created.getTime();
            Instant instant = Instant.ofEpochMilli(millisSinceEpoch);
            OffsetDateTime dt = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
            thread.setCreated(dt);
        }

        return thread;
    }

    private Thread map(GeneratedKeyHolder generatedKeyHolder) {
        Map<String, String> keys = (Map<String, String>) (Object) generatedKeyHolder.getKeys();
        if (keys == null) {
            return null;
        }
        Thread thread = new Thread();
        thread.setId((Integer) (Object) keys.get("id"));
        thread.setTitle(keys.get("title"));
        thread.setMessage(keys.get("message"));
        thread.setSlug(keys.get("slug"));
        thread.setForum(keys.get("forum"));
        thread.setAuthor(keys.get("author"));

        Timestamp created = (Timestamp) (Object) keys.get("created");
        if (created != null) {
            long millisSinceEpoch = created.getTime();
            Instant instant = Instant.ofEpochMilli(millisSinceEpoch);
            OffsetDateTime dt = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
            thread.setCreated(dt);
        }

        return thread;
    }
}
