package server;

public class SqlAuthService implements AuthService {
    @Override
    public String getNicknameByLoginAndPassword(String login, String pass){
        return SQLHandler.authorization(login, pass);
    }

    @Override
    public boolean registration(String login, String password, String nickname){
        return SQLHandler.createUser(login, password, nickname);
    }
}
