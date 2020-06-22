package server;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlAuthService implements AuthService {
    @Override
    public String getNicknameByLoginAndPassword(String login, String pass) throws SQLException {
        ResultSet rs = Server.authorization(login, pass);
        while (rs.next()){
            return rs.getString(1);
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) throws SQLException {
        ResultSet rs = Server.isExist(login, nickname);
        if (rs.next()){
            return false;
        }
        Server.createUser(login, password, nickname);
        return true;
    }
}
