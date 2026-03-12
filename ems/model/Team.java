package ems.model;

import java.sql.Timestamp;

public class Team {
    private long id;
    private long tournamentId;
    private String name;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTournamentId() { return tournamentId; }
    public void setTournamentId(long tournamentId) { this.tournamentId = tournamentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}