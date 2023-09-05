package ru.gadjini.blog.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.blog.model.Forum;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class ForumRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ForumRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean create(Forum forum) {
        int created = jdbcTemplate.update(
                "INSERT INTO forum(slug, lowercase_slug, title, author) VALUES(?, ?, ?, ?) ON CONFLICT DO NOTHING",
                forum.getSlug(), forum.getSlug().toLowerCase(), forum.getTitle(), forum.getUser()
        );

        return created > 0;
    }

    public Forum getBySlug(String slug) {
        return jdbcTemplate.query("SELECT * FROM forum where lowercase_slug = ?",
                rs -> rs.next() ? mapForum(rs) : null,
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
}
