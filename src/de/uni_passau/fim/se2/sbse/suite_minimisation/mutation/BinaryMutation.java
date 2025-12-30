package de.uni_passau.fim.se2.sbse.suite_minimisation.mutation;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.BinaryChromosom;

import java.util.Random;

public class BinaryMutation implements Mutation<BinaryChromosom> {

    // Probability that a single gene is flipped during mutation (range: 0.0 – 1.0)
    private final double mutationRate;

    // Random number generator used for probabilistic mutation
    private final Random random;

    // Creates a binary mutation operator with the given mutation rate
    public BinaryMutation(double mutationRate) {
        // Store mutation probability
        this.mutationRate = mutationRate;

        // Initialize random generator
        this.random = new Random();
    }

    // Applies mutation to the given binary chromosome
    @Override
    public BinaryChromosom apply(BinaryChromosom chromosome) {

        // Retrieve a defensive copy of the chromosome's genes
        boolean[] genes = chromosome.getGenes();

        // Clone the gene array to avoid modifying the original chromosome
        boolean[] mutatedGenes = genes.clone();

        // Iterate over all genes and mutate them independently
        for (int i = 0; i < mutatedGenes.length; i++) {

            // With probability = mutationRate, flip the gene
            if (random.nextDouble() < mutationRate) {
                mutatedGenes[i] = !mutatedGenes[i];
            }
        }

        // Return a new chromosome containing the mutated genes
        // Reuse this mutation operator and the original crossover strategy
        return new BinaryChromosom(
                mutatedGenes,
                this,
                chromosome.getCrossover()
        );
    }
}
