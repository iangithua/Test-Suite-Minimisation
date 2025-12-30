package de.uni_passau.fim.se2.sbse.suite_minimisation.crossover;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.BinaryChromosom;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;

import java.util.Random;

// Implements single-point crossover for BinaryChromosome instances
public class BinaryCrossover implements Crossover<BinaryChromosom> {

    // Random number generator for selecting crossover point
    private final Random random;

    // Default constructor
    public BinaryCrossover() {
        this.random = new Random();
    }

    // Applies single-point crossover on two parent chromosomes
    // and produces two offspring chromosomes
    @Override
    public Pair<BinaryChromosom> apply(
            BinaryChromosom parent1,
            BinaryChromosom parent2) {

        // Retrieve parent gene arrays
        boolean[] genes1 = parent1.getGenes();
        boolean[] genes2 = parent2.getGenes();

        // Initialize offspring gene arrays
        boolean[] offspring1 = new boolean[genes1.length];
        boolean[] offspring2 = new boolean[genes2.length];

        // Select a random crossover point
        int crossoverPoint = random.nextInt(genes1.length);

        // Perform single-point crossover
        for (int i = 0; i < genes1.length; i++) {
            if (i < crossoverPoint) {
                // Copy genes from original parents before crossover point
                offspring1[i] = genes1[i];
                offspring2[i] = genes2[i];
            } else {
                // Swap genes after crossover point
                offspring1[i] = genes2[i];
                offspring2[i] = genes1[i];
            }
        }

        // Return offspring chromosomes while preserving
        // mutation and crossover operators from parents
        return Pair.of(
                new BinaryChromosom(
                        offspring1,
                        parent1.getMutation(),
                        parent1.getCrossover()
                ),
                new BinaryChromosom(
                        offspring2,
                        parent2.getMutation(),
                        parent2.getCrossover()
                )
        );
    }
}

