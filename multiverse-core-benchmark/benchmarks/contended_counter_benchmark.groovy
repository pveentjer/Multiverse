import org.benchy.Benchmark
import org.benchy.GroovyTestCase
import org.multiverse.stms.beta.benchmarks.ContendedCounterDriver

def benchmark = new Benchmark();
benchmark.name = "counter"

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "counter_with_${k}_threads"
    testCase.dirtyCheck = true
    testCase.threadCount = k
    if (k > 2) {
        testCase.transactionsPerThread = 1000 * 1000 * 5
    } else {
        testCase.transactionsPerThread = 1000 * 1000 * 20
    }
    testCase.warmupRunIterationCount = k == 1 ? 1 : 0;
    testCase.driver = ContendedCounterDriver.class
    benchmark.add(testCase)
}

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "counter_no_dirtyCheck_with_${k}_threads"
    testCase.threadCount = k
    testCase.dirtyCheck = false
    if (k > 2) {
        testCase.transactionsPerThread = 1000 * 1000 * 5
    } else {
        testCase.transactionsPerThread = 1000 * 1000 * 20
    }
    testCase.warmupRunIterationCount = k == 1 ? 1 : 0;
    testCase.driver = ContendedCounterDriver.class
    benchmark.add(testCase)
}

benchmark
