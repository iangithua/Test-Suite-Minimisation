package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.BinaryChromosom;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.Crossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.Mutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;

import java.util.*;

public class NSGA2<C extends Chromosome<C>> implements GeneticAlgorithm<C> {


    private final ChromosomeGenerator<C> generator;
    private final Mutation<C> mutation;
    private final Crossover<C> crossover;
    private final List<FitnessFunction<C>> fitnessFunctions;
    private final StoppingCondition stoppingCondition;
    private final Random random;

    private final int populationSize = 100;
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

        stoppingCondition.notifySearchStarted();

        // Generate initial population with guaranteed coverage
        for (int i = 0; i < populationSize && !stoppingCondition.searchMustStop(); i++) {
            C candidate = generator.get();
            if (!hasCoverage(candidate)) candidate = forceCoverage(candidate);
            population.add(candidate);
            stoppingCondition.notifyFitnessEvaluation();
        }

        // Main NSGA-II loop
        while (!stoppingCondition.searchMustStop()) {

            List<C> offspring = new ArrayList<>();

            while (offspring.size() < populationSize) {
                C parent1 = tournamentSelection();
                C parent2 = tournamentSelection();

                // Crossover
                Pair<C> children = crossover.apply(parent1, parent2);

                // Mutation
                C child1 = mutation.apply(children.getFst());
                C child2 = mutation.apply(children.getSnd());

                if (!hasCoverage(child1)) child1 = forceCoverage(child1);
                if (!hasCoverage(child2)) child2 = forceCoverage(child2);

                offspring.add(child1);
                if (offspring.size() < populationSize) offspring.add(child2);
            }

            // Combine population and offspring
            population.addAll(offspring);

            // Non-dominated sorting
            List<List<C>> fronts = nonDominatedSort(population);

            // Compute crowding distances
            for (List<C> front : fronts) computeCrowdingDistance(front);

            // Fill next generation preserving Pareto fronts and crowding
            population.clear();
            for (List<C> front : fronts) {
                if (population.size() + front.size() <= populationSize) {
                    population.addAll(front);
                } else {
                    front.sort(Comparator.comparingDouble(f -> -f.getCrowdingDistance()));
                    int remaining = populationSize - population.size();
                    population.addAll(front.subList(0, remaining));
                    break;
                }
            }

            stoppingCondition.notifyFitnessEvaluation();
        }

        // Return non-dominated solutions
        return getParetoFront(population);
    }

    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }

    private boolean hasCoverage(C candidate) {
        double coverage = fitnessFunctions.get(1).applyAsDouble(candidate);
        return coverage > 0;
    }

    private C forceCoverage(C candidate) {
        // For binary chromosomes, flip a random gene to true
        if (candidate instanceof BinaryChromosom binary) {
            boolean[] genes = binary.getGenes();
            genes[random.nextInt(genes.length)] = true;
            return (C) new BinaryChromosom(genes, binary.getMutation(), binary.getCrossover());
        }
        return candidate;
    }

    private C tournamentSelection() {
        C a = population.get(random.nextInt(population.size()));
        C b = population.get(random.nextInt(population.size()));
        return dominates(a, b) ? a : b;
    }

    private boolean dominates(C s1, C s2) {
        boolean betterInAtLeastOne = false;
        for (FitnessFunction<C> ff : fitnessFunctions) {
            double f1 = ff.applyAsDouble(s1);
            double f2 = ff.applyAsDouble(s2);

            if (ff.isMinimizing()) {
                if (f1 > f2) return false;
                if (f1 < f2) betterInAtLeastOne = true;
            } else {
                if (f1 < f2) return false;
                if (f1 > f2) betterInAtLeastOne = true;
            }
        }
        return betterInAtLeastOne;
    }

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
                if (!dominated) front.add(candidate);
            }
            remaining.removeAll(front);
            fronts.add(front);
        }
        return fronts;
    }

    private void computeCrowdingDistance(List<C> front) {
        int n = front.size();
        if (n == 0) return;

        double[] distance = new double[n];
        Arrays.fill(distance, 0);

        int numObjectives = fitnessFunctions.size();
        for (int m = 0; m < numObjectives; m++) {
            FitnessFunction<C> ff = fitnessFunctions.get(m);
            final int idx = m;
            front.sort(Comparator.comparingDouble(ff::applyAsDouble));
            distance[0] = distance[n - 1] = Double.POSITIVE_INFINITY;

            double fMin = ff.applyAsDouble(front.get(0));
            double fMax = ff.applyAsDouble(front.get(n - 1));
            if (fMax - fMin == 0) continue;

            for (int i = 1; i < n - 1; i++) {
                double fNext = ff.applyAsDouble(front.get(i + 1));
                double fPrev = ff.applyAsDouble(front.get(i - 1));
                distance[i] += (fNext - fPrev) / (fMax - fMin);
            }
        }

        // Store crowding distance in chromosome
        for (int i = 0; i < n; i++) {
            front.get(i).setCrowdingDistance(distance[i]);
        }
    }

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
            if (!dominated) paretoFront.add(candidate);
        }
        return paretoFront;
    }
}
