package util;

import models.Account;
import models.faculty.Faculty;
import models.enums.UserType;

public class AuthorizationUtil {
    private AuthorizationUtil() {}

    public static boolean hasElevatedAccess(Account account) {
        if (account.getUserType() == UserType.ADMIN) return true;
        if (account.getUserType() == UserType.FACULTY) {
            return "Department Head".equalsIgnoreCase(
                    ((Faculty) account).getPosition());
        }
        return false;
    }
}
