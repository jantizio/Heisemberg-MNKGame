package player;

import mnkgame.*;
import java.util.Random;

public class NostroPlayer implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private int TIMEOUT;
	private int M, N, K;

	private MNKCell bestMove;
	private MNKCell globalBestMove;
	private long timerStart;
	private final int INITIAL_DEPTH = 2;
	private boolean timedOut; // is true if the search got interrupted beacause of timeout
	private boolean isTreeCompleted; // is true if the search completed the tree
	private int currentDepth;

	private boolean firstMove;

	private Evaluator evaluator;

	/**
	 * Default empty constructor
	 */
	public NostroPlayer() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand = new Random(System.currentTimeMillis());
		B = new MNKBoard(M, N, K);
		evaluator = new Evaluator(M, N, K, first);
		TIMEOUT = timeout_in_secs;
		this.M = M;
		this.N = N;
		this.K = K;
		isTreeCompleted = true;
		firstMove = false;
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		timerStart = System.currentTimeMillis();
		timedOut = false;
		isTreeCompleted = true;
		firstMove = false;

		if (MC.length > 0) {
			MNKCell c = MC[MC.length - 1]; // Recover the last move from MC
			B.markCell(c.i, c.j); // Save the last move in the local MNKBoard
			evaluator.calculateIncidence(B, c.i, c.j);
		}
		// If there is just one possible move, return immediately
		if (FC.length == 1)
			return FC[0];

		if (FC.length == M * N) {
			if (M * N < 500) {
				firstMove = true;
			} else {
				B.markCell((M - 1) / 2, (N - 1) / 2);
				evaluator.calculateIncidence(B, globalBestMove.i, globalBestMove.j);
				return B.getMarkedCells()[0]; // zero perchè siamo alla prima mossa
			}
		}

		evaluator.rebaseStore();

		bestMove = globalBestMove = FC[rand.nextInt(FC.length)]; // select random move
		// iterative deepening search
		for (int depth = 0;; depth++) {
			currentDepth = INITIAL_DEPTH + depth;

			int searchResult = alphabeta(B, currentDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
			evaluator.resetStore();

			// if the time is over stop the loop and the best move is the previous one
			if (timedOut)
				break;

			globalBestMove = bestMove; // update the best move

			// if the tree is completed the search is over for this move
			// if the score is higher than the value of the win,
			// i found a winning move i can stop the search
			if (isTreeCompleted || searchResult >= evaluator.evalWeights[0] / 2)
				break;
			isTreeCompleted = true; // if we are here then the flag was false,
									// need to set to true for the next loop
		}

		B.markCell(globalBestMove.i, globalBestMove.j);
		evaluator.calculateIncidence(B, globalBestMove.i, globalBestMove.j);
		return globalBestMove;
	}

	public String playerName() {
		return "Heisenberg-cutoff";
	}

	/**
	 * Performs standard alphabeta pruning algorithm
	 * 
	 * @param b            current game board
	 * @param depth        depth of the tree you want to reach
	 * @param alpha        best possible score for the maximazing player
	 * @param beta         best possible score for the minimizer player
	 * @param isMaximazing whether the current player is the min or max player
	 * @return the score of the given board
	 */
	private int alphabeta(MNKBoard b, int depth, int alpha, int beta, boolean isMaximazing) {
		MNKGameState result = b.gameState();
		MNKCell FC[] = b.getFreeCells();
		int bestScore;
		// if we are in terminal state or the depth is reached stop the recursion and
		// evaluate the score of the current board
		if (result != MNKGameState.OPEN || depth == 0) {
			if (depth <= 0)
				isTreeCompleted = false;
			return evaluator.eval(b);
		}

		if (isMaximazing) {
			bestScore = Integer.MIN_VALUE;
			for (MNKCell c : FC) {
				if (!hasAdjacent(b, c) && !firstMove)
					continue;
				if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (98.0 / 100.0)) {
					timedOut = true;
					return bestScore;
				}

				b.markCell(c.i, c.j);
				evaluator.calculateIncidence(b, c.i, c.j);

				int score = alphabeta(b, depth - 1, alpha, beta, false);

				b.unmarkCell();
				evaluator.undoIncidence();

				if (score > bestScore) {
					bestScore = score;
					if (depth == currentDepth)
						bestMove = c;
				}
				alpha = Math.max(alpha, bestScore);
				if (alpha >= beta)
					break;

			}
		} else {
			bestScore = Integer.MAX_VALUE;
			for (MNKCell c : FC) {

				if (!hasAdjacent(b, c) && !firstMove)
					continue;
				if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (99.0 / 100.0)) {
					timedOut = true;
					return bestScore;
				}

				b.markCell(c.i, c.j);
				evaluator.calculateIncidence(b, c.i, c.j);

				int score = alphabeta(b, depth - 1, alpha, beta, true);

				b.unmarkCell();
				evaluator.undoIncidence();

				if (score < bestScore) {
					bestScore = score;
					if (depth == currentDepth)
						bestMove = c;

				}
				beta = Math.min(beta, bestScore);
				if (beta <= alpha)
					break;
			}
		}
		return bestScore;

	}

	/**
	 * return true if the cell c has at least one non FREE cell
	 * in a 5x5 grid around it. False otherwise
	 * 
	 * @param b the board
	 * @param c the cell
	 * 
	 * @implNote O(9)
	 */
	private boolean hasAdjacent(MNKBoard b, MNKCell c) {
		int range = 2;
		for (int di = -range; di <= range; di++) {
			for (int dj = -range; dj <= range; dj++) {
				if (di == 0 && dj == 0) // case cell c
					continue;
				if (!evaluator.inBounds(c.i + di, c.j + dj)) // case out of bound
					continue;
				if ((Math.abs(di) == 2 && Math.abs(dj) == 1) || (Math.abs(dj) == 2 && Math.abs(di) == 1)) // case cell not aligned
					continue;
				if (b.cellState(c.i + di, c.j + dj) != MNKCellState.FREE) // case cell is adjacent
					return true;
			}
		}
		return false; // case cell isn't adjacent
	}

}
