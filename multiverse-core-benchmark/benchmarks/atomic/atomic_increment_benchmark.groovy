import org.benchy.Benchmark
import org.benchy.GroovyTestCase

import org.multiverse.stms.beta.benchmarks.AtomicIncrementDriver

def benchmark = new Benchmark();
benchmark.name = "atomic_increment"

for (def k in 1..processorCount) {
  def testCase = new GroovyTestCase()
  testCase.name = "atomic_increment_and_no_shared_ref"
  testCase.warmupRunIterationCount = k == 1 ? 1 : 0;
  testCase.threadCount = k
  testCase.transactionsPerThread = 1000 * 1000 * 1000L
  testCase.sharedRef = false
  testCase.driver = AtomicIncrementDriver.class
  benchmark.add(testCase)
}

for (def k in 1..processorCount) {
  def testCase = new GroovyTestCase()
  testCase.name = "atomic_increment_and_shared_ref"
  testCase.warmupRunIterationCount = k == 1 ? 1 : 0;
  testCase.threadCount = k
  testCase.sharedRef = true
  testCase.transactionsPerThread = k == 0 ? 1000 * 1000 * 100L : 1000 * 1000 * 10L
  testCase.driver = AtomicIncrementDriver.class
  benchmark.add(testCase)
}

benchmark
