package ca.carleton.ai.ai.strategy;

import ca.carleton.ai.Player;
import ca.carleton.ai.ai.Move;
import ca.carleton.ai.board.Board;

import java.util.Scanner;

/**
 * Strategy for humans making a move...
 * <p>
 * Created by Mike on 3/7/2016.
 */
public class PlayerStrategy implements MoveStrategy {

    private static final Scanner input = new Scanner(System.in);

    @Override
    public boolean appliesTo(final Player player, final boolean enablePruning) {
        return player == Player.HUMAN_PLAYER;
    }

    @Override
    public Move getMove(final Player player, final Board board) {
        System.out.print("Enter move as follows: houseIndex (starting at 0): ");
        while (true) {
            final String move = input.nextLine();
            try {
                final int index = Integer.parseInt(move);
                return new Move(player, index);
            } catch (final Exception exception) {
                System.out.print("Please try again: ");
            }
        }
    }
}
