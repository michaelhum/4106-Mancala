package ca.carleton.ai.ai;

import ca.carleton.ai.Player;
import ca.carleton.ai.ai.strategy.MoveStrategy;
import ca.carleton.ai.ai.strategy.MoveStrategyFactory;
import ca.carleton.ai.board.Board;

/**
 * Decision maker.
 * <p>
 * Created by Mike on 3/6/2016.
 */
public class DecisionMaker {

    private static final MoveStrategyFactory strategyFactory = new MoveStrategyFactory();

    public static void initialize(final int maxDepth) {
        strategyFactory.initialize(maxDepth);
    }

    public static Move determineMove(final Player player, final Board board) {
        final MoveStrategy strategy = strategyFactory.getMoveStrategy(player);
        return strategy.getMove(player, board);
    }

}
