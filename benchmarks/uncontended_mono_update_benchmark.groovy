import org.benchy.Benchmark
import org.benchy.GroovyTestCase
import org.multiverse.stms.beta.benchmarks.UncontendedMonoUpdateDriver

def benchmark = new Benchmark();
benchmark.name = "uncontended_mono_update"

for (def k in 1..processorCount) {
    def testCase = new GroovyTestCase()
    testCase.name = "uncontended_mono_update_with_${k}_threads"
    testCase.threadCount = k
    testCase.transactionsPerThread = 1000 * 1000 * 50
    testCase.driver = UncontendedMonoUpdateDriver.class
    testCase.warmupRunIterationCount = k==1?1:0;
    benchmark.add(testCase)
}
benchmark
