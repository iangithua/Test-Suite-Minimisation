package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class PairTest {

    @Test
    @DisplayName("Should create pair with two non-null elements")
    void testConstructorWithValidElements() {
        Pair<String> pair = new Pair<>("first", "second");

        assertEquals("first", pair.getFst());
        assertEquals("second", pair.getSnd());
    }

    @Test
    @DisplayName("Should throw NullPointerException when first element is null")
    void testConstructorThrowsExceptionForNullFirstElement() {
        assertThrows(NullPointerException.class, () ->
                new Pair<String>(null, "second")
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when second element is null")
    void testConstructorThrowsExceptionForNullSecondElement() {
        assertThrows(NullPointerException.class, () ->
                new Pair<String>("first", null)
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when both elements are null")
    void testConstructorThrowsExceptionForBothNullElements() {
        assertThrows(NullPointerException.class, () ->
                new Pair<String>(null, null)
        );
    }

    @Test
    @DisplayName("Should create pair from copy constructor")
    void testCopyConstructor() {
        Pair<Integer> original = new Pair<>(10, 20);
        Pair<Integer> copy = new Pair<>(original);

        assertEquals(original.getFst(), copy.getFst());
        assertEquals(original.getSnd(), copy.getSnd());
        assertNotSame(original, copy);
    }

    @Test
    @DisplayName("Should throw NullPointerException when copy source is null")
    void testCopyConstructorThrowsExceptionForNullPair() {
        assertThrows(NullPointerException.class, () ->
                new Pair<String>((Pair<String>) null)
        );
    }

    @Test
    @DisplayName("Should create pair using of() factory method")
    void testOfFactoryMethod() {
        Pair<String> pair = Pair.of("alpha", "beta");

        assertEquals("alpha", pair.getFst());
        assertEquals("beta", pair.getSnd());
    }

    @Test
    @DisplayName("Should throw NullPointerException in of() when first element is null")
    void testOfThrowsExceptionForNullFirstElement() {
        assertThrows(NullPointerException.class, () ->
                Pair.of(null, "second")
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException in of() when second element is null")
    void testOfThrowsExceptionForNullSecondElement() {
        assertThrows(NullPointerException.class, () ->
                Pair.of("first", null)
        );
    }

    @Test
    @DisplayName("Should generate pair with constant supplier")
    void testGenerateWithConstantSupplier() {
        Supplier<Integer> constantSupplier = () -> 42;
        Pair<Integer> pair = Pair.generate(constantSupplier);

        assertEquals(42, pair.getFst());
        assertEquals(42, pair.getSnd());
    }

    @Test
    @DisplayName("Should generate pair with counter supplier")
    void testGenerateWithCounterSupplier() {
        AtomicInteger counter = new AtomicInteger(0);
        Supplier<Integer> counterSupplier = counter::incrementAndGet;
        Pair<Integer> pair = Pair.generate(counterSupplier);

        assertEquals(1, pair.getFst());
        assertEquals(2, pair.getSnd());
    }

    @Test
    @DisplayName("Should throw NullPointerException when supplier is null")
    void testGenerateThrowsExceptionForNullSupplier() {
        assertThrows(NullPointerException.class, () ->
                Pair.generate(null)
        );
    }

    @Test
    @DisplayName("Should return first element")
    void testGetFst() {
        Pair<String> pair = new Pair<>("first", "second");
        assertEquals("first", pair.getFst());
    }

    @Test
    @DisplayName("Should return second element")
    void testGetSnd() {
        Pair<String> pair = new Pair<>("first", "second");
        assertEquals("second", pair.getSnd());
    }

    @Test
    @DisplayName("Should map both elements with single mapper")
    void testMapWithSingleMapper() {
        Pair<Integer> pair = new Pair<>(5, 10);
        Function<Integer, Integer> doubleMapper = x -> x * 2;

        Pair<Integer> mapped = pair.map(doubleMapper);

        assertEquals(10, mapped.getFst());
        assertEquals(20, mapped.getSnd());
    }

    @Test
    @DisplayName("Should map elements with different types")
    void testMapChangesType() {
        Pair<Integer> pair = new Pair<>(5, 10);
        Function<Integer, String> stringMapper = Object::toString;

        Pair<String> mapped = pair.map(stringMapper);

        assertEquals("5", mapped.getFst());
        assertEquals("10", mapped.getSnd());
    }

    @Test
    @DisplayName("Should map with different mappers for each element")
    void testMapWithDifferentMappers() {
        Pair<Integer> pair = new Pair<>(5, 10);
        Function<Integer, Integer> addOne = x -> x + 1;
        Function<Integer, Integer> multiplyTwo = x -> x * 2;

        Pair<Integer> mapped = pair.map(addOne, multiplyTwo);

        assertEquals(6, mapped.getFst());
        assertEquals(20, mapped.getSnd());
    }

    @Test
    @DisplayName("Should throw NullPointerException when single mapper is null")
    void testMapThrowsExceptionForNullMapper() {
        Pair<Integer> pair = new Pair<>(5, 10);

        assertThrows(NullPointerException.class, () ->
                pair.map((Function<Integer, Integer>) null)
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when first mapper is null")
    void testMapThrowsExceptionForNullFirstMapper() {
        Pair<Integer> pair = new Pair<>(5, 10);
        Function<Integer, Integer> mapper = x -> x * 2;

        assertThrows(NullPointerException.class, () ->
                pair.map(null, mapper)
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when second mapper is null")
    void testMapThrowsExceptionForNullSecondMapper() {
        Pair<Integer> pair = new Pair<>(5, 10);
        Function<Integer, Integer> mapper = x -> x * 2;

        assertThrows(NullPointerException.class, () ->
                pair.map(mapper, null)
        );
    }

    @Test
    @DisplayName("Should reduce with pair combiner")
    void testReduceWithPairCombiner() {
        Pair<Integer> pair = new Pair<>(5, 10);
        Function<Pair<? extends Integer>, Integer> sumCombiner =
                p -> p.getFst() + p.getSnd();

        Integer result = pair.reduce(sumCombiner);

        assertEquals(15, result);
    }

    @Test
    @DisplayName("Should reduce with BiFunction combiner")
    void testReduceWithBiFunctionCombiner() {
        Pair<Integer> pair = new Pair<>(5, 10);
        BiFunction<Integer, Integer, Integer> sumCombiner = Integer::sum;

        Integer result = pair.reduce(sumCombiner);

        assertEquals(15, result);
    }

    @Test
    @DisplayName("Should reduce to different type")
    void testReduceChangesType() {
        Pair<Integer> pair = new Pair<>(5, 10);
        BiFunction<Integer, Integer, String> concatenate =
                (a, b) -> a + " and " + b;

        String result = pair.reduce(concatenate);

        assertEquals("5 and 10", result);
    }

    @Test
    @DisplayName("Should throw NullPointerException when pair combiner is null")
    void testReduceThrowsExceptionForNullPairCombiner() {
        Pair<Integer> pair = new Pair<>(5, 10);

        assertThrows(NullPointerException.class, () ->
                pair.reduce((Function<Pair<? extends Integer>, Integer>) null)
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when BiFunction combiner is null")
    void testReduceThrowsExceptionForNullBiFunctionCombiner() {
        Pair<Integer> pair = new Pair<>(5, 10);

        assertThrows(NullPointerException.class, () ->
                pair.reduce((BiFunction<Integer, Integer, Integer>) null)
        );
    }

    @Test
    @DisplayName("Should iterate over both elements")
    void testIterator() {
        Pair<String> pair = new Pair<>("first", "second");
        Iterator<String> iterator = pair.iterator();

        assertTrue(iterator.hasNext());
        assertEquals("first", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("second", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when iterating past end")
    void testIteratorThrowsExceptionWhenExhausted() {
        Pair<String> pair = new Pair<>("first", "second");
        Iterator<String> iterator = pair.iterator();

        iterator.next();
        iterator.next();

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    @DisplayName("Should support multiple independent iterators")
    void testMultipleIterators() {
        Pair<Integer> pair = new Pair<>(1, 2);
        Iterator<Integer> it1 = pair.iterator();
        Iterator<Integer> it2 = pair.iterator();

        assertEquals(1, it1.next());
        assertEquals(1, it2.next());
        assertEquals(2, it1.next());
        assertEquals(2, it2.next());
    }

    @Test
    @DisplayName("Should use iterator in for-each loop")
    void testIteratorInForEachLoop() {
        Pair<String> pair = new Pair<>("alpha", "beta");
        StringBuilder result = new StringBuilder();

        for (String element : pair) {
            result.append(element).append(" ");
        }

        assertEquals("alpha beta ", result.toString());
    }

    @Test
    @DisplayName("Should always return size 2")
    void testSize() {
        Pair<String> pair = new Pair<>("first", "second");
        assertEquals(2, pair.size());
    }

    @Test
    @DisplayName("Should return size 2 for different types")
    void testSizeForDifferentTypes() {
        Pair<Integer> intPair = new Pair<>(1, 2);
        Pair<Double> doublePair = new Pair<>(1.0, 2.0);

        assertEquals(2, intPair.size());
        assertEquals(2, doublePair.size());
    }

    @Test
    @DisplayName("Should be equal to itself")
    void testEqualsReflexive() {
        Pair<String> pair = new Pair<>("first", "second");
        assertEquals(pair, pair);
    }

    @Test
    @DisplayName("Should be equal to pair with same elements")
    void testEqualsSameElements() {
        Pair<String> pair1 = new Pair<>("first", "second");
        Pair<String> pair2 = new Pair<>("first", "second");

        assertEquals(pair1, pair2);
        assertEquals(pair2, pair1);
    }

    @Test
    @DisplayName("Should not be equal to pair with different first element")
    void testNotEqualsDifferentFirst() {
        Pair<String> pair1 = new Pair<>("first", "second");
        Pair<String> pair2 = new Pair<>("other", "second");

        assertNotEquals(pair1, pair2);
    }

    @Test
    @DisplayName("Should not be equal to pair with different second element")
    void testNotEqualsDifferentSecond() {
        Pair<String> pair1 = new Pair<>("first", "second");
        Pair<String> pair2 = new Pair<>("first", "other");

        assertNotEquals(pair1, pair2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void testNotEqualsNull() {
        Pair<String> pair = new Pair<>("first", "second");
        assertNotEquals(pair, null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void testNotEqualsDifferentClass() {
        Pair<String> pair = new Pair<>("first", "second");
        String notAPair = "not a pair";

        assertNotEquals(pair, notAPair);
    }

    @Test
    @DisplayName("Should have same hashCode for equal pairs")
    void testHashCodeConsistency() {
        Pair<String> pair1 = new Pair<>("first", "second");
        Pair<String> pair2 = new Pair<>("first", "second");

        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    @DisplayName("Should have different hashCode for different pairs")
    void testHashCodeDifferent() {
        Pair<String> pair1 = new Pair<>("first", "second");
        Pair<String> pair2 = new Pair<>("other", "different");

        // Note: Different objects can have same hashCode, but likely different
        assertNotEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    @DisplayName("Should produce readable string representation")
    void testToString() {
        Pair<String> pair = new Pair<>("first", "second");
        String result = pair.toString();

        assertTrue(result.contains("Pair"));
        assertTrue(result.contains("first"));
        assertTrue(result.contains("second"));
    }

    @Test
    @DisplayName("Should produce correct format in toString")
    void testToStringFormat() {
        Pair<Integer> pair = new Pair<>(10, 20);
        String result = pair.toString();

        assertEquals("Pair(10, 20)", result);
    }

    @Test
    @DisplayName("Should handle different types in toString")
    void testToStringDifferentTypes() {
        Pair<Double> pair = new Pair<>(3.14, 2.71);
        String result = pair.toString();

        assertEquals("Pair(3.14, 2.71)", result);
    }

    @Test
    @DisplayName("Should contain first element")
    void testContainsFirstElement() {
        Pair<String> pair = new Pair<>("first", "second");
        assertTrue(pair.contains("first"));
    }

    @Test
    @DisplayName("Should contain second element")
    void testContainsSecondElement() {
        Pair<String> pair = new Pair<>("first", "second");
        assertTrue(pair.contains("second"));
    }

    @Test
    @DisplayName("Should not contain element not in pair")
    void testDoesNotContainOtherElement() {
        Pair<String> pair = new Pair<>("first", "second");
        assertFalse(pair.contains("other"));
    }

    @Test
    @DisplayName("Should not contain null")
    void testDoesNotContainNull() {
        Pair<String> pair = new Pair<>("first", "second");
        assertFalse(pair.contains(null));
    }
}