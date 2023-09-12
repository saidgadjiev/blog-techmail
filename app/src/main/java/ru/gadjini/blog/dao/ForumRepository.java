package ru.gadjini.blog.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.blog.model.Forum;

import java.sql.*;
import java.util.Map;

@Repository
public class ForumRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ForumRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void truncate() {
        jdbcTemplate.execute("truncate table forum cascade");
    }

    public Forum create(Forum forum) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO forum(slug, lowercase_slug, title, author) \n" +
                            "select ?, ?, ?, nickname\n" +
                            "    from users where lowercase_nickname = ?\n" +
                            "on conflict do nothing returning *", Statement.RETURN_GENERATED_KEYS);
                    new ArgumentPreparedStatementSetter(new Object[] {forum.getSlug(),
                            forum.getSlug().toLowerCase(), forum.getTitle(),
                            forum.getUser().toLowerCase()}).setValues(preparedStatement);

                    return preparedStatement;
                }, generatedKeyHolder);

        return mapForum(generatedKeyHolder);
    }

    public Forum getBySlug(String slug) {
        return jdbcTemplate.query("SELECT * FROM forum where lowercase_slug = ?",
                rs -> rs.next() ? mapForum(rs) : null,
                slug.toLowerCase()
        );
    }

    public boolean existsBySlug(String slug) {
        return jdbcTemplate.query("SELECT true FROM forum where lowercase_slug = ?",
                ResultSet::next,
                slug.toLowerCase()
        );
    }

    private Forum mapForum(ResultSet resultSet) throws SQLException {
        Forum forum = new Forum();
        forum.setSlug(resultSet.getString("slug"));
        forum.setTitle(resultSet.getString("title"));
        forum.setUser(resultSet.getString("author"));

        return forum;
    }

    private Forum mapForum(GeneratedKeyHolder generatedKeyHolder) {
        Map<String, String> keys = (Map<String, String>) (Object) generatedKeyHolder.getKeys();
        if (keys == null) {
            return null;
        }
        Forum forum = new Forum();
        forum.setSlug(keys.get("slug"));
        forum.setTitle(keys.get("title"));
        forum.setUser(keys.get("author"));

        return forum;
    }
}
