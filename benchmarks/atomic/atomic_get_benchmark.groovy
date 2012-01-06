import org.benchy.Benchmark
import org.benchy.GroovyTestCase
import org.multiverse.stms.beta.benchmarks.AtomicGetDriver

def benchmark = new Benchmark();
benchmark.name = "atomic_get"

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "atomic_get_with_${k}_threads"
    testCase.warmupRunIterationCount = k==1?1:0;
    testCase.threadCount = k
    testCase.transactionsPerThread = 1000 * 1000 * 10000L
    testCase.sharedRef = true
    testCase.weakGet = false
    testCase.driver = AtomicGetDriver.class
    benchmark.add(testCase)
}
benchmark
