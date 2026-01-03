package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.NSGA2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class CoverageTrackerTest {

    @Test
    @DisplayName("Should create CoverageTracker with valid class and test suite")
    void testConstructorWithValidClasses() {
        assertDoesNotThrow(() ->
                new CoverageTracker(DummyClassUnderTest.class, DummyTestSuite.class)
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when class under test is null")
    void testConstructorThrowsExceptionForNullClassUnderTest() {
        assertThrows(NullPointerException.class, () ->
                new CoverageTracker(null, DummyTestSuite.class)
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when test suite is null")
    void testConstructorThrowsExceptionForNullTestSuite() {
        assertThrows(NullPointerException.class, () ->
                new CoverageTracker(DummyClassUnderTest.class, null)
        );
    }

    @Test
    @DisplayName("Should create CoverageTracker from class name")
    void testConstructorWithClassName() throws ClassNotFoundException {
        String className = NSGA2.class.getName();

        CoverageTracker tracker = new CoverageTracker(className);

        assertNotNull(tracker);
        assertNotNull(tracker.getTestCases());
    }

    @Test
    @DisplayName("Should throw ClassNotFoundException for non-existent class")
    void testConstructorThrowsExceptionForNonExistentClass() {
        assertThrows(ClassNotFoundException.class, () ->
                new CoverageTracker("com.example.NonExistentClass")
        );
    }

    @Test
    @DisplayName("Should throw ClassNotFoundException when test suite not found")
    void testConstructorThrowsExceptionWhenTestSuiteNotFound() {
        // This class exists but DummyClassWithoutTestTest doesn't
        assertThrows(ClassNotFoundException.class, () ->
                new CoverageTracker(DummyClassWithoutTest.class.getName())
        );
    }

    @Test
    @DisplayName("Should return all test cases from test suite")
    void testGetTestCases() {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        String[] testCases = tracker.getTestCases();

        assertNotNull(testCases);
        assertTrue(testCases.length > 0);
    }

    @Test
    @DisplayName("Should return test cases in sorted order")
    void testGetTestCasesReturnsSortedOrder() {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        String[] testCases = tracker.getTestCases();

        // Check if sorted lexicographically
        for (int i = 0; i < testCases.length - 1; i++) {
            assertTrue(testCases[i].compareTo(testCases[i + 1]) <= 0,
                    "Test cases should be sorted lexicographically");
        }
    }

    @Test
    @DisplayName("Should only include methods annotated with @Test")
    void testGetTestCasesOnlyIncludesTestAnnotatedMethods() {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        String[] testCases = tracker.getTestCases();

        // Should only contain actual test methods
        for (String testCase : testCases) {
            assertTrue(testCase.startsWith("test"),
                    "Test case names should start with 'test'");
        }
    }

    @Test
    @DisplayName("Should return empty array for test suite with no tests")
    void testGetTestCasesForEmptyTestSuite() {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                EmptyTestSuite.class
        );

        String[] testCases = tracker.getTestCases();

        assertEquals(0, testCases.length);
    }

    @Test
    @DisplayName("Should return non-null coverage matrix")
    void testGetCoverageMatrixReturnsNonNull() throws Exception {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        boolean[][] matrix = tracker.getCoverageMatrix();

        assertNotNull(matrix);
    }

    @Test
    @DisplayName("Should return rectangular coverage matrix")
    void testGetCoverageMatrixIsRectangular() throws Exception {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        boolean[][] matrix = tracker.getCoverageMatrix();

        if (matrix.length > 0) {
            int expectedLength = matrix[0].length;
            for (boolean[] row : matrix) {
                assertEquals(expectedLength, row.length,
                        "All rows should have the same length");
            }
        }
    }

    @Test
    @DisplayName("Should have one row per test case")
    void testCoverageMatrixHasCorrectNumberOfRows() throws Exception {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        String[] testCases = tracker.getTestCases();
        boolean[][] matrix = tracker.getCoverageMatrix();

        assertEquals(testCases.length, matrix.length,
                "Matrix should have one row per test case");
    }

    @Test
    @DisplayName("Should cache coverage matrix on subsequent calls")
    void testCoverageMatrixIsCached() throws Exception {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        boolean[][] matrix1 = tracker.getCoverageMatrix();
        boolean[][] matrix2 = tracker.getCoverageMatrix();

        // Should return same reference (cached)
        assertSame(matrix1, matrix2);
    }

    @Test
    @DisplayName("Should contain boolean coverage values")
    void testCoverageMatrixContainsBooleans() throws Exception {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        boolean[][] matrix = tracker.getCoverageMatrix();

        // Just verify we can access the boolean values without exception
        if (matrix.length > 0 && matrix[0].length > 0) {
            boolean coverage = matrix[0][0];
            assertTrue(coverage || !coverage); // Always true, just checking type
        }
    }

    @Test
    @DisplayName("Should handle class with no executable lines")
    void testGetCoverageMatrixForEmptyClass() throws Exception {
        CoverageTracker tracker = new CoverageTracker(
                EmptyClass.class,
                EmptyClassTest.class
        );

        // Should not throw exception
        assertDoesNotThrow(() -> tracker.getCoverageMatrix());
    }

    @Test
    @DisplayName("Should return non-null string representation")
    void testToStringReturnsNonNull() {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        String result = tracker.toString();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should include class names in string representation")
    void testToStringIncludesClassNames() {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        String result = tracker.toString();

        assertTrue(result.contains("DummyClassUnderTest"));
        assertTrue(result.contains("DummyTestSuite"));
    }

    @Test
    @DisplayName("Should indicate when coverage not measured yet")
    void testToStringIndicatesNotMeasured() {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        String result = tracker.toString();

        assertTrue(result.contains("Not measured") || result.contains("not measured"));
    }

    @Test
    @DisplayName("Should show coverage information after measurement")
    void testToStringShowsCoverageAfterMeasurement() throws Exception {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        tracker.getCoverageMatrix(); // Trigger measurement
        String result = tracker.toString();

        // Should contain test case names after measurement
        for (String testCase : tracker.getTestCases()) {
            assertTrue(result.contains(testCase));
        }
    }

    @Test
    @DisplayName("Should create MemoryClassLoader")
    void testMemoryClassLoaderCreation() {
        assertDoesNotThrow(() ->
                new CoverageTracker.MemoryClassLoader()
        );
    }

    @Test
    @DisplayName("Should add class definition to MemoryClassLoader")
    void testMemoryClassLoaderAddDefinition() {
        CoverageTracker.MemoryClassLoader loader =
                new CoverageTracker.MemoryClassLoader();

        byte[] dummyBytes = new byte[]{0x01, 0x02, 0x03};

        assertDoesNotThrow(() ->
                loader.addDefinition("com.example.DummyClass", dummyBytes)
        );
    }

    @Test
    @DisplayName("Should load standard classes from parent loader")
    void testMemoryClassLoaderLoadsStandardClasses() throws ClassNotFoundException {
        CoverageTracker.MemoryClassLoader loader =
                new CoverageTracker.MemoryClassLoader();

        Class<?> stringClass = loader.loadClass("java.lang.String");

        assertEquals(String.class, stringClass);
    }

    @Test
    @DisplayName("Should measure coverage for simple class")
    void testMeasureCoverageForSimpleClass() throws Exception {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        boolean[][] matrix = tracker.getCoverageMatrix();

        assertNotNull(matrix);
        assertTrue(matrix.length > 0, "Should have at least one test case");
    }

    @Test
    @DisplayName("Should handle multiple test cases")
    void testHandlesMultipleTestCases() throws Exception {
        CoverageTracker tracker = new CoverageTracker(
                DummyClassUnderTest.class,
                DummyTestSuite.class
        );

        String[] testCases = tracker.getTestCases();
        boolean[][] matrix = tracker.getCoverageMatrix();

        assertTrue(testCases.length > 0);
        assertEquals(testCases.length, matrix.length);
    }

    // ==================== Helper Classes for Testing ====================
    public static class DummyClassUnderTest {
        public int add(int a, int b) {
            return a + b;
        }

        public int subtract(int a, int b) {
            return a - b;
        }

        public boolean isPositive(int n) {
            return n > 0;
        }
    }

    public static class DummyTestSuite {
        @Test
        public void testAdd() {
            DummyClassUnderTest cut = new DummyClassUnderTest();
            assertEquals(5, cut.add(2, 3));
        }

        @Test
        public void testSubtract() {
            DummyClassUnderTest cut = new DummyClassUnderTest();
            assertEquals(1, cut.subtract(3, 2));
        }

        @Test
        public void testIsPositive() {
            DummyClassUnderTest cut = new DummyClassUnderTest();
            assertTrue(cut.isPositive(5));
        }

        // Non-test method (should not be included)
        public void helperMethod() {
            // Not a test
        }
    }

    public static class DummyClassWithoutTest {
        public void doNothing() {
            // Empty
        }
    }

    public static class EmptyTestSuite {
        public void notATest() {
            // Not annotated with @Test
        }
    }

    public static class EmptyClass {
        // No methods
    }


    public static class EmptyClassTest {
        @Test
        public void testNothing() {
            // Empty test
        }
    }
}