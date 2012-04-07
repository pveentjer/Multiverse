import org.benchy.Benchmark
import org.benchy.GroovyTestCase
import org.multiverse.stms.beta.benchmarks.JucLockStackDriver

def benchmark = new Benchmark();
benchmark.name = "juc_lock_stack"

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "juc_lock_stack_with_${k}_threads"
    testCase.pushThreadCount = k
    testCase.popThreadCount = k
    testCase.durationInSeconds=30
    testCase.transactionsPerThread = 1000 * 1000 * 10L
    testCase.warmupRunIterationCount = k==1?1:0
    testCase.capacity = Integer.MAX_VALUE
    testCase.driver = JucLockStackDriver.class
    benchmark.add(testCase)
}

benchmark
