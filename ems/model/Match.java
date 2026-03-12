package ems.model;

import java.sql.Timestamp;

public class Match {
    private long id;
    private long tournamentId;

    private Long teamAId;
    private Long teamBId;

    private String teamA;
    private String teamB;

    private Timestamp scheduledAt;

    private Integer scoreA;
    private Integer scoreB;

    private MatchStatus status;

    // NEW
    private String stage; // GROUP, QF, SF, F

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTournamentId() { return tournamentId; }
    public void setTournamentId(long tournamentId) { this.tournamentId = tournamentId; }

    public Long getTeamAId() { return teamAId; }
    public void setTeamAId(Long teamAId) { this.teamAId = teamAId; }

    public Long getTeamBId() { return teamBId; }
    public void setTeamBId(Long teamBId) { this.teamBId = teamBId; }

    public String getTeamA() { return teamA; }
    public void setTeamA(String teamA) { this.teamA = teamA; }

    public String getTeamB() { return teamB; }
    public void setTeamB(String teamB) { this.teamB = teamB; }

    public Timestamp getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Timestamp scheduledAt) { this.scheduledAt = scheduledAt; }

    public Integer getScoreA() { return scoreA; }
    public void setScoreA(Integer scoreA) { this.scoreA = scoreA; }

    public Integer getScoreB() { return scoreB; }
    public void setScoreB(Integer scoreB) { this.scoreB = scoreB; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
}