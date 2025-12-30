package de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes;

import static org.junit.jupiter.api.Assertions.*;

import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;
import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.Mutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.Crossover;

// Test cases for BinaryChromosom
public class BinaryChromosomTest {

    // Dummy mutation operator for testing
    private static class DummyMutation implements Mutation<BinaryChromosom> {
        @Override
        public BinaryChromosom apply(BinaryChromosom chromosome) {
            return chromosome;
        }
    }

    // Dummy crossover operator for testing
    private static class DummyCrossover
            implements Crossover<BinaryChromosom> {

        @Override
        public Pair<BinaryChromosom> apply(
                BinaryChromosom parent1,
                BinaryChromosom parent2) {

            // Return parents unchanged as dummy offspring
            return Pair.of(parent1, parent2);
        }
    }


    // Test that genes are defensively copied in constructor
    @Test
    void testDefensiveCopyInConstructor() {
        boolean[] genes = { true, false, true };

        BinaryChromosom chromosome =
                new BinaryChromosom(genes, new DummyMutation(), new DummyCrossover());

        // Modify original array
        genes[0] = false;

        // Internal genes must remain unchanged
        assertTrue(chromosome.getGenes()[0]);
    }

    // Test that getGenes returns a defensive copy
    @Test
    void testGetGenesReturnsCopy() {
        boolean[] genes = { true, false, false };

        BinaryChromosom chromosome =
                new BinaryChromosom(genes, new DummyMutation(), new DummyCrossover());

        boolean[] returnedGenes = chromosome.getGenes();
        returnedGenes[1] = true;

        // Internal state must not change
        assertFalse(chromosome.getGenes()[1]);
    }

    // Test counting of included test cases
    @Test
    void testIncludedTestCasesCount() {
        boolean[] genes = { true, false, true, true };

        BinaryChromosom chromosome =
                new BinaryChromosom(genes, new DummyMutation(), new DummyCrossover());

        assertEquals(3, chromosome.getIncludedTestCasesCount());
    }

    // Test isTestCaseIncluded for valid indices
    @Test
    void testIsTestCaseIncluded() {
        boolean[] genes = { false, true, false };

        BinaryChromosom chromosome =
                new BinaryChromosom(genes, new DummyMutation(), new DummyCrossover());

        assertFalse(chromosome.isTestCaseIncluded(0));
        assertTrue(chromosome.isTestCaseIncluded(1));
        assertFalse(chromosome.isTestCaseIncluded(2));
    }

    // Test isTestCaseIncluded throws exception for invalid index
    @Test
    void testIsTestCaseIncludedInvalidIndex() {
        boolean[] genes = { true, false };

        BinaryChromosom chromosome =
                new BinaryChromosom(genes, new DummyMutation(), new DummyCrossover());

        assertThrows(IndexOutOfBoundsException.class,
                () -> chromosome.isTestCaseIncluded(-1));

        assertThrows(IndexOutOfBoundsException.class,
                () -> chromosome.isTestCaseIncluded(2));
    }

    // Test copy method creates a deep copy
    @Test
    void testCopyCreatesDeepCopy() {
        boolean[] genes = { true, false, true };

        BinaryChromosom original =
                new BinaryChromosom(genes, new DummyMutation(), new DummyCrossover());

        BinaryChromosom copy = original.copy();

        // Objects must not be the same reference
        assertNotSame(original, copy);

        // Gene arrays must be equal but independent
        assertArrayEquals(original.getGenes(), copy.getGenes());
    }

    // Test self method returns this
    @Test
    void testSelfReturnsThis() {
        BinaryChromosom chromosome =
                new BinaryChromosom(
                        new boolean[] { true },
                        new DummyMutation(),
                        new DummyCrossover()
                );

        assertSame(chromosome, chromosome.self());
    }

    // Test equality based on gene array
    @Test
    void testEquals() {
        boolean[] genes1 = { true, false, true };
        boolean[] genes2 = { true, false, true };

        BinaryChromosom c1 =
                new BinaryChromosom(genes1, new DummyMutation(), new DummyCrossover());
        BinaryChromosom c2 =
                new BinaryChromosom(genes2, new DummyMutation(), new DummyCrossover());

        assertEquals(c1, c2);
    }

    // Test hashCode consistency with equals
    @Test
    void testHashCode() {
        boolean[] genes1 = { false, true };
        boolean[] genes2 = { false, true };

        BinaryChromosom c1 =
                new BinaryChromosom(genes1, new DummyMutation(), new DummyCrossover());
        BinaryChromosom c2 =
                new BinaryChromosom(genes2, new DummyMutation(), new DummyCrossover());

        assertEquals(c1.hashCode(), c2.hashCode());
    }

    // Test inequality for different gene arrays
    @Test
    void testNotEqualsForDifferentGenes() {
        BinaryChromosom c1 =
                new BinaryChromosom(
                        new boolean[] { true, false },
                        new DummyMutation(),
                        new DummyCrossover()
                );

        BinaryChromosom c2 =
                new BinaryChromosom(
                        new boolean[] { false, true },
                        new DummyMutation(),
                        new DummyCrossover()
                );

        assertNotEquals(c1, c2);
    }
}

