package ems.model;

public class StandingRow {
    private final Long teamId; // nullable for legacy name-only
    private final String team;
    private final int played;
    private final int wins;
    private final int draws;
    private final int losses;
    private final int goalsFor;
    private final int goalsAgainst;
    private final int points;

    public StandingRow(Long teamId, String team, int played, int wins, int draws, int losses,
                       int goalsFor, int goalsAgainst, int points) {
        this.teamId = teamId;
        this.team = team;
        this.played = played;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.points = points;
    }

    // Backward-compatible constructor (if any old code still uses it)
    public StandingRow(String team, int played, int wins, int draws, int losses,
                       int goalsFor, int goalsAgainst, int points) {
        this(null, team, played, wins, draws, losses, goalsFor, goalsAgainst, points);
    }

    public Long getTeamId() { return teamId; }
    public String getTeam() { return team; }
    public int getPlayed() { return played; }
    public int getWins() { return wins; }
    public int getDraws() { return draws; }
    public int getLosses() { return losses; }
    public int getGoalsFor() { return goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }

    public int getGoalDiff() { return goalsFor - goalsAgainst; }
    public int getPoints() { return points; }
}