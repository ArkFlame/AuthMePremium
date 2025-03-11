package com.arkflame.authmepremium.providers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.md_5.bungee.config.Configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLProvider implements DataProvider {

    private final HikariDataSource dataSource;

    public MySQLProvider(Configuration config) {
        String jdbcUrl = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getInt("mysql.port") +
                "/" + config.getString("mysql.database");
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(config.getString("mysql.username"));
        hikariConfig.setPassword(config.getString("mysql.password"));
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        dataSource = new HikariDataSource(hikariConfig);

        createTable();
    }

    private void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS authme_premium_users (" +
            "name VARCHAR(50) NOT NULL PRIMARY KEY," +
            "premium BOOLEAN NOT NULL DEFAULT FALSE," +
            "premium_uuid BOOLEAN NOT NULL DEFAULT FALSE)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean getPremium(String name) {
        String query = "SELECT premium FROM authme_premium_users WHERE name = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            return executeQueryAndGetPremium(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPremium(String name, boolean premium) {
        String query = "REPLACE INTO authme_premium_users (name, premium) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setBoolean(2, premium);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean getPremiumUUID(String name) {
        String query = "SELECT premium_uuid FROM authme_premium_users WHERE name = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            return executeQueryAndGetPremiumUUID(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPremiumUUID(String name, boolean premiumUUID) {
        String query = "REPLACE INTO authme_premium_users (name, premium_uuid) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setBoolean(2, premiumUUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Boolean executeQueryAndGetPremiumUUID(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getBoolean("premium_uuid");
            }
        } catch (SQLException ex) {
            /* Older data */
        }
        return null;
    }

    @Override
    public void clear(String name) {
        String query = "DELETE FROM authme_premium_users WHERE name = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        String query = "TRUNCATE TABLE authme_premium_users";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Boolean executeQueryAndGetPremium(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getBoolean("premium");
            }
        }
        return null;
    }

    public void closeConnection() {
        dataSource.close();
    }

    public static String generateMySQLURIExample() {
        return "mysql:\n" +
                "  host: localhost\n" +
                "  port: 3306\n" +
                "  database: authme\n" +
                "  username: your_username\n" +
                "  password: your_password";
    }
}
