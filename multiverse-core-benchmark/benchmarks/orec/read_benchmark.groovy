import org.benchy.Benchmark
import org.benchy.GroovyTestCase

def benchmark = new Benchmark();
benchmark.name = "orec_read"

def readNormalTestCase = new GroovyTestCase()
readNormalTestCase.name = "orec_read_normal"
readNormalTestCase.warmupRunIterationCount = 1
readNormalTestCase.operationCount = 1000 * 1000 * 10000L
readNormalTestCase.driver = OrecNormalReadDriver.class
benchmark.add(readNormalTestCase)

def readBiasedUpdate = new GroovyTestCase()
readBiasedUpdate.name = "orec_read_biased"
readBiasedUpdate.warmupRunIterationCount = 1
readBiasedUpdate.operationCount = 1000 * 1000 * 10000L
readBiasedUpdate.driver = OrecReadBiasedReadDriver.class
benchmark.add(readBiasedUpdate)

benchmark
