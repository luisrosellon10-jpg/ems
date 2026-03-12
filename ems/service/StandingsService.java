package ems.service;

import ems.dao.MatchDao;
import ems.dao.TeamDao;
import ems.model.Match;
import ems.model.MatchStatus;
import ems.model.StandingRow;
import ems.model.Team;

import java.sql.SQLException;
import java.util.*;

public class StandingsService {

    private final MatchDao matchDao = new MatchDao();
    private final TeamDao teamDao = new TeamDao();

    public List<StandingRow> computeStandings(long tournamentId) throws SQLException {
        List<Team> teams = teamDao.listByTournament(tournamentId);
        List<Match> matches = matchDao.listByTournament(tournamentId);

        Map<Long, String> teamNameById = new HashMap<>();
        for (Team t : teams) teamNameById.put(t.getId(), nv(t.getName()));

        Map<String, Stats> map = new HashMap<>();

        // 1) Preload ALL teams with zeros
        for (Team t : teams) {
            String name = nv(t.getName());
            map.put("ID:" + t.getId(), new Stats(t.getId(), name));
        }

        // 2) Apply completed match results
        for (Match m : matches) {
            String stage = m.getStage();
            if (stage != null && !stage.equalsIgnoreCase("GROUP")) continue;
            if (m.getStatus() != MatchStatus.COMPLETED) continue;
            if (m.getScoreA() == null || m.getScoreB() == null) continue;

            TeamKey a = resolveTeamA(m, teamNameById);
            TeamKey b = resolveTeamB(m, teamNameById);
            if (a == null || b == null) continue;

            Stats sa = map.computeIfAbsent(a.key, k -> new Stats(a.teamId, a.displayName));
            Stats sb = map.computeIfAbsent(b.key, k -> new Stats(b.teamId, b.displayName));

            sa.played++;
            sb.played++;

            sa.forScore += m.getScoreA();
            sa.againstScore += m.getScoreB();

            sb.forScore += m.getScoreB();
            sb.againstScore += m.getScoreA();

            if (m.getScoreA() > m.getScoreB()) {
                sa.wins++; sb.losses++;
            } else if (m.getScoreA() < m.getScoreB()) {
                sb.wins++; sa.losses++;
            } else {
                sa.draws++; sb.draws++;
            }
        }

        List<StandingRow> rows = new ArrayList<>();
        for (Stats s : map.values()) rows.add(s.toRow());

        rows.sort(Comparator
                .comparingInt(StandingRow::getPoints).reversed()
                .thenComparingInt(StandingRow::getGoalDiff).reversed()
                .thenComparingInt(StandingRow::getGoalsFor).reversed()
                .thenComparing(StandingRow::getTeam, String.CASE_INSENSITIVE_ORDER));

        return rows;
    }

    private static TeamKey resolveTeamA(Match m, Map<Long, String> teamNameById) {
        if (m.getTeamAId() != null) {
            String name = teamNameById.get(m.getTeamAId());
            if (name == null || name.isBlank()) name = nv(m.getTeamA());
            if (name.isBlank()) return null;
            return new TeamKey("ID:" + m.getTeamAId(), m.getTeamAId(), name);
        }
        String name = nv(m.getTeamA());
        if (name.isBlank()) return null;
        return new TeamKey("NAME:" + name.toLowerCase(), null, name);
    }

    private static TeamKey resolveTeamB(Match m, Map<Long, String> teamNameById) {
        if (m.getTeamBId() != null) {
            String name = teamNameById.get(m.getTeamBId());
            if (name == null || name.isBlank()) name = nv(m.getTeamB());
            if (name.isBlank()) return null;
            return new TeamKey("ID:" + m.getTeamBId(), m.getTeamBId(), name);
        }
        String name = nv(m.getTeamB());
        if (name.isBlank()) return null;
        return new TeamKey("NAME:" + name.toLowerCase(), null, name);
    }

    private static String nv(String s) { return s == null ? "" : s.trim(); }

    private static final class TeamKey {
        final String key;
        final Long teamId;     // nullable for legacy/name-only
        final String displayName;
        TeamKey(String key, Long teamId, String displayName) {
            this.key = key;
            this.teamId = teamId;
            this.displayName = displayName;
        }
    }

    private static final class Stats {
        final Long teamId;
        final String team;

        int played = 0, wins = 0, draws = 0, losses = 0;
        int forScore = 0, againstScore = 0;

        Stats(Long teamId, String team) { this.teamId = teamId; this.team = team; }

        StandingRow toRow() {
            int points = wins * 3 + draws;
            return new StandingRow(teamId, team, played, wins, draws, losses, forScore, againstScore, points);
        }
    }
}