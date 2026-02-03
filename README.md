# Test Suite Minimization

## Overview

This assignment implements multi-objective optimization algorithms (Random Search and NSGA-II) to solve the test suite minimization problem in regression testing. The goal is to minimize the number of test cases while maximizing code coverage.

## Problem Description

Regression testing verifies that software changes don't affect unchanged parts of the system. The retest-all approach is straightforward but expensive. Test suite minimization aims to reduce testing costs while maintaining high coverage, creating a multi-objective optimization problem with two competing objectives:

1. **Minimize test suite size** - Reduces execution costs but may reduce coverage
2. **Maximize code coverage** - Often requires more test cases, increasing costs

This assignment uses NSGA-II (Non-dominated Sorting Genetic Algorithm II), a prominent multi-objective GA, which finds a set of optimal solutions rather than a single optimum.

## Multi-Objective Optimization: Pareto Dominance

Given a fitness function **f: T → ℝⁿ** where we minimize all components, solution **t** dominates solution **u** (t ≺ u) if:
```
t ≺ u ⟺ (∀i: 1≤i≤n: fᵢ(t) ≤ fᵢ(u)) ∧ (∃j: 1≤j≤n: fⱼ(t) < fⱼ(u))
```

**Intuition:** Solution **t** dominates **u** if it's at least as good in all objectives AND strictly better in at least one objective.

**Pareto Fronts:** The population is partitioned into non-domination fronts:
- **First Pareto Front:** Optimal solutions - no other solution dominates them
- **Second Pareto Front:** Solutions dominated only by first front members
- Each front's members are incomparable to each other

## Implementation Requirements

### 1. Chromosomes

Represent test suites (or subsets) containing at least one test case.

**Tasks:**
- Extend the abstract class `Chromosome`
- Implement `ChromosomeGenerator` interface to generate random chromosomes

### 2. Search Operators

Design operators working on your chromosome representation.

**Mutation:** Implement `Mutation` interface
- Alters one parent chromosome to introduce new traits

**Crossover:** Implement `Crossover` interface
- Recombines genetic material from two parent chromosomes

*Note: Design is flexible - operators should suit your encoding and be creative.*

### 3. Selection

Implement `BinaryTournamentSelection.apply()` method using binary tournament selection without replacement (tournament size = 2).

### 4. Fitness Functions

Implement two **normalized** fitness functions returning values in [0, 1]:

1. **Test Suite Size** - Minimize (fraction of selected tests)
2. **Test Suite Coverage** - Maximize (fraction of covered lines)

**Implementation:** In `AlgorithmBuilder` class:
- `makeTestSuiteSizeFitnessFunction()`
- `makeTestSuiteCoverageFitnessFunction()`

*Important: Use fractions, not absolute values (e.g., 0.75 coverage, not 75 lines).*

### 5. Algorithms

Implement using the `GeneticAlgorithm` interface with `findSolution()` method returning the Pareto Front.

#### NSGA-II
- Use chromosome representation, search operators, selection, and fitness functions
- Processes populations per generation
- Returns first Pareto front after search

#### Random Search
- Samples one solution per iteration (no populations/generations)
- Returns Pareto front of all non-dominated solutions sampled
- Enables fair comparison with NSGA-II

**Both algorithms must:**
- Adhere to `MaxFitnessEvaluations` stopping condition
- Be instantiated via `AlgorithmBuilder.buildNSGA2()` and `AlgorithmBuilder.buildRandomSearch()`

### 6. Utility Functions

Implement the following in their respective classes:

**In `Main` class:**
- `getTestCaseNamesFrom()` - Extract test case names from chromosome
- `getCoverageOf()` - Extract normalized coverage from chromosome
- `getSizeOf()` - Compute normalized test suite size from chromosome

**In `Utils` class:**
- `computeHyperVolume()` - Compute hypervolume of Pareto front with respect to objectives and reference points

## Usage

Build and run:
```bash
mvn package
java -jar target/Test-Suite-Minimisation.jar -c Lift NSGA2
```

**Test Classes:** AddNumbers, Lift, Rational, Complex

**Coverage Matrices:** Use `Utils.parseCoverageMatrix()` to parse boolean[][] arrays where `matrix[i][j]` indicates if test i covers line j.

## Development Notes

- Use `Randomness` class for random numbers (not `java.util.Random`)
- Test files must end with `Test` suffix
- Tests involve 1000 fitness evaluations and 10 experiment repetitions
- Local testing recommended before pushing to Artemis
- Results may take ~10 minutes to appear on Artemis
- Pipeline timeout: 10 minutes

## Test Requirements

### Functional Tests (80% of points)
- ✅ Binary tournament selection (3/3)
- ✅ Hypervolume calculation (4/4)
- ⚠️ Random Search results (1/4)
- ⚠️ NSGA-II results (3/4)

### Test Suite (20% of points)
- ✅ Line Coverage ≥ 70%
- ✅ Branch Coverage ≥ 60%
- ✅ Mutation Score ≥ 60%

## Key Concepts

**Pareto Front:** Set of non-dominated solutions representing optimal trade-offs between objectives

**Dominance:** A solution is better if it improves at least one objective without worsening any other

**Hypervolume:** Metric measuring the volume of objective space dominated by a Pareto front (quality indicator)

**NSGA-II Features:**
- Fast non-dominated sorting
- Crowding distance for diversity
- Elite preservation
- Multi-objective optimization without weight selection
