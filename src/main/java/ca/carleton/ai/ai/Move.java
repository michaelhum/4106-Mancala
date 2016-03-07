package ca.carleton.ai.ai;

import ca.carleton.ai.Player;

/**
 * Represents a move to make.
 * <p>
 * Created by Mike on 3/6/2016.
 */
public class Move {

    private final Player player;

    private final int houseIndex;

    public Move(final Player player, final int houseIndex) {
        this.player = player;
        this.houseIndex = houseIndex;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getHouseIndex() {
        return houseIndex;
    }
}
