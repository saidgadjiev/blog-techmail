package ru.gadjini.blog.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.blog.model.User;
import ru.gadjini.blog.model.UserUpdate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean create(User user) {
        int updated = jdbcTemplate.update(
                "insert into users(nickname, lowercase_nickname, about, fullname, email, lowercase_email) " +
                        "values(?, ?, ?, ?, ?, ?) " +
                        "on conflict do nothing",
                user.getNickname(), user.getNickname().toLowerCase(),
                user.getAbout(), user.getFullname(), user.getEmail(), user.getEmail().toLowerCase()
        );

        return updated > 0;
    }

    public List<User> getBynicknameOrEmail(String nickname, String email) {
        return jdbcTemplate.query(
                "select * from users where lowercase_nickname = ? or lowercase_email = ? limit 2",
                (rs, rowNum) -> mapUser(rs),
                nickname.toLowerCase(), email.toLowerCase()
        );
    }

    public User update(String nickname, UserUpdate userUpdate) {
        StringBuilder updateQuery = new StringBuilder();
        StringBuilder updateSet = new StringBuilder();
        StringBuilder updateWhere = new StringBuilder();
        List<Object> args = new ArrayList<>();

        if (userUpdate.isEmailSet()) {
            updateSet.append(
                    "email = ?,\nlowercase_email = ?"
            );
            args.add(userUpdate.getEmail());
            args.add(userUpdate.getEmail().toLowerCase());
        }
        if (userUpdate.isAboutSet()) {
            if (updateSet.length() > 0) {
                updateSet.append(",\n");
            }
            updateSet.append("about = ?");
            args.add(userUpdate.getAbout());
        }
        if (userUpdate.isFullnameSet()) {
            if (updateSet.length() > 0) {
                updateSet.append(",\n");
            }
            updateSet.append("fullname = ?");
            args.add(userUpdate.getFullname());
        }
        if (updateSet.length() == 0) {
            return getBynickname(nickname);
        }
        updateWhere.append("\nwhere lowercase_nickname = ?");
        args.add(nickname.toLowerCase());
        if (userUpdate.isEmailSet()) {
            updateWhere.append("\nand not exists(select 1 from users where lowercase_email = ?)");
            args.add(userUpdate.getEmail().toLowerCase());
        }
        updateQuery.append("update users\n")
                .append("set\n")
                .append(updateSet)
                .append(updateWhere)
                .append("\nRETURNING *");
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement preparedStatement = con
                            .prepareStatement(updateQuery.toString(), Statement.RETURN_GENERATED_KEYS);
                    new ArgumentPreparedStatementSetter(args.toArray(new Object[0])).setValues(preparedStatement);

                    return preparedStatement;
                },
                generatedKeyHolder
        );
        
        return mapUser(generatedKeyHolder);
    }

    public User getBynickname(String nickname) {
        return jdbcTemplate.query(
                "select * from users where lowercase_nickname = ?",
                rs -> rs.next() ? mapUser(rs) : null,
                nickname.toLowerCase()
        );
    }

    public String getNicknameByEmail(String email) {
        return jdbcTemplate.query(
                "select nickname from users where lowercase_email = ?",
                rs -> rs.next() ? rs.getString("nickname") : null,
                email.toLowerCase()
        );
    }

    public void truncate() {
        jdbcTemplate.execute("truncate table users");
    }

    private User mapUser(GeneratedKeyHolder generatedKeyHolder) {
        Map<String, String> values = (Map<String, String>) (Object) generatedKeyHolder.getKeys();
        if (values == null) {
            return null;
        }
        User user = new User();
        user.setNickname(values.get("nickname"));
        user.setEmail(values.get("email"));
        user.setAbout(values.get("about"));
        user.setFullname(values.get("fullname"));

        return user;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setNickname(rs.getString("nickname"));
        user.setEmail(rs.getString("email"));
        user.setAbout(rs.getString("about"));
        user.setFullname(rs.getString("fullname"));

        return user;
    }
}
