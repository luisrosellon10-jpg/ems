package ems.service;

import ems.dao.MatchDao;
import ems.dao.TournamentDao;
import ems.model.Match;
import ems.model.MatchStatus;
import ems.model.TournamentStatus;

import java.util.List;

public class TournamentCompletionService {

    private final MatchDao matchDao = new MatchDao();
    private final TournamentDao tournamentDao = new TournamentDao();

    public void onMatchSaved(long tournamentId, Match match) throws Exception {
        if (match == null) return;

        String stage = match.getStage();
        if (stage == null) return;
        stage = stage.trim().toUpperCase();

        // only final triggers completion
        if (!"F".equals(stage)) return;

        if (match.getStatus() != MatchStatus.COMPLETED) return;

        Integer a = match.getScoreA();
        Integer b = match.getScoreB();
        if (a == null || b == null) return;
        if (a.intValue() == b.intValue()) return;

        List<Match> finals = matchDao.listByTournamentAndStage(tournamentId, "F");
        if (finals.isEmpty()) return;

        tournamentDao.updateStatus(tournamentId, TournamentStatus.COMPLETED);
    }
}