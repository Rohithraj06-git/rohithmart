package com.rohith6.rohith6mart.dao;

import com.rohith6.rohith6mart.config.DatabaseConfig;
import com.rohith6.rohith6mart.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public User findByEmail(String email) throws SQLException {
        String sql = """
                SELECT id, name, email, password, role
                FROM users
                WHERE email = ?
                """;

        try (Connection connection =
                     DatabaseConfig.getDataSource().getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {

                    return new User(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("email"),
                            resultSet.getString("password"),
                            resultSet.getString("role")
                    );
                }
                return null;
            }
        }
    }
}