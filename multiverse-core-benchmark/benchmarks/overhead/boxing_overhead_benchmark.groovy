import org.multiverse.stms.beta.benchmarks.BoxingOverheadDriver
import org.benchy.Benchmark
import org.benchy.GroovyTestCase

def benchmark = new Benchmark();
benchmark.name = "boxing_overhead"

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "boxing_overhead_with_${k}_threads_and_boxing"
    testCase.threadCount = k
    testCase.withBoxing = true
    testCase.transactionsPerThread = 1000 * 1000 * 100
    testCase.warmupRunIterationCount = k == 1 ? 1 : 0;
    testCase.driver = BoxingOverheadDriver.class
    benchmark.add(testCase)
}

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "boxing_overhead_with_${k}_threads_and_no_boxing"
    testCase.threadCount = k
    testCase.withBoxing = false
    testCase.transactionsPerThread = 1000 * 1000 * 200
    testCase.warmupRunIterationCount = k == 1 ? 1 : 0;
    testCase.driver = BoxingOverheadDriver.class
    benchmark.add(testCase)
}

benchmark
