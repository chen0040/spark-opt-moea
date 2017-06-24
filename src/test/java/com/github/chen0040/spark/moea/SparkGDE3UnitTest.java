package com.github.chen0040.spark.moea;


import com.github.chen0040.moea.algorithms.GDE3;
import com.github.chen0040.moea.components.NondominatedPopulation;
import com.github.chen0040.moea.problems.NDND;
import com.github.chen0040.sparkml.commons.SparkContextFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.testng.annotations.Test;


/**
 * Created by xschen on 19/6/2017.
 */
public class SparkGDE3UnitTest {

   @Test
   public void test_ndnd(){

      JavaSparkContext context = SparkContextFactory.createSparkContext("testing-1");
      SparkGDE3 algorithm = new SparkGDE3();
      algorithm.read(new NDND());
      algorithm.setPopulationSize(100);
      algorithm.setMaxGenerations(50);
      algorithm.setDisplayEvery(10);

      NondominatedPopulation pareto_front = algorithm.solve(context);
   }
}
