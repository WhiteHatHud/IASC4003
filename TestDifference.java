import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ThreePrisonersDilemma {
	
	/* 
	 This Java program models the two-player Prisoner's Dilemma game.
	 We use the integer "0" to represent cooperation, and "1" to represent 
	 defection. 
	 
	 Recall that in the 2-players dilemma, U(DC) > U(CC) > U(DD) > U(CD), where
	 we give the payoff for the first player in the list. We want the three-player game 
	 to resemble the 2-player game whenever one player's response is fixed, and we
	 also want symmetry, so U(CCD) = U(CDC) etc. This gives the unique ordering
	 
	 U(DCC) > U(CCC) > U(DDC) > U(CDC) > U(DDD) > U(CDD)
	 
	 The payoffs for player 1 are given by the following matrix: */
	
	static int[][][] payoff = {  
		{{6,3},  //payoffs when first and second players cooperate 
		 {3,0}}, //payoffs when first player coops, second defects
		{{8,5},  //payoffs when first player defects, second coops
	     {5,2}}};//payoffs when first and second players defect
	
	/* 
	 So payoff[i][j][k] represents the payoff to player 1 when the first
	 player's action is i, the second player's action is j, and the
	 third player's action is k.
	 
	 In this simulation, triples of players will play each other repeatedly in a
	 'match'. A match consists of about 100 rounds, and your score from that match
	 is the average of the payoffs from each round of that match. For each round, your
	 strategy is given a list of the previous plays (so you can remember what your 
	 opponent did) and must compute the next action.  */
	
	
	abstract class Player {
		// This procedure takes in the number of rounds elapsed so far (n), and 
		// the previous plays in the match, and returns the appropriate action.
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			throw new RuntimeException("You need to override the selectAction method.");
		}
		
		// Used to extract the name of this player class.
		final String name() {
			String result = getClass().getName();
			return result.substring(result.indexOf('$')+1);
		}
	}
	
	/* Here are four simple strategies: */
	
	class NicePlayer extends Player {
		//NicePlayer always cooperates
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return 0; 
		}
	}
	
	class NastyPlayer extends Player {
		//NastyPlayer always defects
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return 1; 
		}
	}
	
	class RandomPlayer extends Player {
		//RandomPlayer randomly picks his action each time
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (Math.random() < 0.5)
				return 0;  //cooperates half the time
			else
				return 1;  //defects half the time
		}
	}
	
	class TolerantPlayer extends Player {
		//TolerantPlayer looks at his opponents' histories, and only defects
		//if at least half of the other players' actions have been defects
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			int opponentCoop = 0;
			int opponentDefect = 0;
			for (int i=0; i<n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			for (int i=0; i<n; i++) {
				if (oppHistory2[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			if (opponentDefect > opponentCoop)
				return 1;
			else
				return 0;
		}
	}
	
	class FreakyPlayer extends Player {
		//FreakyPlayer determines, at the start of the match, 
		//either to always be nice or always be nasty. 
		//Note that this class has a non-trivial constructor.
		int action;
		FreakyPlayer() {
			if (Math.random() < 0.5)
				action = 0;  //cooperates half the time
			else
				action = 1;  //defects half the time
		}
		
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return action;
		}	
	}

	class T4TPlayer extends Player {
		//Picks a random opponent at each play, 
		//and uses the 'tit-for-tat' strategy against them 
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0; //cooperate by default
			if (Math.random() < 0.5)
				return oppHistory1[n-1];
			else
				return oppHistory2[n-1];
		}	
	}

	
	/* In our tournament, each pair of strategies will play one match against each other. 
	 This procedure simulates a single match and returns the scores. */
	float[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
		int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
		float ScoreA = 0, ScoreB = 0, ScoreC = 0;
		
		for (int i=0; i<rounds; i++) {
			int PlayA = A.selectAction(i, HistoryA, HistoryB, HistoryC);
			int PlayB = B.selectAction(i, HistoryB, HistoryC, HistoryA);
			int PlayC = C.selectAction(i, HistoryC, HistoryA, HistoryB);
			ScoreA = ScoreA + payoff[PlayA][PlayB][PlayC];
			ScoreB = ScoreB + payoff[PlayB][PlayC][PlayA];
			ScoreC = ScoreC + payoff[PlayC][PlayA][PlayB];
			HistoryA = extendIntArray(HistoryA, PlayA);
			HistoryB = extendIntArray(HistoryB, PlayB);
			HistoryC = extendIntArray(HistoryC, PlayC);
		}
		float[] result = {ScoreA/rounds, ScoreB/rounds, ScoreC/rounds};
		return result;
	}
	
//	This is a helper function needed by scoresOfMatch.
	int[] extendIntArray(int[] arr, int next) {
		int[] result = new int[arr.length+1];
		for (int i=0; i<arr.length; i++) {
			result[i] = arr[i];
		}
		result[result.length-1] = next;
		return result;
	}
	
	/* The procedure makePlayer is used to reset each of the Players 
	 (strategies) in between matches. When you add your own strategy,
	 you will need to add a new entry to makePlayer, and change numPlayers.*/
	
	int numPlayers = 7;
	Player makePlayer(int which) {
		switch (which) {
		case 0: return new NicePlayer();
		case 1: return new NastyPlayer();
		case 2: return new RandomPlayer();
		case 3: return new TolerantPlayer();
		case 4: return new FreakyPlayer();
		case 5: return new T4TPlayer();
		case 6: return new BinMuhammadTaufiq_Hudzaifah_Player();
		}
		throw new RuntimeException("Bad argument passed to makePlayer");
	}
	
	/* Finally, the remaining code actually runs the tournament. */
	
	public static void main (String[] args) {
		ThreePrisonersDilemma instance = new ThreePrisonersDilemma();
		instance.runTournament();
	}
	
	boolean verbose = true; // set verbose = false if you get too much text output
	
	void runTournament() {
		float[] totalScore = new float[numPlayers];

		// This loop plays each triple of players against each other.
		// Note that we include duplicates: two copies of your strategy will play once
		// against each other strategy, and three copies of your strategy will play once.

		for (int i=0; i<numPlayers; i++) for (int j=i; j<numPlayers; j++) for (int k=j; k<numPlayers; k++) {

			Player A = makePlayer(i); // Create a fresh copy of each player
			Player B = makePlayer(j);
			Player C = makePlayer(k);
			int rounds = 90 + (int)Math.rint(20 * Math.random()); // Between 90 and 110 rounds
			float[] matchResults = scoresOfMatch(A, B, C, rounds); // Run match
			totalScore[i] = totalScore[i] + matchResults[0];
			totalScore[j] = totalScore[j] + matchResults[1];
			totalScore[k] = totalScore[k] + matchResults[2];
			if (verbose)
				System.out.println(A.name() + " scored " + matchResults[0] +
						" points, " + B.name() + " scored " + matchResults[1] + 
						" points, and " + C.name() + " scored " + matchResults[2] + " points.");
		}
		int[] sortedOrder = new int[numPlayers];
		// This loop sorts the players by their score.
		for (int i=0; i<numPlayers; i++) {
			int j=i-1;
			for (; j>=0; j--) {
				if (totalScore[i] > totalScore[sortedOrder[j]]) 
					sortedOrder[j+1] = sortedOrder[j];
				else break;
			}
			sortedOrder[j+1] = i;
		}
		
		// Finally, print out the sorted results.
		if (verbose) System.out.println();
		System.out.println("Tournament Results");
		for (int i=0; i<numPlayers; i++) 
			System.out.println(makePlayer(sortedOrder[i]).name() + ": " 
				+ totalScore[sortedOrder[i]] + " points.");
		
	} // end of runTournament()
	
	class BinMuhammadTaufiq_Hudzaifah_Player extends Player {

		/**
		 * Strategy: Adaptive 4-Rule Agent
		 *
		 * Rule 0 (overarching): Be trustworthy AND unpredictable.
		 *   Execute intended action 99% of the time, opposite 1% of the time.
		 *
		 * Rule 1: Protect myself.
		 *   After n > 100, if BOTH opponents' cooperation probability is below
		 *   STRICT_THRESHOLD (0.750), defect. Only fires late so early noise
		 *   does not trigger premature punishment.
		 *
		 * Rule 2: Cooperate in a cooperative environment.
		 *   If BOTH opponents cooperated last round AND both have cooperation
		 *   probability above LENIENT_THRESHOLD (0.705), cooperate.
		 *   This is the primary path for the majority of rounds.
		 *
		 * Rule 3: Sore Loser fallback.
		 *   When Rules 1 and 2 do not apply, use cumulative score as signal.
		 *   If winning or tied: cooperate (no need to act).
		 *   If losing: defect (stop the gap from widening).
		 */

		// Previous round index
		int r;

		// History references — updated each round for use in helper methods
		int[] myHist, opp1Hist, opp2Hist;

		// Cumulative scores — tracked manually using payoff matrix each round
		int myScore = 0, opp1Score = 0, opp2Score = 0;

		// Running cooperation counts for each opponent (full history, not rolling)
		int opp1Coop = 0;
		int opp2Coop = 0;

		// Lenient threshold — used in Rule 2 to reward broadly cooperative opponents
		final double LENIENT_THRESHOLD = 0.705;

		// Strict threshold — used in Rule 1 only after n > 100 for reliable stats
		final double STRICT_THRESHOLD = 0.750;

		// Local copy of payoff matrix for score tracking in helper methods
		int[][][] payoff = {
			{{6, 3}, {3, 0}},
			{{8, 5}, {5, 2}}
		};

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			// Always cooperate on the first round — establish goodwill
			if (n == 0) return 0;

			// Update round index and history references for helper methods
			this.r        = n - 1;
			this.myHist   = myHistory;
			this.opp1Hist = oppHistory1;
			this.opp2Hist = oppHistory2;

			// Capture last actions for all three players
			int myLA   = myHistory[r];
			int opp1LA = oppHistory1[r];
			int opp2LA = oppHistory2[r];

			// Update cumulative scores using payoff matrix
			this.myScore   += payoff[myLA][opp1LA][opp2LA];
			this.opp1Score += payoff[opp1LA][opp2LA][myLA];
			this.opp2Score += payoff[opp2LA][opp1LA][myLA];

			// Update cooperation counters (getOppAction returns 1 if they cooperated)
			opp1Coop += getOppAction(opp1Hist[r]);
			opp2Coop += getOppAction(opp2Hist[r]);

			// Calculate cooperation probabilities over full history
			double opp1CoopProb = (double) opp1Coop / opp1Hist.length;
			double opp2CoopProb = (double) opp2Coop / opp2Hist.length;

			// ----------------------------------------------------------------
			// EARLY NASTY DETECTION
			// After 10 rounds, if neither opponent has cooperated even once,
			// they are pure defectors — no need to wait until round 100.
			// ----------------------------------------------------------------
			if (n > 10 && opp1Coop == 0 && opp2Coop == 0) {
				return 1;
			}

			// ----------------------------------------------------------------
			// RULE 1 — PROTECT MYSELF
			// After 100 rounds the cooperation probability is statistically
			// reliable. If both opponents cooperate less than 75% of the time,
			// they are confirmed as predominantly nasty — defect to cut losses.
			// ----------------------------------------------------------------
			if ((n > 100)
					&& (opp1CoopProb < STRICT_THRESHOLD)
					&& (opp2CoopProb < STRICT_THRESHOLD)) {
				return applyNoise(1, 99);
			}

			// ----------------------------------------------------------------
			// ASYMMETRIC OPPONENT HANDLING
			// One opponent is confirmed nasty, the other is cooperative.
			// Cooperate to preserve the good relationship — the nasty opponent
			// hurts us regardless of our action, so don't sacrifice the good one.
			// ----------------------------------------------------------------
			boolean opp1Nasty = (n > 100) && (opp1CoopProb < STRICT_THRESHOLD);
			boolean opp2Nasty = (n > 100) && (opp2CoopProb < STRICT_THRESHOLD);
			if ((opp1Nasty && opp2CoopProb > LENIENT_THRESHOLD)
					|| (opp2Nasty && opp1CoopProb > LENIENT_THRESHOLD)) {
				return applyNoise(0, 99);
			}

			// ----------------------------------------------------------------
			// RULE 2 — COOPERATE IN A COOPERATIVE ENVIRONMENT
			// Both cooperated last round AND both have cooperative histories.
			// Sustain mutual cooperation (6 pts/round is the optimal equilibrium).
			// ----------------------------------------------------------------
			if ((opp1LA + opp2LA == 0)
					&& (opp1CoopProb > LENIENT_THRESHOLD)
					&& (opp2CoopProb > LENIENT_THRESHOLD)) {
				return applyNoise(0, 99);
			}

			// ----------------------------------------------------------------
			// RULE 3 — SORE LOSER FALLBACK (proportional)
			// Use the size of the score gap, not just win/lose:
			//   Gap > 50  → defect (hemorrhaging, must act)
			//   Gap < 10  → cooperate (trivial gap, not worth breaking trust)
			//   Otherwise → defect (meaningful deficit, stop the bleeding)
			// ----------------------------------------------------------------
			return handleAmbiguousCase();
		}

		/**
		 * Rule 0: Introduces 1% noise so the agent is unpredictable.
		 * Executes intendedAction (pct)% of the time, opposite (1-pct)%.
		 */
		private int applyNoise(int intendedAction, int pct) {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>() {{
				put(intendedAction, pct);
				put(getOppAction(intendedAction), 1 - pct);
			}};
			LinkedList<Integer> list = new LinkedList<>();
			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
				for (int i = 0; i < entry.getValue(); i++) {
					list.add(entry.getKey());
				}
			}
			Collections.shuffle(list);
			return list.pop();
		}

		/**
		 * Rule 3 (proportional): Cooperate if winning or gap is trivial,
		 * defect only when the deficit is meaningful.
		 */
		private int handleAmbiguousCase() {
			int maxOpponentScore = Math.max(opp1Score, opp2Score);
			int gap = maxOpponentScore - myScore;
			if (gap < 10) return 0;  // winning or close — cooperate
			if (gap > 50) return 1;  // far behind — defect hard
			return 1;                // meaningful deficit — defect
		}

		/**
		 * Returns the opposite action (0→1, 1→0).
		 * Also used to convert a cooperation move into a +1 cooperation count.
		 */
		private int getOppAction(int action) {
			if (action == 1) return 0;
			return 1;
		}
	}

} // end of class PrisonersDilemma

