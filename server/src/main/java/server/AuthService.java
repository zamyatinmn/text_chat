package server;

public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String pass);
    boolean registration(String login, String password, String nickname);
}
