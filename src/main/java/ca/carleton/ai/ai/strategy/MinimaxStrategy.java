package ca.carleton.ai.ai.strategy;

import ca.carleton.ai.Player;
import ca.carleton.ai.ai.Move;
import ca.carleton.ai.ai.strategy.heuristic.Heuristic;
import ca.carleton.ai.board.Board;
import ca.carleton.ai.board.House;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for minimax with AB pruning.
 * <p>
 * Created by Mike on 3/7/2016.
 */
public class MinimaxStrategy implements MoveStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(MinimaxStrategy.class);

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
        if (player == Player.COMPUTER_ONE) {
            heuristic = Heuristic.MINIMUM_DIFFERENCE;
        } else {
            heuristic = Heuristic.MAXIMUM_SEEDS;
        }

        for (int i = 0; i < board.getNumberOfHouses(); i++) {
            if (!houses[i].isEmpty()) {
                final MinimaxThread thread = new MinimaxThread(player, new Board(board), i, heuristic);
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

        LOG.info("Selected move is house index {}. Results for search: Node count {}", bestIndex, nodeCount);

        return new Move(player, bestIndex);
    }

    private class MinimaxThread extends Thread {
        private final Board board;

        private final int firstHouse;

        private int moveValue;

        private final Heuristic heuristic;

        private final Player player;

        private int currentLevel;

        public MinimaxThread(final Player player, final Board board, final int firstIndex, final Heuristic heuristic) {
            this.board = board;
            this.firstHouse = firstIndex;
            this.heuristic = heuristic;
            this.player = player;
        }

        public int getFirstHouse() {
            return this.firstHouse;
        }

        public int getMoveValue() {
            return this.moveValue;
        }

        public void run() {
            this.moveValue = new MoveFinder(this.board, this.player, false).findMove(this.firstHouse, this.heuristic);
        }

        private class MoveFinder {

            private final Board board;

            private final Player player;

            private boolean gameOver;

            private boolean isPlayerTurn;

            public MoveFinder(final Board board, final Player player, final boolean isPlayerTurn) {
                this.board = board;
                this.player = player;
                this.isPlayerTurn = isPlayerTurn;
                nodeCount++;
            }

            public int findMove(final int startIndex, final Heuristic heuristic) {

                try {
                    this.moveSeeds(startIndex);
                } catch (final Exception ignored) {
                }

                if (this.gameOver || MinimaxThread.this.currentLevel >= MinimaxStrategy.this.maxDepth) {

                    if (MinimaxThread.this.currentLevel >= MinimaxStrategy.this.maxDepth) {
                        LOG.trace("Maximum depth reached or game over found.");
                    }

                    // Return heuristic value.
                    if (heuristic == Heuristic.MINIMUM_DIFFERENCE) {
                        if (this.isPlayerTurn) {
                            if (this.player == Player.COMPUTER_ONE) {
                                return this.board.getPlayerTwoKalah().getSeeds() - this.board.getPlayerOneKalah()
                                        .getSeeds();
                            } else {
                                return this.board.getPlayerOneKalah().getSeeds() - this.board.getPlayerTwoKalah()
                                        .getSeeds();
                            }
                        } else {
                            if (this.player == Player.COMPUTER_ONE) {
                                return this.board.getPlayerOneKalah().getSeeds() - this.board.getPlayerTwoKalah()
                                        .getSeeds();
                            } else {
                                return this.board.getPlayerTwoKalah().getSeeds() - this.board.getPlayerOneKalah()
                                        .getSeeds();
                            }
                        }
                    } else {
                        if (this.isPlayerTurn) {
                            if (this.player == Player.COMPUTER_ONE) {
                                return this.board.getPlayerOneHouseValue();
                            } else {
                                return -this.board.getPlayerTwoHouseValue();
                            }
                        } else {
                            if (this.player == Player.COMPUTER_ONE) {
                                return -this.board.getPlayerTwoHouseValue();
                            } else {
                                return this.board.getPlayerOneHouseValue();
                            }
                        }
                    }
                }

                final House[] houses;
                if (this.isPlayerTurn) {
                    if (this.player == Player.COMPUTER_ONE) {
                        houses = this.board.getPlayerOneHouses();
                    } else {
                        houses = this.board.getPlayerTwoHouses();
                    }
                } else {
                    if (this.player == Player.COMPUTER_ONE) {
                        houses = this.board.getPlayerTwoHouses();
                    } else {
                        houses = this.board.getPlayerOneHouses();
                    }
                }

                int bestMove = Integer.MAX_VALUE;
                MinimaxThread.this.currentLevel++;
                for (int i = 0; i < houses.length; i++) {
                    final int moveValue = new MoveFinder(new Board(this.board), this.player, !this.isPlayerTurn)
                            .findMove(i, heuristic);
                    if (moveValue < bestMove) {
                        bestMove = moveValue;
                    }
                }

                return bestMove;
            }

            private void moveSeeds(final int startIndex) {
                final House[] houses = this.player == Player.COMPUTER_ONE ? this.board.getPlayerOneHouses() : this.board
                        .getPlayerTwoHouses();

                if (startIndex > -1 && startIndex < houses.length) {
                    final House selected = houses[startIndex];
                    if (!selected.isEmpty()) {
                        this.board.applyMove(new Move(this.player, startIndex), false);
                    } else {
                        return;
                    }
                }

                this.isPlayerTurn = !this.isPlayerTurn;

                if (this.board.isGameOver()) {
                    this.gameOver = true;
                }

            }
        }
    }
}
