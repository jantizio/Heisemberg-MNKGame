package player;

import mnkgame.*;
import java.util.Random;

import javax.lang.model.util.ElementScanner6;

public class NostroPlayer implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private MNKCellState me;
	private MNKCellState opponent;
	private int TIMEOUT;
	private int M, N, K;

	private int pesi[];
	private MNKCell bestMove;
	private MNKCell globalBestMove;
	private long timerStart;
	private final int INITIAL_DEPTH = 2;
	private boolean timedOut; // is true if the search got interrupted beacause of timeout
	private boolean isTreeCompleted; // is true if the search completed the tree
	private int currentDepth;

	private int[][] positionWeights;
	private MNKCellState[][] winSequence;
	private MNKCellState[][] sevenTrapSequence;
	private MNKCellState[][] openEndSequence;
	private MNKCellState[][] threatSequence;
	private final int[] evalWeights = { 1000000, 100, 10, 15, 20 };

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
		me = first ? MNKCellState.P1 : MNKCellState.P2;
		opponent = first ? MNKCellState.P2 : MNKCellState.P1;
		TIMEOUT = timeout_in_secs;
		this.M = M;
		this.N = N;
		this.K = K;
		isTreeCompleted = true;

		pesi = new int[MNKGameState.WINP2.ordinal() + 1];
		pesi[myWin.ordinal()] = 1;
		pesi[MNKGameState.DRAW.ordinal()] = 0;
		pesi[yourWin.ordinal()] = -1;

		// debug variables
		gameStateCounter = 0;
		numMosse = 0;

		createPositionWeights();
		createWinSequence();
		createSevenTrapSequence();
		createOpenEndSequence();
		createThreatSequence();

		// for (int i = 0; i < 3; i++) {
		// for (int j = 0; j < K; j++) {
		// System.out.print(sevenTrapSequence[i][j] + " ");
		// }
		// System.out.println();
		// }

	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		timerStart = System.currentTimeMillis();
		timedOut = false;
		isTreeCompleted = true;

		gameStateCounter = 0;
		numMosse += 1;

		if (MC.length > 0) {
			MNKCell c = MC[MC.length - 1]; // Recover the last move from MC
			B.markCell(c.i, c.j); // Save the last move in the local MNKBoard
		}
		// If there is just one possible move, return immediately
		if (FC.length == 1)
			return FC[0];

		bestMove = globalBestMove = FC[rand.nextInt(FC.length)]; // select random move

		// iterative deepening search
		for (int depth = 0;; depth++) {
			currentDepth = INITIAL_DEPTH + depth;
			int searchResult = alphabeta(B, currentDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
			// if the time is over stop the loop and the best move is the previous one
			if (timedOut)
				break;

			globalBestMove = bestMove; // update the best move
			System.out.println("Completed search with depth " + currentDepth + ". Best move so far: " + globalBestMove);
			// if the tree is completed the search is over for this move
			// if the score is higher than the value of the win,
			// i found a winning move i can stop the search
			if (isTreeCompleted || searchResult >= evalWeights[0] / 2)
				break;
			isTreeCompleted = true; // if we are here then the flag was false,
									// need to set to true for the next loop
		}

		System.out.println();
		B.markCell(globalBestMove.i, globalBestMove.j);
		return globalBestMove;
	}

	public MNKCell bigSelect(MNKCell[] FC, MNKCell[] MC){
		MNKCell lastMove = MC[MC.length - 1];
		int totalPos = 8;
		int matrAroundPos[][] = {
			{-1, -1},
			{0, -1},
			{1, -1},
			{-1, 0},
			{1, 0},
			{-1, 1},
			{0, 1},
			{1, 1}
		};
		int matrDirectionAround[][] = {
			{1, 1},
			{0, 1},
			{-1, 1},
			{1, 0},
			{-1, 0},
			{1, -1},
			{0, -1},
			{-1, -1}
		};
		//case first: take one of the central cells
		if(MC.length == 0)
			return new MNKCell(Math.ceilDiv(M, 2)-1, Math.ceilDiv(N, 2)-1);
		//case second: take one of the central cells or near to the center if, the center one is taken
		if(MC.length == 1)
		{
			int temp_m=Math.ceilDiv(M, 2), temp_n=Math.ceilDiv(N, 2);
			if(B.cellState(temp_m-1,temp_n-1)==MNKCellState.FREE) return new MNKCell(temp_m-1, temp_n-1);
			return new MNKCell(temp_m, temp_n);
		}
		//middle game cases
		else{
			//case near to lost: FREE [k-2 opponent] FREE
			MNKCellState opponentSequence[] = new MNKCellState[K];
			opponentSequence[0] = opponentSequence[K-1] = MNKCellState.FREE;
			for (int i = 1; i < opponentSequence.length - 1; i++)
				opponentSequence[i] = opponent;
			for (int i = 0; i < totalPos; i++) {
				if(match(B, lastMove.j + matrAroundPos[i][0], lastMove.i + matrAroundPos[i][1], opponentSequence, matrDirectionAround[i][0], matrDirectionAround[i][1], 1))
					return new MNKCell(lastMove.j + matrAroundPos[i][0], lastMove.i + matrAroundPos[i][1]); 
			}
		}

		return MC[0];
	}

	public String playerName() {
		return "TicTacToe PRO"; // TODO: scegliere un nome
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
		gameStateCounter += 1;
		MNKGameState result = b.gameState();
		MNKCell FC[] = b.getFreeCells();
		int bestScore;
		// if we are in terminal state or the depth is reached stop the recursion and
		// evaluate the score of the current board
		if (result != MNKGameState.OPEN || depth == 0) {
			if (depth <= 0)
				isTreeCompleted = false;
			return eval(b);
		}

		if (isMaximazing) {
			bestScore = Integer.MIN_VALUE;
			for (MNKCell c : FC) {
				if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (98.0 / 100.0)) {
					timedOut = true;
					return bestScore;
				}

				b.markCell(c.i, c.j);
				int score = alphabeta(b, depth - 1, alpha, beta, false);
				b.unmarkCell();

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
				if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (99.0 / 100.0)) {
					timedOut = true;
					return bestScore;
				}
				b.markCell(c.i, c.j);
				int score = alphabeta(b, depth - 1, alpha, beta, true);
				b.unmarkCell();

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

	private int eval(MNKBoard b) {
		// MNKGameState result = b.gameState();
		// MNKCell FC[] = b.getFreeCells();
		// int count = 0;

		// if (result != MNKGameState.OPEN)
		// return pesi[result.ordinal()] * (1 + FC.length);
		// TODO: forse è più efficiente fare (M*N-depth) al posto di FC.lenght?
		// verificare se sono uguali in primo luogo

		int[] aiScores = { 0, 0, 0, 0, 0 };
		int[] humanScores = { 0, 0, 0, 0, 0 };

		// if b.gameState() == WINP1 or WINP2 then valuta
		aiScores[0] = evalWins(b, true);
		humanScores[0] = evalWins(b, false);

		aiScores[1] = evalThreats(b, true);
		humanScores[1] = evalThreats(b, false);

		aiScores[2] = evalOpenEnds(b, true);
		humanScores[2] = evalOpenEnds(b, false);

		aiScores[3] = evalPositionWeights(b, true);
		humanScores[3] = evalPositionWeights(b, false);

		aiScores[4] = evalSevenTraps(b, true);
		humanScores[4] = evalSevenTraps(b, false);

		int finalScore = 0;

		for (int i = 0; i < aiScores.length; i++) {
			// System.out.println(aiScores[i] + " - " + humanScores[i]);
			finalScore += (evalWeights[i] * (aiScores[i] - humanScores[i]));
		}

		return finalScore;
	}

	/**
	 * is in the bounds on the board, cost: O(1).
	 * 
	 * @param x pos x.
	 * @param y pos y.
	 * @return true if is in the bound, false if else.
	 */
	private boolean inBounds(int x, int y) {
		return ((0 <= x) && (x < M) && (0 <= y) && (y < N)) ? true : false;
	}

	private int evalPositionWeights(MNKBoard b, boolean isAi) {
		// int player = (isAi) ? aiPosition : humanPosition;
		MNKCellState player = (isAi) ? me : opponent;
		int score = 0;

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				if (b.cellState(i, j) == player) {
					score += positionWeights[i][j];
				}
			}
		}

		return score;
	}

	private void createPositionWeights() {
		positionWeights = new int[M][N];

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				if (inBounds(i + (K - 1), j)) {
					for (int l = 0; l < K; l++) {
						positionWeights[i + l][j]++;
					}
				}

				if (inBounds(i, j + (K - 1))) {
					for (int l = 0; l < K; l++) {
						positionWeights[i][j + l]++;
					}
				}

				if (inBounds(i + (K - 1), j + (K - 1))) {
					for (int l = 0; l < K; l++) {
						positionWeights[i + l][j + l]++;
					}
				}

				if (inBounds(i + (K - 1), j - (K - 1))) {
					for (int l = 0; l < K; l++) {
						positionWeights[i + l][j - l]++;
					}
				}
			}
		}
	}

	private int evalWins(MNKBoard state, boolean isAi) {
		MNKCellState player = (isAi) ? me : opponent;
		return countPositionForward(state, winSequence[player.ordinal()]);
	}

	private void createWinSequence() {
		winSequence = new MNKCellState[MNKCellState.FREE.ordinal() + 1][K];

		for (MNKCellState i : MNKCellState.values()) {
			for (int j = 0; j < K; j++) {
				winSequence[i.ordinal()][j] = i;
			}
		}
	}

	private int evalSevenTraps(MNKBoard state, boolean isAi) {
		MNKCellState player = (isAi) ? me : opponent;

		int sevenTraps = 0;

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N - 1; j++) {
				if (match(state, i, j, sevenTrapSequence[player.ordinal()], -1, 0, 1)) {
					if (match(state, i, j + 1, sevenTrapSequence[player.ordinal()], -1, -1, 1)) {
						sevenTraps++;
					}
				}

				if (match(state, i, j, sevenTrapSequence[player.ordinal()], 1, 0, 1)) {
					if (match(state, i, j + 1, sevenTrapSequence[player.ordinal()], 1, -1, 1)) {
						sevenTraps++;
					}
				}
			}
		}

		for (int i = 0; i < M; i++) {
			for (int j = 1; j < N; j++) {
				if (match(state, i, j, sevenTrapSequence[player.ordinal()], -1, 0, 1)) {
					if (match(state, i, j - 1, sevenTrapSequence[player.ordinal()], -1, 1, 1)) {
						sevenTraps++;
					}
				}

				if (match(state, i, j, sevenTrapSequence[player.ordinal()], 1, 0, 1)) {
					if (match(state, i, j - 1, sevenTrapSequence[player.ordinal()], 1, 1, 1)) {
						sevenTraps++;
					}
				}
			}
		}

		return sevenTraps;
	}

	private void createSevenTrapSequence() {
		sevenTrapSequence = new MNKCellState[MNKCellState.FREE.ordinal() + 1][K];

		for (MNKCellState i : MNKCellState.values()) {
			sevenTrapSequence[i.ordinal()][0] = MNKCellState.FREE;
			for (int j = 1; j < K; j++) {
				sevenTrapSequence[i.ordinal()][j] = i;
			}
		}
	}

	private int evalOpenEnds(MNKBoard state, boolean isAi) {
		MNKCellState player = (isAi) ? me : opponent;

		return (countPositionForward(state, openEndSequence[player.ordinal()])
				+ countPositionBackward(state, openEndSequence[player.ordinal()]));
	}

	private void createOpenEndSequence() {
		openEndSequence = new MNKCellState[MNKCellState.FREE.ordinal() + 1][K + 1];
		for (MNKCellState i : MNKCellState.values()) {
			openEndSequence[i.ordinal()][0] = MNKCellState.FREE;
			for (int j = 1; j < K - 1; j++) {
				openEndSequence[i.ordinal()][j] = i;
			}
			openEndSequence[i.ordinal()][K - 1] = MNKCellState.FREE;
			openEndSequence[i.ordinal()][K] = MNKCellState.FREE;
		}
	}

	private int evalThreats(MNKBoard state, boolean isAi) {
		MNKCellState player = (isAi) ? me : opponent;

		return (countPositionForward(state, threatSequence[player.ordinal()])
				+ countPositionBackward(state, threatSequence[player.ordinal()]));
	}

	private void createThreatSequence() {
		threatSequence = new MNKCellState[MNKCellState.FREE.ordinal() + 1][K];
		for (MNKCellState i : MNKCellState.values()) {
			threatSequence[i.ordinal()][0] = MNKCellState.FREE;
			for (int j = 1; j < K; j++) {
				threatSequence[i.ordinal()][j] = i;
			}
		}

	}

	private void debugMessage(boolean timeout) {
		if (timeout)
			System.out.print("time ended, ");
		System.out.println(
				"(" + playerName() + ")Stati di gioco valutati alla mossa " + numMosse + ": " + gameStateCounter);
	}

	/**
	 * match, cost: O(|sequence|) = O(K).
	 * 
	 * @param board      board.
	 * @param x          pos x.
	 * @param y          pos y.
	 * @param sequence   sequence of simbols to find.
	 * @param directionX direction x.
	 * @param directionY direction y.
	 * @param revert     sequence revertion.
	 * @return true if is thera a sequence, false if else.
	 */
	private boolean match(MNKBoard board, int x, int y, MNKCellState[] sequence, int directionX, int directionY,
			int revert) {
		int s = revert > 0 ? 0 : sequence.length - 1;

		if (this.inBounds(x + directionX * (sequence.length - 1), y + directionY * (sequence.length - 1))) {
			for (int i = 0; i < sequence.length; i++) {
				if (board.cellState(x, y) != sequence[s])
					return false;
				x += directionX;
				y += directionY;
				s += revert;
			}
			return true;
		}
		return false;
	}

	/**
	 * match position FIXED(ideas in meating 15/11/22), cost: O(4K) ~ O(K).
	 * 
	 * @param board     board.
	 * @param x         pos x.
	 * @param y         pos y.
	 * @param sequence  sequence of simbols to find.
	 * @param direction sequence direction.
	 * @return number of sequence starting from the specified position.
	 */
	private int matchPosition(MNKBoard board, int x, int y, MNKCellState[] sequence, int direction) {
		int starting = 0;
		// horiz
		if (match(board, x, y, sequence, 1, 0, direction))
			starting++;
		// vertic
		if (match(board, x, y, sequence, 0, 1, direction))
			starting++;
		// diag right
		if (match(board, x, y, sequence, 1, 1, direction))
			starting++;
		// diag left
		if (match(board, x, y, sequence, 1, -1, direction))
			starting++;
		return starting;
	}

	/**
	 * Find out how many sequence are there in the board(it scans forwards), cost:
	 * O(4MNK) ~ O(MNK).
	 * 
	 * @param board    board to check.
	 * @param sequence looking for sequence.
	 * @return number os sequence.
	 */
	private int countPositionForward(MNKBoard board, MNKCellState[] sequence) {
		int sequenceCount = 0;
		for (int i = 0; i < M; i++)
			for (int j = 0; j < N; j++)
				sequenceCount += matchPosition(board, i, j, sequence, 1);
		return sequenceCount;
	}

	/**
	 * Find out how many sequence are there in the board(it scans backwards), cost:
	 * O(4MNK) ~ O(MNK).
	 * 
	 * @param board    board to check.
	 * @param sequence looking for sequence.
	 * @return number os sequence.
	 */
	private int countPositionBackward(MNKBoard board, MNKCellState[] sequence) {
		int sequenceCount = 0;
		for (int i = 0; i < M; i++)
			for (int j = 0; j < N; j++)
				sequenceCount += matchPosition(board, i, j, sequence, -1);
		return sequenceCount;
	}
}
