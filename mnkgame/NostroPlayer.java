package mnkgame;

import java.util.Random;

public class NostroPlayer implements MNKPlayer {
    private Random rand;
    private MNKBoard B;
    private MNKGameState myWin;
    private MNKGameState yourWin;
    private int TIMEOUT;

    private int pesi[];
    private MNKCell bestMove;
    private long timerStart;

    private int gameStateCounter, numMosse; // debug variables

    /**
     * Default empty constructor
     */
    public NostroPlayer() {
    }

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        // New random seed for each game
        rand = new Random(System.currentTimeMillis());
        B = new MNKBoard(M, N, K);
        myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
        yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
        TIMEOUT = timeout_in_secs;

        pesi = new int[MNKGameState.WINP2.ordinal() + 1];
        pesi[myWin.ordinal()] = 1;
        pesi[MNKGameState.DRAW.ordinal()] = 0;
        pesi[yourWin.ordinal()] = -1;

        // debug variables
        gameStateCounter = 0;
        numMosse = 0;

    }

    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        timerStart = System.currentTimeMillis();

        gameStateCounter = 0;
        numMosse += 1;

        if (MC.length > 0) {
            MNKCell c = MC[MC.length - 1]; // Recover the last move from MC
            B.markCell(c.i, c.j); // Save the last move in the local MNKBoard
        }
        // If there is just one possible move, return immediately
        if (FC.length == 1)
            return FC[0];

        bestMove = FC[rand.nextInt(FC.length)];

        alphabeta(B, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

        // debugMessage(false);
        B.markCell(bestMove.i, bestMove.j);
        return bestMove;
    }

    public String playerName() {
        return "TicTacToe PRO"; // TODO: scegliere un nome
    }

    private int alphabeta(MNKBoard b, int depth, int alpha, int beta, boolean isMaximazing) {
        gameStateCounter += 1;
        // if we are in a terminal state, evaluate the score
        MNKGameState result = b.gameState();
        MNKCell FC[] = b.getFreeCells();
        int bestScore;
        if (result != MNKGameState.OPEN) {
            return pesi[result.ordinal()] * (1 + FC.length);
            // TODO: forse è più efficiente fare (M*N-depth) al posto di FC.lenght?
            // verificare se sono uguali in primo luogo
        }

        if (isMaximazing) {
            bestScore = Integer.MIN_VALUE;
            for (MNKCell c : FC) {
                if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (99.0 / 100.0))
                    return bestScore;

                B.markCell(c.i, c.j);
                int score = alphabeta(B, depth + 1, alpha, beta, false);
                B.unmarkCell();

                if (score > bestScore) {
                    bestScore = score;
                    if (depth == 0)
                        bestMove = c;
                }
                // bestScore = Math.max(score, bestScore);
                alpha = Math.max(alpha, bestScore);
                if (alpha >= beta)
                    break; // beta cutoff

            }
        } else {
            bestScore = Integer.MAX_VALUE;
            for (MNKCell c : FC) {
                if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (99.0 / 100.0))
                    return bestScore;

                B.markCell(c.i, c.j);
                int score = alphabeta(B, depth + 1, alpha, beta, true);
                B.unmarkCell();

                if (score < bestScore) {
                    bestScore = score;
                    if (depth == 0)
                        bestMove = c;
                }
                // bestScore = Math.min(score, bestScore);
                beta = Math.min(beta, bestScore);
                if (beta <= alpha)
                    break;
            }
        }
        return bestScore;
    }

    private void debugMessage(boolean timeout) {
        if (timeout)
            System.out.print("time ended, ");
        System.out.println(
                "(" + playerName() + ")Stati di gioco valutati alla mossa " + numMosse + ": " + gameStateCounter);
    }

}