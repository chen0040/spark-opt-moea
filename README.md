# spark-opt-moea

Spark implementation of Multi-Objective Evolutionary Computation Framework for Distributed Computing Numerical Optimization

# Features

The distributed optimization is performed so that computationally intensive optimization cost evaluation can be distributed in a computing cluster via Spark.

The following Multi-Objective EA are supported:

* NSGA-II
* GDE-3


# Install

Add the follow dependency to your POM file:

```xml
<dependency>
  <groupId>com.github.chen0040</groupId>
  <artifactId>spark-opt-moea</artifactId>
  <version>1.0.1</version>
</dependency>
```

# Usage

### NSGA-II for solving NDND 2-Objective Problem

The following sample code shows how to use NSGA-II to solve the NDND 2-objective optimization problem:

```java
SparkNSGAII algorithm = new SparkNSGAII();
algorithm.setCostFunction((CostFunction) (x, objective_index, lowerBounds, upperBounds) -> {
 double f1 = 1 - Math.exp((-4) * x.get(0)) * Math.pow(Math.sin(5 * Math.PI * x.get(0)), 4);
 if (objective_index == 0)
 {
    // objective 0
    return f1;
 }
 else
 {
    // objective 1
    double f2, g, h;
    if (x.get(1) > 0 && x.get(1) < 0.4)
       g = 4 - 3 * Math.exp(-2500 * (x.get(1) - 0.2) * (x.get(1) - 0.2));
    else
       g = 4 - 3 * Math.exp(-25 * (x.get(1) - 0.7) * (x.get(1) - 0.7));
    double a = 4;
    if (f1 < g)
       h = 1 - Math.pow(f1 / g, a);
    else
       h = 0;
    f2 = g * h;
    return f2;
 }
});
algorithm.setDimension(2);
algorithm.setObjectiveCount(2);
algorithm.setLowerBounds(Arrays.asList(0.0, 0.0));
algorithm.setUpperBounds(Arrays.asList(1.0, 1.0));

algorithm.setPopulationSize(1000);
algorithm.setMaxGenerations(100);
algorithm.setDisplayEvery(10);

JavaSparkContext context = SparkContextFactory.createSparkContext("testing-1");
NondominatedPopulation pareto_front = algorithm.solve(context);
```

'pareto_front' is a set of solutions that represents that best solutions found by the algorithm (i.e. the pareto front).

To access individual solution in the pareto front:

```java
for(int i=0; i < pareto_front.size(); ++i) {
   Solution solution = pareto_front.get(i);
}

```

To visualize the pareto front:
 
```java
List<TupleTwo<Double, Double>> pareto_front_data = pareto_front.front2D();
ParetoFront chart = new ParetoFront(pareto_front_data, "Pareto Front");
chart.showIt(true);
```

### GDE-3 for solving NDND 2-Objective Problem

The following sample code shows how to use GDE-3 to solve the NDND 2-objective optimization problem:

```java
SparkGDE3 algorithm = new SparkGDE3();
algorithm.setCostFunction((CostFunction) (x, objective_index, lowerBounds, upperBounds) -> {
 double f1 = 1 - Math.exp((-4) * x.get(0)) * Math.pow(Math.sin(5 * Math.PI * x.get(0)), 4);
 if (objective_index == 0)
 {
    // objective 0
    return f1;
 }
 else
 {
    // objective 1
    double f2, g, h;
    if (x.get(1) > 0 && x.get(1) < 0.4)
       g = 4 - 3 * Math.exp(-2500 * (x.get(1) - 0.2) * (x.get(1) - 0.2));
    else
       g = 4 - 3 * Math.exp(-25 * (x.get(1) - 0.7) * (x.get(1) - 0.7));
    double a = 4;
    if (f1 < g)
       h = 1 - Math.pow(f1 / g, a);
    else
       h = 0;
    f2 = g * h;
    return f2;
 }
});
algorithm.setDimension(2);
algorithm.setObjectiveCount(2);
algorithm.setLowerBounds(Arrays.asList(0.0, 0.0));
algorithm.setUpperBounds(Arrays.asList(1.0, 1.0));

algorithm.setPopulationSize(100);
algorithm.setMaxGenerations(50);
algorithm.setDisplayEvery(10);

JavaSparkContext context = SparkContextFactory.createSparkContext("testing-1");
NondominatedPopulation pareto_front = algorithm.solve(context);

List<TupleTwo<Double, Double>> pareto_front_data = pareto_front.front2D();

ParetoFront chart = new ParetoFront(pareto_front_data, "Pareto Front for NDND");
chart.showIt(true);
```

