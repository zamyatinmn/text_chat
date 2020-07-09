package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

public class ClientHandler {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();
                        //регистрация
                        if (str.startsWith("/reg ")) {
                            logger.fine(str);
                            String[] token = str.split(" ");

                            if (token.length < 4) {
                                continue;
                            }

                            boolean succeed = server
                                    .getAuthService()
                                    .registration(token[1], token[2], token[3]);
                            if (succeed) {
                                sendMsg("Регистрация прошла успешно");
                            } else {
                                sendMsg("Регистрация  не удалась. \n" +
                                        "Возможно логин уже занят, или данные содержат пробел");
                            }
                        }
                        //авторизация
                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");

                            logger.fine(str);
                            if (token.length < 3) {
                                continue;
                            }

                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            login = token[1];
                            if (newNick != null) {
                                if (!server.isLoginAuthorized(login)){
                                    socket.setSoTimeout(0);
                                    sendMsg("/authok " + newNick);
                                    nick = newNick;

                                    server.subscribe(this);
                                    logger.info("Клиент: " + nick + " подключился");
                                    break;
                                }else {
                                    sendMsg("Пользователь с таким логином уже авторизован");
                                }

                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();
                        //блок системных сообщений
                        if (str.startsWith("/")){
                            logger.fine(str);
                            if (str.equals("/help")){
                                sendMsg("Список системных комманд:\n" +
                                        "/end - логаут\n" +
                                        "/change newNick - смена ника на указанный\n" +
                                        "/w nick text - отправка приватного сообщения");
                                continue;
                            }

                            //логаут
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            //Смена ника
                            if (str.startsWith("/change ")){
                                String[] token = str.split("\\s");
                                if (token.length != 2){
                                    continue;
                                }
                                if (SQLHandler.changeNick(token[1], login)){
                                    sendMsg("/newnick " + token[1]);
                                    this.nick = token[1];
                                    server.broadcastClientList();
                                } else {
                                    sendMsg("Не удалось сменить ник");
                                }

//                                sendMsg("/end");
                                continue;
                            }

                            //Отправка сообщения одному клиенту
                            if (str.startsWith("/w")){
                                String[] token = str.split("\\s", 3);
                                if (token.length < 3){
                                    continue;
                                }
                                server.sendTargetMsg(this, token[1], token[2]);
                                continue;
                            }
                        }
                        server.broadcastMsg(nick, str);
                    }
                }
                catch (SocketTimeoutException e){
                    logger.info("Таймаут бездействия: " + nick);
                }
                    catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    logger.info("Клиент: " + nick + " отключился");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogin() {
        return login;
    }

    public String getNick() {
        return nick;
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}