package ca.carleton.ai.ai.strategy;

import ca.carleton.ai.Player;
import ca.carleton.ai.ai.Move;
import ca.carleton.ai.board.Board;

/**
 * Strategy for making moves.
 * <p>
 * Created by Mike on 3/7/2016.
 */
public interface MoveStrategy {

    boolean appliesTo(final Player player, final boolean enablePruning);

    Move getMove(final Player player, final Board board);
}
