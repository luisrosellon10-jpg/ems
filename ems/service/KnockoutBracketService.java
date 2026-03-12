package ems.service;

import ems.dao.MatchDao;
import ems.model.Match;
import ems.model.MatchStatus;
import ems.model.StandingRow;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KnockoutBracketService {

    private final MatchDao matchDao = new MatchDao();
    private final StandingsService standingsService = new StandingsService();

    // QF: TOP 8 => 4 matches
    public void generateTop8QuarterFinals(long tournamentId, LocalDateTime startDateTime) throws Exception {
        // Allow group matches to exist; only block if QF already exists
        if (matchDao.existsByTournamentAndStage(tournamentId, "QF")) {
            throw new IllegalStateException("Quarterfinals already exist.");
        }

        List<StandingRow> standings = standingsService.computeStandings(tournamentId);

        List<StandingRow> top = new ArrayList<>();
        for (StandingRow r : standings) {
            if (top.size() >= 8) break;
            top.add(r);
        }

        if (top.size() < 8) throw new IllegalStateException("Not enough teams to generate TOP 8 knockouts.");
        for (StandingRow r : top) if (r.getTeamId() == null) throw new IllegalStateException("A team in standings has no teamId.");

        insertMatch(tournamentId, top.get(0), top.get(7), startDateTime, "QF"); // 1v8
        insertMatch(tournamentId, top.get(1), top.get(6), startDateTime, "QF"); // 2v7
        insertMatch(tournamentId, top.get(2), top.get(5), startDateTime, "QF"); // 3v6
        insertMatch(tournamentId, top.get(3), top.get(4), startDateTime, "QF"); // 4v5
    }

    // SF: winners of QF1 vs QF2, and QF3 vs QF4
    public void generateSemifinals(long tournamentId, LocalDateTime startDateTime) throws Exception {
        if (matchDao.existsByTournamentAndStage(tournamentId, "SF")) {
            throw new IllegalStateException("Semifinals already exist.");
        }

        List<Match> qfs = matchDao.listByTournamentAndStage(tournamentId, "QF");
        if (qfs.size() != 4) throw new IllegalStateException("Expected 4 quarterfinals, found: " + qfs.size());

        for (Match m : qfs) ensureCompletedNonDraw(m);

        StandingRow w1 = winnerAsRow(qfs.get(0));
        StandingRow w2 = winnerAsRow(qfs.get(1));
        StandingRow w3 = winnerAsRow(qfs.get(2));
        StandingRow w4 = winnerAsRow(qfs.get(3));

        insertMatch(tournamentId, w1, w2, startDateTime, "SF");
        insertMatch(tournamentId, w3, w4, startDateTime, "SF");
    }

    // Final: winners of SF1 and SF2
    public void generateFinal(long tournamentId, LocalDateTime startDateTime) throws Exception {
        if (matchDao.existsByTournamentAndStage(tournamentId, "F")) {
            throw new IllegalStateException("Final already exists.");
        }

        List<Match> sfs = matchDao.listByTournamentAndStage(tournamentId, "SF");
        if (sfs.size() != 2) throw new IllegalStateException("Expected 2 semifinals, found: " + sfs.size());

        for (Match m : sfs) ensureCompletedNonDraw(m);

        StandingRow w1 = winnerAsRow(sfs.get(0));
        StandingRow w2 = winnerAsRow(sfs.get(1));

        insertMatch(tournamentId, w1, w2, startDateTime, "F");
    }

    private void ensureCompletedNonDraw(Match m) {
        if (m.getStatus() != MatchStatus.COMPLETED) {
            throw new IllegalStateException("Match " + m.getId() + " is not COMPLETED.");
        }
        if (m.getScoreA() == null || m.getScoreB() == null) {
            throw new IllegalStateException("Match " + m.getId() + " has no score.");
        }
        if (m.getScoreA().intValue() == m.getScoreB().intValue()) {
            throw new IllegalStateException("Match " + m.getId() + " is a draw. Knockout matches cannot end in a draw.");
        }
        if (m.getTeamAId() == null || m.getTeamBId() == null) {
            throw new IllegalStateException("Match " + m.getId() + " is missing team IDs.");
        }
    }

    private StandingRow winnerAsRow(Match m) {
        boolean aWins = m.getScoreA() > m.getScoreB();
        Long teamId = aWins ? m.getTeamAId() : m.getTeamBId();
        String name = aWins ? m.getTeamA() : m.getTeamB();

        if (teamId == null) throw new IllegalStateException("Winner teamId is null for match " + m.getId());
        return new StandingRow(teamId, name, 0, 0, 0, 0, 0, 0, 0);
    }

    private void insertMatch(long tournamentId, StandingRow a, StandingRow b, LocalDateTime dt, String stage) throws Exception {
        Match m = new Match();
        m.setTournamentId(tournamentId);

        m.setTeamAId(a.getTeamId());
        m.setTeamBId(b.getTeamId());

        // legacy text
        m.setTeamA(a.getTeam());
        m.setTeamB(b.getTeam());

        m.setScheduledAt(dt == null ? null : Timestamp.valueOf(dt));
        m.setScoreA(null);
        m.setScoreB(null);
        m.setStatus(MatchStatus.SCHEDULED);
        m.setStage(stage);

        matchDao.insert(m);
    }
}