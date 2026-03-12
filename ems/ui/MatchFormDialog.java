package ems.ui;

import ems.dao.MatchDao;
import ems.dao.TeamDao;
import ems.model.Match;
import ems.model.MatchStatus;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class MatchFormDialog extends JDialog {

    private final MatchDao dao = new MatchDao();
    private final TeamDao teamDao = new TeamDao();

    private final long tournamentId;
    private final Match editing;

    private final JComboBox<TeamComboItem> teamABox = new JComboBox<>();
    private final JComboBox<TeamComboItem> teamBBox = new JComboBox<>();

    private final JTextField scheduledAtField = new JTextField(); // yyyy-mm-dd hh:mm:ss (optional)
    private final JTextField scoreAField = new JTextField(); // optional
    private final JTextField scoreBField = new JTextField(); // optional
    private final JComboBox<MatchStatus> statusBox = new JComboBox<>(MatchStatus.values());

    private boolean saved = false;

    public MatchFormDialog(Frame owner, long tournamentId, Match editing) {
        super(owner, true);
        this.tournamentId = tournamentId;
        this.editing = editing;

        setTitle(editing == null ? "Add Match" : "Edit Match");
        setSize(520, 360);
        setLocationRelativeTo(owner);

        loadTeams();

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Team A"));
        form.add(teamABox);

        form.add(new JLabel("Team B"));
        form.add(teamBBox);

        form.add(new JLabel("Scheduled At (yyyy-mm-dd hh:mm:ss, optional)"));
        form.add(scheduledAtField);

        form.add(new JLabel("Score A (optional)"));
        form.add(scoreAField);

        form.add(new JLabel("Score B (optional)"));
        form.add(scoreBField);

        form.add(new JLabel("Status"));
        form.add(statusBox);

        JButton cancelBtn = new JButton("Cancel");
        JButton saveBtn = new JButton("Save");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(cancelBtn);
        actions.add(saveBtn);

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> onSave());
        getRootPane().setDefaultButton(saveBtn);

        if (editing != null) fill(editing);
    }

    public boolean isSaved() { return saved; }

    private void loadTeams() {
        try {
            int minPlayers = 5;
            try {
                String v = ems.config.AppConfig.get("tournament.min_players_per_team");
                if (v != null && !v.isBlank()) minPlayers = Integer.parseInt(v.trim());
            } catch (Exception ignored) { }

            teamABox.removeAllItems();
            teamBBox.removeAllItems();

            List<ems.model.Team> teams = teamDao.listEligibleForMatches(tournamentId, minPlayers);

            for (ems.model.Team t : teams) {
                TeamComboItem item = new TeamComboItem(t.getId(), t.getName());
                teamABox.addItem(item);
                teamBBox.addItem(item);
            }

            if (teams.isEmpty()) {
                Ui.error(this,
                        "No eligible teams.\n\nEach team needs at least " + minPlayers +
                                " players before you can schedule a match.");
            }

        } catch (Exception ex) {
            System.err.println("[EMS] Failed to load eligible teams: " + ex.getMessage());
        }
    }

    private void fill(Match m) {
        selectTeam(teamABox, m.getTeamAId());
        selectTeam(teamBBox, m.getTeamBId());

        scheduledAtField.setText(m.getScheduledAt() == null ? "" : m.getScheduledAt().toString());
        scoreAField.setText(m.getScoreA() == null ? "" : String.valueOf(m.getScoreA()));
        scoreBField.setText(m.getScoreB() == null ? "" : String.valueOf(m.getScoreB()));
        statusBox.setSelectedItem(m.getStatus());
    }

    private static void selectTeam(JComboBox<TeamComboItem> box, Long teamId) {
        if (teamId == null) return;
        for (int i = 0; i < box.getItemCount(); i++) {
            TeamComboItem it = box.getItemAt(i);
            if (it != null && it.getId() == teamId) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private void onSave() {
        try {
            // Block if COMPLETED
            ems.model.Tournament t = new ems.dao.TournamentDao()
                    .findById(tournamentId)
                    .orElseThrow(() -> new IllegalStateException("Tournament not found"));
            if (t.getStatus() == ems.model.TournamentStatus.COMPLETED) {
                Ui.error(this, "Tournament is COMPLETED. Match changes are not allowed.");
                return;
            }

            TeamComboItem a = (TeamComboItem) teamABox.getSelectedItem();
            TeamComboItem b = (TeamComboItem) teamBBox.getSelectedItem();

            if (a == null || b == null) throw new IllegalStateException("Create/select teams first");
            if (a.getId() == b.getId()) throw new IllegalStateException("Teams must be different");

            Timestamp scheduled = parseTsOrNull(text(scheduledAtField));
            Integer sa = parseIntOrNull(text(scoreAField));
            Integer sb = parseIntOrNull(text(scoreBField));

            MatchStatus st = (MatchStatus) statusBox.getSelectedItem();
            if (st == null) st = MatchStatus.SCHEDULED;

            // Stage for validation:
            String stage = (editing != null ? editing.getStage() : null);
            if (stage == null || stage.isBlank()) stage = "GROUP";
            stage = stage.trim().toUpperCase();

            // KO: no draws if both scores set
            boolean isKnockout = "QF".equals(stage) || "SF".equals(stage) || "F".equals(stage);
            if (isKnockout && sa != null && sb != null && sa.intValue() == sb.intValue()) {
                Ui.error(this, "Knockout matches (QF/SF/Final) cannot end in a draw. Please change the score.");
                return;
            }

            if (editing == null) {
                Match m = new Match();
                m.setTournamentId(tournamentId);
                m.setTeamAId(a.getId());
                m.setTeamBId(b.getId());
                m.setTeamA(a.getName());
                m.setTeamB(b.getName());
                m.setScheduledAt(scheduled);
                m.setScoreA(sa);
                m.setScoreB(sb);
                m.setStatus(st);
                m.setStage(stage);

                dao.insert(m);

                new ems.service.TournamentCompletionService().onMatchSaved(tournamentId, m);

            } else {
                editing.setTeamAId(a.getId());
                editing.setTeamBId(b.getId());
                editing.setTeamA(a.getName());
                editing.setTeamB(b.getName());
                editing.setScheduledAt(scheduled);
                editing.setScoreA(sa);
                editing.setScoreB(sb);
                editing.setStatus(st);

                if (editing.getStage() == null || editing.getStage().isBlank()) {
                    editing.setStage(stage);
                }

                dao.update(editing);

                new ems.service.TournamentCompletionService().onMatchSaved(tournamentId, editing);
            }

            saved = true;
            dispose();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static String text(JTextField f) {
        return f.getText() == null ? "" : f.getText().trim();
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        return Integer.parseInt(s.trim());
    }

    private static Timestamp parseTsOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        return Timestamp.valueOf(s.trim());
    }
}