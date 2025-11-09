
package VoCongHieu_DE181064.example;

import java.util.List;

public class AccountService {

    public boolean registerAccount(String username, String password, String email) {
        return isValidUsername(username)
                && isValidPassword(password)
                && isValidEmail(email);
    }

    public boolean isUsernameNotEmpty(String username) {
        return username != null && !username.trim().isEmpty();
    }

    public boolean isUsernameLengthValid(String username) {
         return username != null && username.trim().length() >= 3;
    }

    public boolean isValidUsername(String username) {
         return username != null && username.trim().matches("^[A-Z][A-Za-z0-9_ ]{2,19}$");
    }

    public boolean isPasswordNotEmpty(String password) {
        return password != null && !password.trim().isEmpty();
    }

    public boolean isPasswordLengthValid(String password) {
        return password != null && password.trim().length() >= 6;
    }

    public boolean isValidPassword(String password) {
        return password != null && password.trim().matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[_.@$!%*?&])[A-Za-z\\d_.@$!%*?&]{6,}$");
    }

    public boolean isEmailNotEmpty(String email) {
        return email != null && !email.trim().isEmpty();
    }

    public boolean isValidEmail(String email) {
        return email != null && email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,6}$");
    }

    public boolean isDuplicateEmail(String email, List<String> emails) {
        if (email == null || emails == null) return false;
        for (String e : emails) {
            if (email.equalsIgnoreCase(e.trim())) {
                return true;
            }
        }
        return false;
    }

}
