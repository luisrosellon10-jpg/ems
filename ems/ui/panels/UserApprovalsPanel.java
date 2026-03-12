package ems.ui.panels;

import ems.dao.UserDao;
import ems.model.AccountStatus;
import ems.model.User;
import ems.service.EmailService;
import ems.ui.Ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class UserApprovalsPanel extends JPanel {

    private final UserDao userDao = new UserDao();
    private final EmailService emailService = new EmailService();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Email", "Full Name", "Gamer Tag", "Gender", "DOB", "Address", "Role", "Status", "Created"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    public UserApprovalsPanel() {
        super(new BorderLayout());

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JButton refreshBtn = new JButton("Refresh");
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");

        refreshBtn.addActionListener(e -> loadData());
        approveBtn.addActionListener(e -> updateSelected(AccountStatus.APPROVED));
        rejectBtn.addActionListener(e -> updateSelected(AccountStatus.REJECTED));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);
        top.add(approveBtn);
        top.add(rejectBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        try {
            model.setRowCount(0);
            List<User> pending = userDao.listPendingUsers();
            for (User u : pending) {
                model.addRow(new Object[]{
                        u.getId(),
                        nv(u.getEmail()),
                        nv(u.getFullName()),
                        nv(u.getGamerTag()),
                        nv(u.getGender()),
                        formatDob(u.getDateOfBirth()),
                        nv(u.getAddress()),
                        u.getRole(),
                        u.getStatus(),
                        u.getCreatedAt()
                });
            }
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void updateSelected(AccountStatus status) {
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a user first"); return; }

        long userId = ((Number) model.getValueAt(row, 0)).longValue();
        String email = String.valueOf(model.getValueAt(row, 1));

        if (!Ui.confirm(this, "Set " + email + " to " + status + "?")) return;

        try {
            userDao.updateStatus(userId, status);

            try {
                if (status == AccountStatus.APPROVED) emailService.sendAccountApprovedEmail(email);
                else if (status == AccountStatus.REJECTED) emailService.sendAccountRejectedEmail(email);
            } catch (Exception mailEx) {
                System.err.println("[EMS] Failed to send status email to " + email + ": " + mailEx.getMessage());
            }

            Ui.info(this, "Updated: " + email + " -> " + status);
            loadData();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static String nv(String s) { return s == null ? "" : s; }
    private static String formatDob(Date d) { return d == null ? "" : d.toString(); }
}