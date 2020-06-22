package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.List;
import java.util.Vector;


public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;
    private static Connection connection;
    private static PreparedStatement psInsert;
    private static PreparedStatement psSearch;
    private static PreparedStatement psChange;
    private static PreparedStatement psIsExist;

    public Server() {
        clients = new Vector<>();
        authService = new SqlAuthService();
        ServerSocket server = null;
        Socket socket;

        final int PORT = 8189;


        try {
            connectDatabase();
            prepareAllStatements();
            System.out.println("Соединение с БД");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }


        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился ");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert server != null;
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    //Соединение с базой данных
    private static void connectDatabase() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:maindb.db");
    }

    //Подготовка всех PreparedStatements
    private static void prepareAllStatements() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO users (login, password, nickname) VALUES (?, ?, ?)");
        psSearch = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?");
        psChange = connection.prepareStatement("UPDATE users SET nickname = ? WHERE login = ?");
        psIsExist = connection.prepareStatement("SELECT id FROM users WHERE nickname = ? OR login = ?");
    }

    //Трансляция списка авторизованных пользователей
    private void broadcastClientList(){
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler c: clients) {
            sb.append(c.getNick()).append(" ");
        }
        String msg = sb.toString();
        for (ClientHandler c: clients) {
            c.sendMsg(msg);
        }
    }

    /**
     * Создание нового пользователя
     * @param login
     * @param password
     * @param nicname
     * @throws SQLException
     */
    public static void createUser(String login, String password, String nicname) throws SQLException {
        psInsert.setString(1, login);
        psInsert.setString(2, password);
        psInsert.setString(3, nicname);
        psInsert.executeUpdate();
    }

    /**
     * Проверка на совпадения в БД пользователей. Ник и логин должны быть уникальными
     * @param login
     * @param nickname
     * @return
     * @throws SQLException
     */
    public static ResultSet isExist(String login, String nickname) throws SQLException {
        psIsExist.setString(1, nickname);
        psIsExist.setString(2, login);
        return psIsExist.executeQuery();
    }

    /**
     * Поиск в БД пользователя по логину и паролю
     * @param login
     * @param password
     * @return
     * @throws SQLException
     */
    public static ResultSet authorization(String login, String password) throws SQLException {
        psSearch.setString(1, login);
        psSearch.setString(2, password);
        return psSearch.executeQuery();
    }

    /**
     * Смена ника в БД
     * @param newNick
     * @param login
     * @throws SQLException
     */
    public static void changeNick(String newNick, String login) throws SQLException {
        psChange.setString(1, newNick);
        psChange.setString(2, login);
        psChange.executeUpdate();
    }

    /**
     * Трансляция сообщения
     * @param nick
     * @param msg
     */
    public void broadcastMsg(String nick, String msg){
        for (ClientHandler c:clients) {
            c.sendMsg(nick + ": " + msg);
        }
    }

    /**
     * Отправка приватного сообщения
     * @param sender
     * @param nick
     * @param msg
     */
    public void sendTargetMsg(ClientHandler sender, String nick, String msg){
        for (ClientHandler c:clients) {
            if (c.getNick().equals(nick)) {
                c.sendMsg(sender.getNick() + "->" + nick + ": " + msg);
                sender.sendMsg(sender.getNick() + "->" + nick + ": " + msg);
                return;
            }
            sender.sendMsg("Пользователя с ником " + nick + " не найдено");
        }
    }

    /**
     * Добавление ника в список авторизованных
     * @param clientHandler
     */
    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    }

    /**
     * Удаление ника из списка авторизованных
     * @param clientHandler
     */
    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    }

    /**
     * Проверка авторизован ли пользователь с данным ником
     * @param login
     * @return
     */
    public boolean isLoginAuthorized(String login){
        for (ClientHandler c: clients) {
            if (c.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }
}
