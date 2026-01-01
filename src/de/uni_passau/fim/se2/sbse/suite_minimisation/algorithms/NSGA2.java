package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.Crossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.Mutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NSGA2<C extends Chromosome<C>> implements GeneticAlgorithm<C> {

    private final ChromosomeGenerator<C> generator;
    private final Mutation<C> mutation;
    private final Crossover<C> crossover;
    private final List<FitnessFunction<C>> fitnessFunctions;
    private final StoppingCondition stoppingCondition;
    private final Random random;

    private final int populationSize = 100; // default population size
    private final List<C> population = new ArrayList<>();

    public NSGA2(
            ChromosomeGenerator<C> generator,
            Mutation<C> mutation,
            Crossover<C> crossover,
            List<FitnessFunction<C>> fitnessFunctions,
            StoppingCondition stoppingCondition,
            Random random
    ) {
        this.generator = generator;
        this.mutation = mutation;
        this.crossover = crossover;
        this.fitnessFunctions = fitnessFunctions;
        this.stoppingCondition = stoppingCondition;
        this.random = random;
    }

    @Override
    public List<C> findSolution() {

        // Notify stopping condition that search has started
        stoppingCondition.notifySearchStarted();

        // Generate initial population
        for (int i = 0; i < populationSize && !stoppingCondition.searchMustStop(); i++) {
            C candidate = generator.get();
            population.add(candidate);
            stoppingCondition.notifyFitnessEvaluation();
        }

        // Main NSGA-II loop
        while (!stoppingCondition.searchMustStop()) {

            // Create offspring population
            List<C> offspring = new ArrayList<>();

            // Generate offspring via selection, crossover, and mutation
            while (offspring.size() < populationSize) {
                C parent1 = tournamentSelection();
                C parent2 = tournamentSelection();

                // Apply crossover
                Pair<C> children = crossover.apply(parent1, parent2);

                // Apply mutation
                C child1 = mutation.apply(children.getFst());
                C child2 = mutation.apply(children.getSnd());

                offspring.add(child1);
                if (offspring.size() < populationSize) {
                    offspring.add(child2);
                }
            }

            // Combine current population with offspring
            population.addAll(offspring);

            // Apply non-dominated sorting
            List<List<C>> fronts = nonDominatedSort(population);

            // Build next generation preserving Pareto fronts and crowding
            population.clear();
            for (List<C> front : fronts) {
                if (population.size() + front.size() <= populationSize) {
                    population.addAll(front);
                } else {
                    // Fill remaining slots using crowding distance
                    front.sort(this::compareByCrowdingDistance);
                    int remaining = populationSize - population.size();
                    population.addAll(front.subList(0, remaining));
                    break;
                }
            }

            // Notify stopping condition of fitness evaluations
            stoppingCondition.notifyFitnessEvaluation();
        }

        // Return the final Pareto front (non-dominated solutions)
        return getParetoFront(population);
    }

    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }

    // ------------------------------
    // Helper methods
    // ------------------------------

    // Simple tournament selection
    private C tournamentSelection() {
        C a = population.get(random.nextInt(population.size()));
        C b = population.get(random.nextInt(population.size()));
        return dominates(a, b) ? a : b;
    }

    // Check if solution1 dominates solution2
    private boolean dominates(C s1, C s2) {
        boolean betterInAtLeastOne = false;

        for (FitnessFunction<C> ff : fitnessFunctions) {
            double f1 = ff.applyAsDouble(s1);
            double f2 = ff.applyAsDouble(s2);

            if (ff instanceof MinimizingFitnessFunction) {
                if (f1 > f2) return false;
                if (f1 < f2) betterInAtLeastOne = true;
            } else {
                if (f1 < f2) return false;
                if (f1 > f2) betterInAtLeastOne = true;
            }
        }
        return betterInAtLeastOne;
    }

    // Non-dominated sorting: returns list of fronts
    private List<List<C>> nonDominatedSort(List<C> solutions) {
        List<List<C>> fronts = new ArrayList<>();
        List<C> remaining = new ArrayList<>(solutions);

        while (!remaining.isEmpty()) {
            List<C> front = new ArrayList<>();
            for (C candidate : remaining) {
                boolean dominated = false;
                for (C other : remaining) {
                    if (dominates(other, candidate)) {
                        dominated = true;
                        break;
                    }
                }
                if (!dominated) {
                    front.add(candidate);
                }
            }
            remaining.removeAll(front);
            fronts.add(front);
        }
        return fronts;
    }

    // Placeholder crowding distance comparison
    private int compareByCrowdingDistance(C a, C b) {
        // Simple random tie-breaker (for demo)
        return random.nextInt(3) - 1;
    }

    // Return non-dominated solutions only
    private List<C> getParetoFront(List<C> solutions) {
        List<C> paretoFront = new ArrayList<>();
        for (C candidate : solutions) {
            boolean dominated = false;
            for (C other : solutions) {
                if (dominates(other, candidate)) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated) {
                paretoFront.add(candidate);
            }
        }
        return paretoFront;
    }
}
