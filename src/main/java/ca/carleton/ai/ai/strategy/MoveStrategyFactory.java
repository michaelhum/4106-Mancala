package ca.carleton.ai.ai.strategy;

import ca.carleton.ai.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for strategies.
 * <p>
 * Created by Mike on 3/7/2016.
 */
public class MoveStrategyFactory {

    private final List<MoveStrategy> strategies = new ArrayList<>();

    public void initialize(final int maxDepth) {
        this.strategies.add(new PlayerStrategy());
        this.strategies.add(new MinimaxStrategy(maxDepth));
    }

    public MoveStrategy getMoveStrategy(final Player player) {
        for (final MoveStrategy strategy : this.strategies)
            if (strategy.appliesTo(player, true)) {
                return strategy;
            }
        throw new IllegalStateException("No strategy found for decision maker.");
    }
}
