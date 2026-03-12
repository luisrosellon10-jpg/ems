package ems.ui.panels;

import ems.dao.AnnouncementDao;
import ems.model.Announcement;
import ems.model.User;
import ems.ui.Ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AnnouncementListPanel extends JPanel {
    private final User user;
    private final AnnouncementDao dao = new AnnouncementDao();
    private final DefaultListModel<Announcement> model = new DefaultListModel<>();
    private final JList<Announcement> list = new JList<>(model);

    public AnnouncementListPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(8, 8));
        JLabel title = new JLabel("Announcements");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        list.setCellRenderer(new AnnouncementRenderer());
        add(title, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        load();
    }

    private void load() {
        try {
            model.clear();
            List<Announcement> anns = dao.listForUser(user);
            for (Announcement a : anns) model.addElement(a);
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static class AnnouncementRenderer extends JLabel implements ListCellRenderer<Announcement> {
        public Component getListCellRendererComponent(JList<? extends Announcement> list, Announcement a, int index, boolean isSelected, boolean cellHasFocus) {
            setText("<html><b>" + a.getTitle() + "</b>&nbsp;(" + a.getTarget() + ")<br/>"
                    + a.getBody().replace("\n", "<br/>")
                    + "<br/><span style='font-size:smaller;'>Posted " + a.getPostedAt() + " by " + a.getAuthorName() + "</span></html>");
            setOpaque(true);
            setBackground(isSelected ? new Color(220,240,255) : Color.WHITE);
            return this;
        }
    }
}