package ru.gadjini.blog.dao;

import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.blog.model.Post;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void truncate() {
        jdbcTemplate.execute("truncate table post cascade");
    }

    public void create(String slugOrId, List<Post> posts) {
        StringBuilder insert = new StringBuilder();
        insert.append("insert into post(author, forum, message, parent, thread)\n");
        StringBuilder values = new StringBuilder();
        List<Object> args = new ArrayList<>();

        boolean slugTypeString = false;
        Integer slugIntId = null;
        try {
            slugIntId = Integer.parseInt(slugOrId);
        } catch (NumberFormatException ex) {
            slugTypeString = true;
        }

        for (Post post : posts) {
            if (values.length() > 0) {
                values.append("UNION ALL\n");
            }
            values.append("select u.nickname, th.forum, ?, ?, th.id from users u, thread th " +
                    "where u.lowercase_nickname = ?");

            args.add(post.getMessage());
            args.add(post.getParent());
            args.add(post.getAuthor().toLowerCase());

            if (slugTypeString) {
                args.add(slugOrId);
                values.append(" and th.slug = ?");
            } else {
                args.add(slugIntId);
                values.append(" and th.id = ?");
            }
            values.append("\n");
        }
        insert.append(values);
        insert.append("RETURNING *");
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement preparedStatement = con
                            .prepareStatement(insert.toString(), Statement.RETURN_GENERATED_KEYS);
                    new ArgumentPreparedStatementSetter(args.toArray(new Object[0]))
                            .setValues(preparedStatement);
                    return preparedStatement;
                },
                generatedKeyHolder
        );
        map(posts, generatedKeyHolder);
    }

    private void map(List<Post> posts, GeneratedKeyHolder generatedKeyHolder) {
        List<Map<String, Object>> keyList = generatedKeyHolder.getKeyList();

        if (keyList.isEmpty()) {
            return;
        }

        for (int i = 0; i < posts.size(); i++) {
            Map<String, Object> vals = keyList.get(i);
            map(posts.get(i), vals);
        }
    }

    private void map(Post post, Map<String, Object> vals) {
        post.setId((Long) vals.get("id"));
        post.setForum((String) vals.get("forum"));

        Timestamp created = (Timestamp) vals.get("created");
        long millisSinceEpoch = created.getTime();
        Instant instant = Instant.ofEpochMilli(millisSinceEpoch);
        OffsetDateTime dt = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));

        post.setCreated(dt);
        post.setThread((Integer) vals.get("thread"));
    }
}
