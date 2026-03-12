package ems.ui;

import ems.model.Role;
import ems.model.User;

public class RoleRouter {

    public static void openWorkspace(User user) {
        if (user.getRole() == Role.ADMIN) {
            new AdminWorkspaceFrame(user).setVisible(true);
        } else if (user.getRole() == Role.MANAGER) {
            new ManagerWorkspaceFrame(user).setVisible(true);
        } else if (user.getRole() == Role.VIEWER) {
            new ViewerWorkspaceFrame(user).setVisible(true);
        } else {
            new PlayerWorkspaceFrame(user).setVisible(true);
        }
    }
}