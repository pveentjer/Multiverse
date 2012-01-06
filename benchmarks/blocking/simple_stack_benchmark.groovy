import org.benchy.Benchmark
import org.benchy.GroovyTestCase
import org.multiverse.stms.beta.benchmarks.SimpleStackDriver

def benchmark = new Benchmark();
benchmark.name = "simple_stack"

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "simple_stack_with_${k}_threads"
    testCase.pushThreadCount = k
    testCase.popThreadCount = k
    testCase.durationInSeconds=30
    testCase.transactionsPerThread = 1000 * 1000 * 10L
    testCase.warmupRunIterationCount = k==1?1:0;
    testCase.driver = SimpleStackDriver.class
    benchmark.add(testCase)
}

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "simple_stack_using_pooled_closures_with_${k}_threads"
    testCase.pushThreadCount = k
    testCase.popThreadCount = k
    testCase.durationInSeconds=30
    testCase.poolClosures = true
    testCase.transactionsPerThread = 1000 * 1000 * 10L
    testCase.warmupRunIterationCount = k==1?1:0;
    testCase.driver = SimpleStackDriver.class
    benchmark.add(testCase)
}
benchmark
