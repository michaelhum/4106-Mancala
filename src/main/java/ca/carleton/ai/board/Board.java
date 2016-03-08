package ca.carleton.ai.board;

import ca.carleton.ai.Player;
import ca.carleton.ai.ai.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the mancala board.
 * <p>
 * Created by Mike on 3/6/2016.
 */
public class Board {

    private static final Logger LOG = LoggerFactory.getLogger(Board.class);

    // Top player
    private House playerOneKalah;

    private House playerTwoKalah;

    // Bottom player
    private House[] playerOneHouses;

    private House[] playerTwoHouses;

    private final int numberOfHouses;

    private final List<Player> players;

    private boolean enablePruning;

    private Board(final int numberHouses) {
        this.numberOfHouses = numberHouses;
        this.players = new ArrayList<>();
    }

    public Board(final Board old) {
        this.numberOfHouses = old.numberOfHouses;
        this.players = new ArrayList<>(old.players);

        this.playerOneKalah = new House(old.getPlayerOneKalah());
        this.playerTwoKalah = new House(old.getPlayerTwoKalah());

        this.playerOneHouses = new House[this.numberOfHouses];
        this.playerTwoHouses = new House[this.numberOfHouses];

        for (int i = 0; i < this.numberOfHouses; i++) {
            this.playerOneHouses[i] = new House(old.playerOneHouses[i]);
            this.playerTwoHouses[i] = new House(old.playerTwoHouses[i]);
        }

    }

    // Apply a move and modify players to set next player.
    public void applyMove(final Move move, final boolean log) {

        if (move == null) {
            if (log) {
                LOG.warn("No move given - skipping.");
            }
            return;
        }
        if (move.getHouseIndex() < 0 || move.getHouseIndex() > this.numberOfHouses - 1) {
            if (log) {
                LOG.warn("Invalid move index - skipping.");
            }
            this.players.add(move.getPlayer());
            return;
        }

        final House[] houses = this.getHouses(move.getPlayer() == Player.COMPUTER_ONE);
        int startIndex = move.getHouseIndex() + 1;


        final int numberOfSeeds = houses[move.getHouseIndex()].takeAllSeeds();

        for (int i = 0; i < numberOfSeeds; i++) {
            final House current = houses[startIndex];
            startIndex++;
            current.addSeed();

            // Wrap around.
            if (startIndex == houses.length) {
                startIndex = 0;
            }

            // Last seed.
            if (i == numberOfSeeds - 1) {
                // If we drop last seed into player kalah we go again.
                if (move.getPlayer() == Player.COMPUTER_ONE && current.isEdgeHouse()) {
                    this.players.add(0, this.getPlayerOne());
                    if (log) {
                        LOG.info("Last seed dropped into kalah - another turn given.");
                    }
                } else if (move.getPlayer() != Player.COMPUTER_ONE && current.isEdgeHouse()) {
                    this.players.add(0, this.getPlayerTwo());
                    if (log) {
                        LOG.info("Last seed dropped into kalah - another turn given.");
                    }
                } else {
                    // Add them to the end to go after.
                    this.players.add(move.getPlayer());
                }
                // If we drop last seed into an empty house (well, there is 1 now), we get all the seeds from the other house.
                if (current.getSeeds() == 1 && !current.isEdgeHouse()) {
                    try {
                        final int oppositeIndex = startIndex - 1;

                        final House[] copy;
                        if (move.getPlayer() == Player.COMPUTER_ONE) {
                            copy = this.playerTwoHouses.clone();
                        } else {
                            copy = this.playerOneHouses.clone();
                        }

                        final House opposite = copy[oppositeIndex];
                        if (move.getPlayer() == Player.COMPUTER_ONE) {
                            this.playerOneKalah.takeSeedsFrom(opposite);
                        } else {
                            this.playerTwoKalah.takeSeedsFrom(opposite);
                        }
                        if (log) {
                            LOG.info(
                                    "Last seed dropped into empty house - seeds taken from opposite house. (Landed on house {}. Taken from opponent house {})",
                                    startIndex,
                                    oppositeIndex);
                        }
                    } catch (final Exception exception) {
                        if (log) {
                            LOG.trace("Error with 'dropping last seed into empty house' scenario!", exception);
                        }
                    }
                }
            }
        }
        // Special case - add them anyway even if their move did nothing.
        if (numberOfSeeds == 0 && log) {
            this.players.add(move.getPlayer());
        }
    }

    public House[] getHouses(final boolean playerOne) {
        final House[] houses = new House[(this.numberOfHouses * 2) + 1];
        int i = 0;
        if (playerOne) {
            for (final House house : this.playerOneHouses) {
                houses[i++] = house;
            }
            houses[i++] = this.playerOneKalah;
            for (final House house : this.playerTwoHouses) {
                houses[i++] = house;
            }
        } else {
            for (final House house : this.playerTwoHouses) {
                houses[i++] = house;
            }
            houses[i++] = this.playerTwoKalah;
            for (final House house : this.playerOneHouses) {
                houses[i++] = house;
            }
        }
        return houses;
    }

    public static Board createNewPlayerVsComputerBoard(final int numberOfHouses, final int numberOfSeeds) {
        final Board board = new Board(numberOfHouses);

        board.playerOneKalah = new House(Player.COMPUTER_ONE);
        board.playerOneHouses = new House[numberOfHouses];
        for (int i = 0; i < numberOfHouses; i++) {
            board.playerOneHouses[i] = new House(Player.COMPUTER_ONE, numberOfSeeds);
        }

        board.playerTwoKalah = new House(Player.HUMAN_PLAYER);
        board.playerTwoHouses = new House[numberOfHouses];
        for (int i = 0; i < numberOfHouses; i++) {
            board.playerTwoHouses[i] = new House(Player.HUMAN_PLAYER, numberOfSeeds);
        }

        return board;
    }

    public static Board createNewComputerVsComputerBoard(final int numberOfHouses, final int numberOfSeeds) {
        final Board board = new Board(numberOfHouses);

        board.playerOneKalah = new House(Player.COMPUTER_ONE);
        board.playerOneHouses = new House[numberOfHouses];
        for (int i = 0; i < numberOfHouses; i++) {
            board.playerOneHouses[i] = new House(Player.COMPUTER_ONE, numberOfSeeds);
        }

        board.playerTwoKalah = new House(Player.COMPUTER_TWO);
        board.playerTwoHouses = new House[numberOfHouses];
        for (int i = 0; i < numberOfHouses; i++) {
            board.playerTwoHouses[i] = new House(Player.COMPUTER_TWO, numberOfSeeds);
        }

        return board;
    }

    public boolean isGameOver() {
        // Return true if we check every house and there is no seed...
        boolean result = true;
        for (int i = 0; i < this.numberOfHouses; i++) {
            if (this.playerOneHouses[i].getSeeds() > 0) {
                result = false;
                break;
            }
        }
        if (result) {
            return true;
        }

        for (int i = 0; i < this.numberOfHouses; i++) {
            if (this.playerTwoHouses[i].getSeeds() > 0) {
                return false;
            }
        }
        return true;
    }

    public Player getPlayerOne() {
        return this.playerOneKalah.getPlayer();
    }

    public Player getPlayerTwo() {
        return this.playerTwoKalah.getPlayer();
    }

    public int getNumberOfHouses() {
        return this.numberOfHouses;
    }

    public House getPlayerOneKalah() {
        return this.playerOneKalah;
    }

    public House getPlayerTwoKalah() {
        return this.playerTwoKalah;
    }

    public House[] getPlayerOneHouses() {
        return this.playerOneHouses;
    }

    public House[] getPlayerTwoHouses() {
        return this.playerTwoHouses;
    }

    public void setPlayers(final List<Player> players) {
        this.players.addAll(players);
        if (this.players.size() != 2) {
            throw new IllegalArgumentException("Max 2 players.");
        }
    }

    public Player getNextPlayer() {
        final Player next = this.players.remove(0);
        LOG.debug("Next player will be {}.", next);
        return next;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        final List<House[]> houses = Arrays.asList(this.playerOneHouses, this.playerTwoHouses);
        for (final House[] house : houses) {
            builder.append(String.format("\n%s: ", house[0].getPlayer()));
            for (final House aHouse : house) {
                builder.append(aHouse);
            }
            builder.append(house[0].getPlayer() == Player.COMPUTER_ONE ? this.playerOneKalah : this.playerTwoKalah);
        }
        builder.append("\n");
        return builder.toString();
    }

    public int getPlayerOneHouseValue() {
        int value = 0;
        for (final House house : this.getPlayerOneHouses()) {
            value += house.getSeeds();
        }
        return value;
    }

    public int getPlayerTwoHouseValue() {
        int value = 0;
        for (final House house : this.getPlayerTwoHouses()) {
            value += house.getSeeds();
        }
        return value;
    }
}
