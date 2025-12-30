package de.uni_passau.fim.se2.sbse.suite_minimisation.mutation;

import static org.junit.jupiter.api.Assertions.*;

import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;
import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.BinaryChromosom;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.Mutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.Crossover;

// Test cases for BinaryMutation
public class BinaryMutationTest {

    // Dummy crossover operator for constructing chromosomes
    private static class DummyCrossover
            implements Crossover<BinaryChromosom> {

        @Override
        public Pair<BinaryChromosom> apply(
                BinaryChromosom p1,
                BinaryChromosom p2) {
            return Pair.of(p1, p2);
        }
    }

    // Test that mutation with rate 0.0 does not change any genes
    @Test
    void testNoMutationWhenRateIsZero() {
        boolean[] genes = { true, false, true, false };

        BinaryMutation mutation = new BinaryMutation(0.0);

        BinaryChromosom original =
                new BinaryChromosom(
                        genes,
                        mutation,
                        new DummyCrossover()
                );

        BinaryChromosom mutated = mutation.apply(original);

        // Genes must remain identical
        assertArrayEquals(original.getGenes(), mutated.getGenes());

        // Original chromosome must remain unchanged
        assertArrayEquals(genes, original.getGenes());
    }

    // Test that mutation with rate 1.0 flips all genes
    @Test
    void testFullMutationWhenRateIsOne() {
        boolean[] genes = { true, false, true };

        BinaryMutation mutation = new BinaryMutation(1.0);

        BinaryChromosom original =
                new BinaryChromosom(
                        genes,
                        mutation,
                        new DummyCrossover()
                );

        BinaryChromosom mutated = mutation.apply(original);

        boolean[] expected = { false, true, false };

        // All genes must be flipped
        assertArrayEquals(expected, mutated.getGenes());
    }

    // Test that mutation creates a new chromosome instance
    @Test
    void testMutationCreatesNewChromosome() {
        boolean[] genes = { true, true };

        BinaryMutation mutation = new BinaryMutation(0.0);

        BinaryChromosom original =
                new BinaryChromosom(
                        genes,
                        mutation,
                        new DummyCrossover()
                );

        BinaryChromosom mutated = mutation.apply(original);

        // Must not return the same object
        assertNotSame(original, mutated);
    }

    // Test that crossover operator is preserved
    @Test
    void testCrossoverIsPreserved() {
        BinaryMutation mutation = new BinaryMutation(0.0);
        DummyCrossover crossover = new DummyCrossover();

        BinaryChromosom original =
                new BinaryChromosom(
                        new boolean[] { true, false },
                        mutation,
                        crossover
                );

        BinaryChromosom mutated = mutation.apply(original);

        // Crossover strategy must be reused
        assertSame(crossover, mutated.getCrossover());
    }

    // Test that mutation operator is reused in mutated chromosome
    @Test
    void testMutationOperatorIsReused() {
        BinaryMutation mutation = new BinaryMutation(0.5);

        BinaryChromosom original =
                new BinaryChromosom(
                        new boolean[] { false, false },
                        mutation,
                        new DummyCrossover()
                );

        BinaryChromosom mutated = mutation.apply(original);

        // Mutation operator must be the same instance
        assertSame(mutation, mutated.getMutation());
    }
}

