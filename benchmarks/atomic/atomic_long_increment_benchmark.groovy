import org.benchy.Benchmark
import org.benchy.GroovyTestCase

import org.multiverse.stms.beta.benchmarks.AtomicLongIncrementDriver

def benchmark = new Benchmark();
benchmark.name = "atomic_long_increment"

for (def k in 1..processorCount) {
  def testCase = new GroovyTestCase()
  testCase.name = "atomic_long_increment_no_shared_ref"
  testCase.warmupRunIterationCount = k == 1 ? 1 : 0;
  testCase.threadCount = k
  testCase.sharedRef = false
  testCase.transactionsPerThread = 1000 * 1000 * 1000L
  testCase.driver = AtomicLongIncrementDriver.class
  benchmark.add(testCase)
}

for (def k in 1..processorCount) {
  def testCase = new GroovyTestCase()
  testCase.name = "atomic_long_increment_shared_ref"
  testCase.warmupRunIterationCount = k == 1 ? 1 : 0;
  testCase.threadCount = k
  testCase.sharedRef = true
  testCase.transactionsPerThread = k == 0 ? 1000 * 1000 * 1000L : 100 * 1000 * 1000L
  testCase.driver = AtomicLongIncrementDriver.class
  benchmark.add(testCase)
}

benchmark
