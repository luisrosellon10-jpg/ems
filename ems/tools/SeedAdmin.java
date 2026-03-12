package ems.tools;

import ems.dao.UserDao;
import ems.model.AccountStatus;
import ems.model.Role;
import ems.model.User;
import ems.security.PasswordHasher;

public final class SeedAdmin {
    private SeedAdmin() {}

    public static final String DEFAULT_ADMIN_EMAIL = "admin@ems.com";
    public static final String DEFAULT_ADMIN_PASSWORD = "Admin12345";

    public static void ensureAdminExists() {
        try {
            UserDao userDao = new UserDao();

            var existing = userDao.findByEmail(DEFAULT_ADMIN_EMAIL);
            if (existing.isPresent()) return;

            User u = new User();
            u.setEmail(DEFAULT_ADMIN_EMAIL);
            u.setPasswordHash(PasswordHasher.hash(DEFAULT_ADMIN_PASSWORD));
            u.setRole(Role.ADMIN);
            u.setStatus(AccountStatus.APPROVED);

            // profile fields can be null
            u.setFullName("System Admin");

            long id = userDao.insertUser(u);
            System.out.println("[EMS] Seeded default admin id=" + id + " email=" + DEFAULT_ADMIN_EMAIL);

        } catch (Exception e) {
            System.err.println("[EMS] Failed to seed admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}