import org.benchy.Benchmark
import org.benchy.GroovyTestCase
import org.multiverse.stms.beta.benchmarks.AtomicGetDriver

def benchmark = new Benchmark();
benchmark.name = "atomic_weak_get"

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "atomic_weak_get_with_${k}_threads"
    testCase.threadCount = k
    testCase.transactionsPerThread = 1000 * 1000 * 10000L
    testCase.sharedRef = true
    testCase.weakGet = true
    testCase.warmupRunIterationCount = k==1?1:0;
    testCase.driver = AtomicGetDriver.class
    benchmark.add(testCase)
}
benchmark
