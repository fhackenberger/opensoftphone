package org.acoveo.tools;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "preCommit")
public class RandomDecisionMakerTest {
	long seed = 1234567890;
	static final int NUM_RUNS = 1000000;
	static final double tolerancePercent = 0.005;
	
	public void testMakeDecision() {
		RandomDecisionMaker make = new RandomDecisionMaker(seed);
		List<Float> weights = new ArrayList<Float>(4);
		weights.add(0.25f);
		weights.add(0.25f);
		weights.add(0.25f);
		weights.add(0.25f);
		long wins[] = {0, 0, 0, 0};
		for(int runIndex = 0; runIndex <= NUM_RUNS; runIndex++) {
			wins[make.makeDecision(weights)]++;
		}
		double expectedWins = 0.25 * NUM_RUNS;
		double lowerLimit = expectedWins - expectedWins * tolerancePercent;
		double upperLimit = expectedWins + expectedWins * tolerancePercent;
		for(long numWins : wins) {
			Assert.assertFalse(numWins < lowerLimit || numWins > upperLimit, numWins + " is out of the allowed range of " + lowerLimit + ", " + upperLimit + " of wins");
		}
	}
}
