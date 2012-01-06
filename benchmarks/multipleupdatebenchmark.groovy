import org.benchy.Benchmark
import org.benchy.GroovyTestCase
import org.multiverse.stms.beta.benchmarks.MultipleUpdateDriver

def benchmark = new Benchmark()
benchmark.name = "multiple_update"

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "multiple_update_with_${k}_refs"
    testCase.threadCount = 1
    testCase.refCount = k
    testCase.transactionsPerThread = 1000 * 1000 * 20
    testCase.driver = MultipleUpdateDriver.class
    benchmark.add(testCase)
    testCase.warmupRunIterationCount = k==1?1:0;
}
benchmark
