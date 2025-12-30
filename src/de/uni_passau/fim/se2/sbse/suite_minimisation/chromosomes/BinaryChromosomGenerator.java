package de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes;

import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.BinaryCrossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.BinaryMutation;

import java.util.Random;

// Generates BinaryChromosom instances for a genetic algorithm.
// The generation strategy adapts based on how many chromosomes
// have already been generated.
public class BinaryChromosomGenerator
        implements ChromosomeGenerator<BinaryChromosom> {

    // Number of genes in each chromosome
    private final int geneLength;

    // Random number generator used for gene creation
    private final Random random;

    // Tracks the number of chromosomes generated so far
    private int generationCount = 0;

    // Generation thresholds for strategy switching
    private static final int EARLY_GENERATION_LIMIT = 50;
    private static final int MID_GENERATION_LIMIT = 100;

    // Probability used by the mutation operator
    private static final double MUTATION_RATE = 0.1;

    // Creates a generator with the given gene length
    public BinaryChromosomGenerator(int geneLength) {
        this.geneLength = geneLength;
        this.random = new Random();
    }

    // Generates a new BinaryChromosom using a strategy
    // determined by the current generation count
    @Override
    public BinaryChromosom get() {
        boolean[] genes = new boolean[geneLength];

        // Select gene initialization strategy
        if (generationCount < EARLY_GENERATION_LIMIT) {
            generateEarlyGenes(genes);
        } else if (generationCount < MID_GENERATION_LIMIT) {
            generateMidGenes(genes);
        } else {
            generateRandomGenes(genes);
        }

        // Ensure at least one gene is selected
        ensureAtLeastOneGeneSelected(genes);

        // Increment generation counter
        generationCount++;

        // Create and return the chromosome with mutation and crossover operators
        return new BinaryChromosom(
                genes,
                new BinaryMutation(MUTATION_RATE),
                new BinaryCrossover()
        );
    }

    // Early generations:
    // Vary gene density to encourage exploration
    private void generateEarlyGenes(boolean[] genes) {
        double density = (generationCount % 5 + 1) / 5.0;
        for (int i = 0; i < genes.length; i++) {
            genes[i] = random.nextDouble() < density;
        }
    }

    // Mid generations:
    // Activate a contiguous random block of genes
    private void generateMidGenes(boolean[] genes) {
        int maxBlockSize = Math.max(1, genes.length / 4);
        int blockSize = random.nextInt(maxBlockSize) + 1;
        int startPos = random.nextInt(genes.length - blockSize);

        for (int i = startPos; i < startPos + blockSize; i++) {
            genes[i] = true;
        }
    }

    // Later generations:
    // Pure random gene selection
    private void generateRandomGenes(boolean[] genes) {
        for (int i = 0; i < genes.length; i++) {
            genes[i] = random.nextBoolean();
        }
    }

    // Ensures at least one gene is set to true
    // to avoid invalid chromosomes
    private void ensureAtLeastOneGeneSelected(boolean[] genes) {
        for (boolean gene : genes) {
            if (gene) {
                return;
            }
        }
        genes[random.nextInt(genes.length)] = true;
    }
}
