package ems.service;

import ems.dao.MatchDao;
import ems.dao.TeamDao;
import ems.model.Match;
import ems.model.MatchStatus;
import ems.model.Team;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoundRobinSchedulerService {

    private final TeamDao teamDao = new TeamDao();
    private final MatchDao matchDao = new MatchDao();

    /**
     * Generates a DOUBLE round-robin schedule.
     *
     * @param tournamentId tournament id
     * @param startDateTime first round date/time
     * @param daysBetweenRounds days between rounds (e.g., 1 = daily)
     */
    public void generateDoubleRoundRobin(long tournamentId, java.time.LocalDateTime startDateTime, int daysBetweenRounds) throws Exception{
        if (daysBetweenRounds < 0) throw new IllegalArgumentException("daysBetweenRounds must be >= 0");

        List<Team> teams = teamDao.listByTournament(tournamentId);
        if (teams.size() < 2) throw new IllegalStateException("Need at least 2 teams to generate schedule");

        // Build working list of team IDs (circle method uses positions)
        List<Team> work = new ArrayList<>(teams);

        // If odd, add a BYE placeholder team with id=0
        boolean hasBye = (work.size() % 2 == 1);
        if (hasBye) {
            Team bye = new Team();
            bye.setId(0);
            bye.setName("BYE");
            work.add(bye);
        }

        int n = work.size();
        int roundsSingle = n - 1;            // number of rounds in single RR
        int matchesPerRound = n / 2;

        // Use a fixed ordering but you can shuffle if you want randomness:
        // Collections.shuffle(work);

        // "Circle method" list: keep first fixed, rotate the rest
        Team fixed = work.get(0);
        List<Team> rot = new ArrayList<>(work.subList(1, n));

        LocalDateTime roundTime = startDateTime;

        // First leg (single round robin)
        for (int round = 0; round < roundsSingle; round++) {
            List<Pair> pairs = buildPairs(fixed, rot, matchesPerRound);

            for (Pair p : pairs) {
                // skip BYE matches
                if (p.a.getId() == 0 || p.b.getId() == 0) continue;

                insertMatch(tournamentId, p.a, p.b, roundTime);
            }

            rot = rotate(rot);
            roundTime = roundTime.plusDays(daysBetweenRounds);
        }

        // Second leg (reverse fixtures)
        for (int round = 0; round < roundsSingle; round++) {
            List<Pair> pairs = buildPairs(fixed, rot, matchesPerRound);

            for (Pair p : pairs) {
                if (p.a.getId() == 0 || p.b.getId() == 0) continue;

                // reverse home/away
                insertMatch(tournamentId, p.b, p.a, roundTime);
            }

            rot = rotate(rot);
            roundTime = roundTime.plusDays(daysBetweenRounds);
        }
    }

    private void insertMatch(long tournamentId, Team a, Team b, LocalDateTime dt) throws Exception {
        Match m = new Match();
        m.setTournamentId(tournamentId);

        // NEW preferred IDs
        m.setTeamAId(a.getId());
        m.setTeamBId(b.getId());

        // legacy text (keep for display/backward compatibility)
        m.setTeamA(a.getName());
        m.setTeamB(b.getName());

        m.setScheduledAt(dt == null ? null : Timestamp.valueOf(dt));
        m.setScoreA(null);
        m.setScoreB(null);
        m.setStatus(MatchStatus.SCHEDULED);

        matchDao.insert(m);
        m.setStage("GROUP");
        m.setStage("QF");
    }

    private static List<Pair> buildPairs(Team fixed, List<Team> rot, int matchesPerRound) {
        List<Pair> pairs = new ArrayList<>(matchesPerRound);

        // positions: [fixed] + rot...
        // pair fixed with last of rot, then mirror inwards
        Team firstOpp = rot.get(rot.size() - 1);
        pairs.add(new Pair(fixed, firstOpp));

        int left = 0;
        int right = rot.size() - 2;
        while (pairs.size() < matchesPerRound) {
            Team a = rot.get(left++);
            Team b = rot.get(right--);
            pairs.add(new Pair(a, b));
        }

        return pairs;
    }

    private static List<Team> rotate(List<Team> rot) {
        // rotation: take last element and insert at index 0
        List<Team> out = new ArrayList<>(rot.size());
        out.add(rot.get(rot.size() - 1));
        out.addAll(rot.subList(0, rot.size() - 1));
        return out;
    }

    private static final class Pair {
        final Team a;
        final Team b;
        Pair(Team a, Team b) { this.a = a; this.b = b; }
    }
}