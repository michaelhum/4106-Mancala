package ca.carleton.ai.ai.strategy;

import ca.carleton.ai.Player;
import ca.carleton.ai.ai.Move;
import ca.carleton.ai.ai.strategy.heuristic.Heuristic;
import ca.carleton.ai.board.Board;
import ca.carleton.ai.board.House;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for minimax with AB pruning.
 * <p>
 * Created by Mike on 3/7/2016.
 */
public class MinimaxStrategy implements MoveStrategy {

    private final int maxDepth;

    private static int nodeCount;

    public MinimaxStrategy(final int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public boolean appliesTo(final Player player, final boolean enablePruning) {
        return player != Player.HUMAN_PLAYER;
    }

    @Override
    public Move getMove(final Player player, final Board board) {

        final List<MinimaxThread> threads = new ArrayList<>();
        nodeCount = 0;

        final House[] houses;
        if (player == Player.COMPUTER_ONE) {
            houses = board.getPlayerOneHouses();
        } else {
            houses = board.getPlayerTwoHouses();
        }

        final Heuristic heuristic;
        // TODO heuristic
        if (player == Player.COMPUTER_ONE) {
            heuristic = Heuristic.MAXIMUM_POINTS;
        } else {
            heuristic = Heuristic.MAXIMUM_TURNS;
        }

        for (int i = 0; i < board.getNumberOfHouses(); i++) {
            if (!houses[i].isEmpty()) {
                final MinimaxThread thread = new MinimaxThread(board, i, heuristic);
                threads.add(thread);
                thread.start();
            }
        }

        int bestValue = Integer.MAX_VALUE;
        int bestIndex = 0;

        for (final MinimaxThread thread : threads) {
            try {
                thread.join();
                if (thread.getMoveValue() < bestValue) {
                    bestValue = thread.getMoveValue();
                    bestIndex = thread.getFirstHouse();
                }
            } catch (final Exception ignored) {
            }
        }

        return new Move(player, bestIndex);
    }

    private class MinimaxThread extends Thread {
        private final Board board;

        private final int firstHouse;

        private int moveValue;

        private final Heuristic heuristic;

        public MinimaxThread(final Board board, final int firstIndex, final Heuristic heuristic) {
            this.board = board;
            this.firstHouse = firstIndex;
            this.heuristic = heuristic;
        }

        public int getFirstHouse() {
            return this.firstHouse;
        }

        public int getMoveValue() {
            return this.moveValue;
        }

        public void run() {
            this.moveValue = new MoveFinder(this.board).findMove(this.firstHouse, this.heuristic);
        }
    }

    private class MoveFinder {

        private final Board board;

        public MoveFinder(final Board board) {
            this.board = board;
        }

        public int findMove(final int startIndex, final Heuristic heuristic) {

            int depth = 0;


            if (depth >= maxDepth) {
                // TODO.
            }

            return 0;
        }
    }
}
