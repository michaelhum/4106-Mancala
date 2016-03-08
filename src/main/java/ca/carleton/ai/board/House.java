package ca.carleton.ai.board;

import ca.carleton.ai.Player;

/**
 * Represents a house on the board.
 * <p>
 * Created by Mike on 3/6/2016.
 */
public class House {

    private Player player;

    private int seeds;

    private final boolean isEdgeHouse;

    public House(final Player player, final int seeds) {
        this.player = player;
        this.seeds = seeds;
        this.isEdgeHouse = false;
    }

    public House(final Player player) {
        this.player = player;
        this.seeds = 0;
        this.isEdgeHouse = true;
    }

    public House(final House old) {
        this.player = old.player;
        this.seeds = old.seeds;
        this.isEdgeHouse = old.isEdgeHouse();
    }

    public boolean isEmpty() {
        return this.seeds == 0;
    }

    public void takeSeedsFrom(final House other) {
        this.seeds += other.seeds;
        other.seeds = 0;
    }

    public int takeAllSeeds() {
        final int seeds = this.seeds;
        this.seeds = 0;
        return seeds;
    }

    public void addSeed() {
        this.seeds++;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    public int getSeeds() {
        return seeds;
    }

    public void setSeeds(final int seeds) {
        this.seeds = seeds;
    }

    public boolean isEdgeHouse() {
        return isEdgeHouse;
    }

    @Override
    public String toString() {
        return this.isEdgeHouse ? String.format("(%d)", this.seeds) : String.format("[%d]", this.seeds);
    }

}
