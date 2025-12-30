package de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes;

import java.util.Arrays;

import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.Crossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.Mutation;

// Binary chromosome representation for test-suite minimisation.
// Each gene indicates whether a test case is selected (true) or not (false).
public class BinaryChromosom extends Chromosome<BinaryChromosom> {

    // Array of binary genes representing test case inclusion
    private final boolean[] genes;

    // Main constructor accepting genes and genetic operators
    public BinaryChromosom(
            boolean[] genes,
            Mutation<BinaryChromosom> mutation,
            Crossover<BinaryChromosom> crossover
    ) {
        // Initialize mutation and crossover in the superclass
        super(mutation, crossover);

        // Defensive copy to prevent external modification
        this.genes = genes.clone();
    }

    // Copy constructor for deep cloning
    public BinaryChromosom(BinaryChromosom other) {
        // Copy mutation and crossover strategies
        super(other);

        // Clone gene array to ensure independence
        this.genes = other.genes.clone();
    }

    // Returns a copy of the gene array to preserve encapsulation
    public boolean[] getGenes() {
        return genes.clone();
    }

    // Counts how many test cases are included (true genes)
    public int getIncludedTestCasesCount() {
        int count = 0;

        // Iterate through all genes and count selected ones
        for (boolean gene : genes) {
            if (gene) {
                count++;
            }
        }
        return count;
    }

    // Checks whether the test case at the given index is included
    public boolean isTestCaseIncluded(int index) {
        // Validate index bounds
        if (index < 0 || index >= genes.length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }

        // Return inclusion status of the test case
        return genes[index];
    }

    // Creates and returns a deep copy of this chromosome
    @Override
    public BinaryChromosom copy() {
        return new BinaryChromosom(this);
    }

    // Returns this object as its own generic type
    @Override
    public BinaryChromosom self() {
        return this;
    }

    // Checks equality based on gene sequence
    @Override
    public boolean equals(Object other) {
        // Check for reference equality
        if (this == other) {
            return true;
        }

        // Ensure the other object is of the same type
        if (!(other instanceof BinaryChromosom)) {
            return false;
        }

        // Compare gene arrays
        BinaryChromosom that = (BinaryChromosom) other;
        return Arrays.equals(this.genes, that.genes);
    }

    // Computes hash code based on gene array
    @Override
    public int hashCode() {
        return Arrays.hashCode(genes);
    }
}
