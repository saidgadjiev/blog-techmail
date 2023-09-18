package ru.gadjini.blog.dao;

import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.blog.model.Post;
import ru.gadjini.blog.model.Thread;

import java.sql.*;
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
            args.add(post.getParent() == null ? new SqlParameterValue(Types.INTEGER, null) : post.getParent());
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

    //TODO: create index
    public List<Post> getPosts(String slugOrId, Integer limit,
                               Long since, String sort, Boolean desc) {
        List<Object> args = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        String threadClause;
        try {
            Integer slugIntId = Integer.parseInt(slugOrId);
            threadClause = "where f.id = ?";
            args.add(slugIntId);
        } catch (NumberFormatException ex) {
            threadClause = "where f.lowercase_slug = ?";
            args.add(slugOrId.toLowerCase());
        }
        query.append("with recursive post_rec as(\n" +
                "    SELECT t.*, 1 AS depth, t.id as root_id\n" +
                "    FROM post t inner join thread f on t.thread = f.id\n" +
                threadClause + " and parent is null\n" +
                "    union all\n" +
                "    SELECT t.*, p.depth + 1, p.root_id FROM post_rec p inner join post t on t.parent = p.id\n" +
                ")\n SELECT * FROM post_rec\n");

        desc = desc != null && desc;
        if (since != null) {
            if (desc) {
                query.append(" where id < ?");
            } else {
                query.append(" where id > ?");
            }
            args.add(since);
        }
        String direction = (desc ? "desc" : "asc");
        String orderBy;
        if ("flat".equals(sort)) {
            orderBy = "id " + direction;
        } else if ("tree".equals(sort)) {
            orderBy = "root_id " + direction + ", id " + direction;
        } else {
            orderBy = "root_id " + direction + " , depth, id";
        }

        query.append("\norder by ").append(orderBy).append("\n")
                .append("limit ").append(limit);

        return jdbcTemplate.query(query.toString(), (rs, rowNum) -> map(rs), args.toArray(Object[]::new));
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

    private Post map(ResultSet resultSet) throws SQLException {
        Post post = new Post();

        post.setId(resultSet.getLong("id"));
        post.setForum(resultSet.getString("forum"));

        Timestamp created = resultSet.getTimestamp("created");
        long millisSinceEpoch = created.getTime();
        Instant instant = Instant.ofEpochMilli(millisSinceEpoch);
        OffsetDateTime dt = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));

        post.setCreated(dt);
        post.setAuthor(resultSet.getString("author"));
        post.setMessage(resultSet.getString("message"));
        post.setParent(resultSet.getLong("parent"));
        post.setThread(resultSet.getInt("thread"));

        return post;
    }
}
