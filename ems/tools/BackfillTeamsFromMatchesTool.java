package ems.tools;

import ems.dao.MatchDao;
import ems.dao.TeamDao;
import ems.dao.TournamentDao;
import ems.model.Match;
import ems.model.Team;
import ems.model.Tournament;

import java.util.*;

public class BackfillTeamsFromMatchesTool {

    private final TournamentDao tournamentDao = new TournamentDao();
    private final MatchDao matchDao = new MatchDao();
    private final TeamDao teamDao = new TeamDao();

    public void run() throws Exception {
        List<Tournament> tournaments = tournamentDao.listAll();

        for (Tournament t : tournaments) {
            long tid = t.getId();
            List<Match> matches = matchDao.listByTournament(tid);

            // Create teams from existing match text
            Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            for (Match m : matches) {
                if (m.getTeamA() != null && !m.getTeamA().trim().isEmpty()) names.add(m.getTeamA().trim());
                if (m.getTeamB() != null && !m.getTeamB().trim().isEmpty()) names.add(m.getTeamB().trim());
            }

            Map<String, Long> teamIdByName = new HashMap<>();
            for (String name : names) {
                Optional<Team> existing = teamDao.findByTournamentAndName(tid, name);
                long teamId = existing.isPresent() ? existing.get().getId() : teamDao.insert(tid, name);
                teamIdByName.put(name.toLowerCase(), teamId);
            }

            // Update matches with IDs
            for (Match m : matches) {
                Long aId = m.getTeamA() == null ? null : teamIdByName.get(m.getTeamA().trim().toLowerCase());
                Long bId = m.getTeamB() == null ? null : teamIdByName.get(m.getTeamB().trim().toLowerCase());
                matchDao.updateTeamIds(m.getId(), aId, bId);
            }

            System.out.println("[EMS] Backfilled teams for tournament " + t.getName() + " (" + tid + ")");
        }

        System.out.println("[EMS] Backfill complete.");
    }
}