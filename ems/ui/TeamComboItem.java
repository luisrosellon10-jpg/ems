package ems.ui;

public class TeamComboItem {
    private final long id;
    private final String name;

    public TeamComboItem(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() { return id; }
    public String getName() { return name; }

    @Override public String toString() { return name; }
}
