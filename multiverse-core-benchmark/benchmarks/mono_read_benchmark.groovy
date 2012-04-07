import org.benchy.Benchmark
import org.benchy.GroovyTestCase
import org.multiverse.stms.beta.benchmarks.MonoReadDriver

def benchmark = new Benchmark();
benchmark.name = "mono_read"

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "mono_read_with_${k}_threads"
    testCase.threadCount = k
    testCase.transactionsPerThread = 1000 * 1000 * 500L
    testCase.driver = MonoReadDriver.class
    testCase.warmupRunIterationCount = k==1?1:0;
    benchmark.add(testCase)
}
benchmark
