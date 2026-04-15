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
	
	int numPlayers = 11;
	Player makePlayer(int which) {
		switch (which) {
		case 0: return new NicePlayer();
		case 1: return new NastyPlayer();
		case 2: return new RandomPlayer();
		case 3: return new TolerantPlayer();
		case 4: return new FreakyPlayer();
		case 5: return new T4TPlayer();
		case 6: return new BinMuhammadTaufiq_Hudzaifah_Player();
		case 7: return new Compare_Player();
		case 8: return new Nice2();
		case 9: return new Nasty2();
		case 10: return new EncourageCoop2();
		}
		throw new RuntimeException("Bad argument passed to makePlayer");
	}
	
	/* Finally, the remaining code actually runs the tournament. */

	public static void main (String[] args) {
		int TOURNAMENT_RUNS = 10000;
		ThreePrisonersDilemma instance = new ThreePrisonersDilemma();

		// Accumulate rank positions over all runs (lower = better)
		int[] rankSum = new int[instance.numPlayers];

		for (int run = 0; run < TOURNAMENT_RUNS; run++) {
			int[] sortedOrder = instance.runTournament(false);
			for (int rank = 0; rank < instance.numPlayers; rank++) {
				rankSum[sortedOrder[rank]] += (rank + 1); // rank 1 = best
			}
		}

		// Print average rankings
		System.out.println("\n=== Average Rankings over " + TOURNAMENT_RUNS + " tournaments ===");
		// Build sorted display by average rank
		double[] avgRank = new double[instance.numPlayers];
		for (int i = 0; i < instance.numPlayers; i++)
			avgRank[i] = (double) rankSum[i] / TOURNAMENT_RUNS;

		// Simple insertion sort for display
		Integer[] order = new Integer[instance.numPlayers];
		for (int i = 0; i < instance.numPlayers; i++) order[i] = i;
		java.util.Arrays.sort(order, (a, b) -> Double.compare(avgRank[a], avgRank[b]));

		for (int i = 0; i < instance.numPlayers; i++) {
			int p = order[i];
			System.out.printf("%d. %-45s avg rank: %.4f%n",
				(i+1), instance.makePlayer(p).name(), avgRank[p]);
		}
	}

	int[] runTournament(boolean verbose) {
		float[] totalScore = new float[numPlayers];

		for (int i=0; i<numPlayers; i++) for (int j=i; j<numPlayers; j++) for (int k=j; k<numPlayers; k++) {
			Player A = makePlayer(i);
			Player B = makePlayer(j);
			Player C = makePlayer(k);
			int rounds = 90 + (int)Math.rint(20 * Math.random());
			float[] matchResults = scoresOfMatch(A, B, C, rounds);
			totalScore[i] = totalScore[i] + matchResults[0];
			totalScore[j] = totalScore[j] + matchResults[1];
			totalScore[k] = totalScore[k] + matchResults[2];
			if (verbose)
				System.out.println(A.name() + " scored " + matchResults[0] +
						" points, " + B.name() + " scored " + matchResults[1] +
						" points, and " + C.name() + " scored " + matchResults[2] + " points.");
		}
		int[] sortedOrder = new int[numPlayers];
		for (int i=0; i<numPlayers; i++) {
			int j=i-1;
			for (; j>=0; j--) {
				if (totalScore[i] > totalScore[sortedOrder[j]])
					sortedOrder[j+1] = sortedOrder[j];
				else break;
			}
			sortedOrder[j+1] = i;
		}
		return sortedOrder;
	} // end of runTournament()
	
	class BinMuhammadTaufiq_Hudzaifah_Player extends Player {

		int myScore = 0, opp1Score = 0, opp2Score = 0;
		int opp1Def = 0, opp2Def = 0;

		int[][][] payoff = {
			{{6, 3}, {3, 0}},
			{{8, 5}, {5, 2}}
		};

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0;

			int r = n - 1;
			int opp1LA = oppHistory1[r];
			int opp2LA = oppHistory2[r];

			// Track scores
			myScore   += payoff[myHistory[r]][opp1LA][opp2LA];
			opp1Score += payoff[opp1LA][opp2LA][myHistory[r]];
			opp2Score += payoff[opp2LA][myHistory[r]][opp1LA];

			// Track defection counts
			opp1Def += opp1LA;
			opp2Def += opp2LA;

			double opp1DefRate = (double) opp1Def / n;
			double opp2DefRate = (double) opp2Def / n;

			// Punish confirmed defectors (defecting > 50% of the time)
			if (opp1DefRate > 0.5 && opp2DefRate > 0.5) return 1;

			// Tolerate a single defection — only retaliate if BOTH defected last round
			if (opp1LA == 1 && opp2LA == 1) return 1;

			// Cooperate with anyone who is mostly cooperative
			return 0;
		}
	}

	class Compare_Player extends Player {

		int[][][] payoff = {
				{{6, 3},
				{3, 0}},
				{{8, 5},
				{5, 2}}};

		int r;
		int[] myHist, opp1Hist, opp2Hist;
		int myScore=0, opp1Score=0, opp2Score=0;
		int opponent1Coop = 0; int opponent2Coop = 0;

		final double LENIENT_THRESHOLD = 0.705;
		final double STRICT_THRESHOLD = 0.750;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0;

			this.r = n - 1;
			this.myHist = myHistory;
			this.opp1Hist = oppHistory1;
			this.opp2Hist = oppHistory2;

			int myLA = myHistory[r];
			int opp1LA = oppHistory1[r];
			int opp2LA = oppHistory2[r];

			this.myScore += payoff[myLA][opp1LA][opp2LA];
			this.opp1Score += payoff[opp1LA][opp2LA][myLA];
			this.opp2Score += payoff[opp2LA][opp1LA][myLA];

			if (n>0) {
				opponent1Coop += oppAction(opp1Hist[r]);
				opponent2Coop += oppAction(opp2Hist[r]);
			}
			double opponent1Coop_prob = opponent1Coop / opp1Hist.length;
			double opponent2Coop_prob = opponent2Coop / opp2Hist.length;

			if ((n>100) && (opponent1Coop_prob<STRICT_THRESHOLD && opponent2Coop_prob<STRICT_THRESHOLD)) {
				return actionWithNoise(1, 99);
			}

			if ((opp1LA+opp2LA ==0)&&(opponent1Coop_prob>LENIENT_THRESHOLD && opponent2Coop_prob>LENIENT_THRESHOLD)) {
				return actionWithNoise(0, 99);
			} else {
				return SoreLoser();
			}
		}

		private int actionWithNoise(int intendedAction, int percent_chance_for_intended_action) {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>() {{
				put(intendedAction, percent_chance_for_intended_action);
				put(oppAction(intendedAction), 100-percent_chance_for_intended_action);
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

		private int SoreLoser() {
			if (myScore>=opp1Score && myScore>=opp2Score) return 0;
			return 1;
		}

		private int oppAction(int action) {
			if (action == 1) return 0;
			return 1;
		}
	}

	class Nice2 extends Player {
		int opp1Def = 0, opp2Def = 0;
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0;
			opp1Def += oppHistory1[n-1];
			opp2Def += oppHistory2[n-1];
			double opp1DefRate = (double) opp1Def / n;
			double opp2DefRate = (double) opp2Def / n;
			// Punish confirmed defectors
			if (opp1DefRate > 0.5 && opp2DefRate > 0.5) return 1;
			// Retaliate only if BOTH defected last round
			if (oppHistory1[n-1] == 1 && oppHistory2[n-1] == 1) return 1;
			// Otherwise cooperate
			return 0;
		}
	}

	class Nasty2 extends Player {
		int intPlayer1Defects = 0, intPlayer2Defects = 0;
		int intRoundRetailate = -1;
		int intObservationRound = 1, intGrudgeRound = 3;
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n > 0) { intPlayer1Defects += oppHistory1[n-1]; intPlayer2Defects += oppHistory2[n-1]; }
			if (n < intObservationRound) return 0;
			if (intRoundRetailate < -1) { intRoundRetailate += 1; intPlayer1Defects = 0; intPlayer2Defects = 0; return 0; }
			if (intRoundRetailate > -1 && n == intRoundRetailate + intGrudgeRound + 1) {
				int p1Coop = 0, p2Coop = 0;
				for (int c = 0; c < intGrudgeRound; c++) {
					p1Coop += oppHistory1[n-1-c] == 0 ? 1 : 0;
					p2Coop += oppHistory2[n-1-c] == 0 ? 1 : 0;
				}
				if (p1Coop > 1 && p2Coop > 1 && (oppHistory1[n-1]+oppHistory2[n-1]) == 0) {
					intRoundRetailate = -2; intPlayer1Defects = 0; intPlayer2Defects = 0; return 0;
				} else { intRoundRetailate = n; return 1; }
			}
			if (intPlayer1Defects + intPlayer2Defects > 0) { intRoundRetailate = n; return 1; }
			return 0;
		}
	}

	class EncourageCoop2 extends Player {
		int myScore = 0, opp1Score = 0, opp2Score = 0;
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 2) return 0;
			if (oppHistory1[n-1]==1 && oppHistory1[n-2]==1 && oppHistory2[n-1]==1 && oppHistory2[n-2]==1) return 1;
			if (isNasty(n, oppHistory1) || isNasty(n, oppHistory2)) return 1;
			if (isRandom(n, oppHistory1) || isRandom(n, oppHistory2)) return 1;
			myScore += payoff[myHistory[n-1]][oppHistory1[n-1]][oppHistory2[n-1]];
			opp1Score += payoff[oppHistory1[n-1]][oppHistory2[n-1]][myHistory[n-1]];
			opp2Score += payoff[oppHistory2[n-1]][oppHistory1[n-1]][myHistory[n-1]];
			if (myScore < opp1Score || myScore < opp2Score) return 1;
			if (Math.random() < 0.5) return oppHistory1[n-1];
			else return oppHistory2[n-1];
		}
		boolean isNasty(int n, int[] h) { for (int i=0; i<n; i++) if (h[i]==0) return false; return n>0; }
		boolean isRandom(int n, int[] h) {
			int s=0; for (int i=0; i<n; i++) s+=h[i];
			return n>0 && Math.abs((double)s/n - 0.5) < 0.025;
		}
	}

} // end of class PrisonersDilemma

