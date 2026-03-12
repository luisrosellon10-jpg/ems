package ems.tools;

import ems.dao.UserDao;
import ems.model.AccountStatus;
import ems.model.Role;
import ems.security.PasswordHasher;

public class AdminBootstrapTool {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: AdminBootstrapTool <adminEmail> <adminPassword>");
            return;
        }

        String email = args[0].trim().toLowerCase();
        String pass = args[1];

        UserDao userDao = new UserDao();
        var existing = userDao.findByEmail(email);
        if (existing.isPresent()) {
            System.out.println("Admin already exists: " + email);
            return;
        }

        String hash = PasswordHasher.hash(pass);
        long id = userDao.insertUser(email, hash, Role.ADMIN, AccountStatus.APPROVED);
        System.out.println("Created ADMIN id=" + id + " email=" + email);
    }
}