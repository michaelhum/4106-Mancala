package ca.carleton.ai;

import ca.carleton.ai.ai.DecisionMaker;
import ca.carleton.ai.ai.Move;
import ca.carleton.ai.board.Board;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.util.Collections.shuffle;

/**
 * Main class.
 * <p>
 * Created by Mike on 3/5/2016.
 */
public class Game {

    private static final Logger LOG = LoggerFactory.getLogger(Game.class);

    public static void main(final String[] args) {

        final Scanner input = new Scanner(System.in);

        int numberStones = -1;
        int numberHouses = -1;

        try {
            System.out.print("Enter number of stones: ");
            numberStones = Integer.parseInt(input.nextLine());
            System.out.print("Enter number of houses: ");
            numberHouses = Integer.parseInt(input.nextLine());
        } catch (final Exception exception) {
            LOG.error("Error with args parse - try again!", exception);
            System.exit(-1);
        }

        if (numberStones < 1 || numberStones > 6) {
            LOG.warn("Invalid selection - defaulted to 4 stones. Minimum is 1, maximum is 6.");
            numberStones = 4;
        }

        if (numberHouses < (numberStones - 1) || numberHouses > (2 * (numberStones - 1))) {
            LOG.warn("Invalid selection - defaulted to {} houses. Min is 1, max is 2(stones - 1)",
                    (2 * (numberStones - 1)));
            numberHouses = 2 * (numberStones - 1);
            if (numberHouses == 0) {
                numberHouses = 1;
            }
        }

        System.out.print("Enter play option. Player vs Computer (1), Computer vs Computer (2): ");
        int playOption;
        try {
            playOption = Integer.parseInt(input.nextLine());
        } catch (final Exception exception) {
            LOG.warn("Error with parse - defaulting to player vs computer.");
            playOption = 1;
        }

        LOG.info("Creating new board with house size of {} and stone count {}.", numberHouses, numberStones);

        final Board board;
        if (playOption == 1) {
            board = Board.createNewPlayerVsComputerBoard(numberHouses, numberStones);
        } else if (playOption == 2) {
            board = Board.createNewComputerVsComputerBoard(numberHouses, numberStones);
        } else {
            board = Board.createNewPlayerVsComputerBoard(numberHouses, numberStones);
        }

        System.out.print("Enter maximum depth of mini-max algorithm for computer player(s): ");

        int maxDepth;
        try {
            maxDepth = Integer.parseInt(input.nextLine());
        } catch (final Exception exception) {
            LOG.warn("Error with parse - defaulting to depth of 100.");
            maxDepth = 100;
        }
        LOG.info("Set max depth to {}.", maxDepth);

        final List<Player> players = new ArrayList<>();
        players.add(board.getPlayerOne());
        players.add(board.getPlayerTwo());
        shuffle(players);
        board.setPlayers(players);

        DecisionMaker.initialize(maxDepth);

        playGame(board);
    }

    public static void playGame(final Board board) {

        LOG.info("Starting game. Initial board: \n{}", board);

        try {
            while (!board.isGameOver()) {
                final Player nextPlayer = board.getNextPlayer();
                final Move nextMove = DecisionMaker.determineMove(nextPlayer, board);
                board.applyMove(nextMove, true);
                LOG.info("\n{}", board);
            }
        } catch (final StackOverflowError error) {
            LOG.error("Stack overflow!", error);
            System.exit(-1);
        } catch (final Exception exception) {
            LOG.error("Error during game.", exception);
            System.exit(-1);
        }
        // TODO game over who wins and shit.
        LOG.info("Game over.");


    }

}
