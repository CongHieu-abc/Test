package VoCongHieu_DE181064.example;

public class AccountService {

    public boolean registerAccount(String username, String password, String email) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        if (password == null || password.length() <= 6) {
            return false;
        }
        if (!isValidEmail(email)) {
            return false;
        }
        return true;
    }

    public boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public boolean isValidUsername(String username) {
        return username != null && !username.isEmpty();
    }

    // Kiểm tra password
    public boolean isValidPassword(String password) {
        return password != null && password.length() > 6;
    }
}

