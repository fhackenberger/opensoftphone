package org.acoveo.tools;

import java.util.Collection;
import java.util.Random;

public class RandomDecisionMaker {
	Random randomNumberGenerator;
	
	public RandomDecisionMaker() {
		randomNumberGenerator = new Random();
	}
	
	public RandomDecisionMaker(long randomSeed) {
		randomNumberGenerator = new Random(randomSeed);
	}

	/** Makes a random decision between weighted alternatives
	 * 
	 * @param weights A list of weights (need not sum to 1)
	 * @return The randomly chosen alternative index (zero-based) into the weights parameter.
	 */
	public int makeDecision(Collection<Float> weights) {
		double weightSum = CollectionTools.sum(weights);
		float randomNumber = randomNumberGenerator.nextFloat();
		double scaledRandomNumber = randomNumber * weightSum;
		double lowerBound = 0.0f;
		int index = 0;
		for(float weight : weights) {
			double upperBound = lowerBound + weight;
			if(scaledRandomNumber >= lowerBound && scaledRandomNumber <= upperBound) {
				return index;
			}
			index++;
			lowerBound = upperBound;
		}
		return index;
	}
	
	/** Convenience method for {@link #makeDecision(Collection)}
	 * 
	 * @see #makeDecision(Collection)
	 */
	public int makeDecision(float[] weights) {
		return makeDecision(new FloatCollection(weights));
	}
}
