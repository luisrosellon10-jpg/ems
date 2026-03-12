package ems.service;

import ems.dao.MatchDao;
import ems.model.Match;
import ems.model.MatchStatus;

import java.sql.SQLException;
import java.util.List;

public class TournamentWinnerService {

    private final MatchDao matchDao = new MatchDao();

    public Winner getWinner(long tournamentId) throws SQLException {
        List<Match> finals = matchDao.listByTournamentAndStage(tournamentId, "F");
        if (finals.isEmpty()) throw new IllegalStateException("No Final match found (stage=F). Generate Final first.");
        if (finals.size() > 1) throw new IllegalStateException("Multiple Final matches found. Please keep only one.");

        Match f = finals.get(0);

        if (f.getStatus() != MatchStatus.COMPLETED) {
            throw new IllegalStateException("Final is not completed yet.");
        }
        if (f.getScoreA() == null || f.getScoreB() == null) {
            throw new IllegalStateException("Final has no score yet.");
        }
        if (f.getScoreA().intValue() == f.getScoreB().intValue()) {
            throw new IllegalStateException("Final is a draw. Knockout matches cannot end in a draw.");
        }

        boolean aWins = f.getScoreA() > f.getScoreB();
        Long winnerId = aWins ? f.getTeamAId() : f.getTeamBId();
        String winnerName = aWins ? f.getTeamA() : f.getTeamB();

        if (winnerId == null) {
            throw new IllegalStateException("Winner teamId is null in Final. Ensure team IDs exist.");
        }

        return new Winner(winnerId, winnerName);
    }

    public static final class Winner {
        public final Long teamId;
        public final String teamName;

        public Winner(Long teamId, String teamName) {
            this.teamId = teamId;
            this.teamName = teamName;
        }
    }
}