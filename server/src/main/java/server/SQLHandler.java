package server;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psAuth;
    private static PreparedStatement psChangeNick;
    private static PreparedStatement psAddMessage;
    private static PreparedStatement psGetHistory;

    public static boolean connectDatabase(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:maindb.db");
            prepareAllStatements();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void prepareAllStatements() throws SQLException {
        psRegistration = connection.prepareStatement("INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);");
        psAuth = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
        psChangeNick = connection.prepareStatement("UPDATE users SET nickname = ? WHERE login = ?;");
    }

    public static boolean createUser(String login, String password, String nicname){
        try{
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.setString(3, nicname);
            psRegistration.executeUpdate();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public static String authorization(String login, String password){
        String nick = null;
        try {
            psAuth.setString(1, login);
            psAuth.setString(2, password);
            ResultSet rs = psAuth.executeQuery();
            if (rs.next()){
                nick = rs.getString(1);
            }
            rs.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        return nick;
    }

    public static boolean changeNick(String newNick, String login){
        try{
            psChangeNick.setString(1, newNick);
            psChangeNick.setString(2, login);
            psChangeNick.executeUpdate();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
}
