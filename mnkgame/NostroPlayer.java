package mnkgame;

import java.util.Random;

public class NostroPlayer implements MNKPlayer {
    private Random rand;
    private MNKBoard B;
    private MNKGameState myWin;
    private MNKGameState yourWin;
    private int TIMEOUT;
    private int M, N, K;

    private int pesi[];
    private MNKCell bestMove;
    private long timerStart;
    private int maxDepth;
    private int myMovesToWin[];
    private int yourMovesToWin[];

    private int gameStateCounter, numMosse; // debug variables

    /**
     * Default empty constructor
     */
    public NostroPlayer() {
    }

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        maxDepth = 1;
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

        iterativeDeepening(B, true, 0);

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
        if (result != MNKGameState.OPEN || depth >= maxDepth) {
            return eval(b);
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

    private int iterativeDeepening(MNKBoard b, boolean isMaximazing, int depth) {
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int eval = 0;
        for (int i = 0; i < depth; i++){
            eval = alphabeta(b, 0, alpha, beta, isMaximazing);
            maxDepth += 1;
        }
        return eval;
    }

    private int eval(MNKBoard b){
        MNKGameState result = b.gameState();
        MNKCell FC[] = b.getFreeCells();
        int count = 0;
        for (MNKCell cell : FC) {
            if(isWinningCell(cell.i, cell.j, K - 1)){
                count += 100;
            }else count -= 50;
        }
        if(result != MNKGameState.OPEN)
            return pesi[result.ordinal()] * (1 + FC.length);
        else return count;
        // TODO: forse è più efficiente fare (M*N-depth) al posto di FC.lenght?
        // verificare se sono uguali in primo luogo
    }

    private boolean isWinningCell(int i, int j, int target) {
		MNKCellState s = B.cellState(i, j);
        int n;

		// Useless pedantic check
		if(s == MNKCellState.FREE) return false;

        MNKCellState notS = s == MNKCellState.P1 ? MNKCellState.P2 : MNKCellState.P1;


        int freeN = 0;
        int emptyCheck = K - target;


		// Horizontal check
		n = 1;
		for(int k = 1; j-k >= 0 && freeN<=emptyCheck; k++){ 
            MNKCellState p = B.cellState(i, j-k);
            if(p == MNKCellState.FREE) freeN+=1;
            else if(p != s) break;   
            n++;
        } // backward check
		for(int k = 1; j+k <  N && freeN<=emptyCheck; k++){
            MNKCellState p = B.cellState(i, j-k);
            if(p == MNKCellState.FREE) freeN+=1;
            else if(p != s) break;   
            n++;
        } // forward check   
		if(n >= target) return true;

		// Vertical check
		n = 1;
		for(int k = 1; i-k >= 0 && B.cellState(i-k,j) == s; k++) n++; // backward check
		for(int k = 1; i+k <  M && B.cellState(i+k,j) == s; k++) n++; // forward check
		if(n >= target) return true;
		

		// Diagonal check
		n = 1;
		for(int k = 1; i-k >= 0 && j-k >= 0 && B.cellState(i-k,j-k) == s; k++) n++; // backward check
		for(int k = 1; i+k <  M && j+k <  N && B.cellState(i+k,j+k) == s; k++) n++; // forward check
		if(n >= target) return true;

		// Anti-diagonal check
		n = 1;
		for(int k = 1; i-k >= 0 && j+k < N  && B.cellState(i-k,j+k) == s; k++) n++; // backward check
		for(int k = 1; i+k <  M && j-k >= 0 && B.cellState(i+k,j-k) == s; k++) n++; // backward check
		if(n >= target) return true;

		return false;
	}

    private void debugMessage(boolean timeout) {
        if (timeout)
            System.out.print("time ended, ");
        System.out.println(
                "(" + playerName() + ")Stati di gioco valutati alla mossa " + numMosse + ": " + gameStateCounter);
    }

}