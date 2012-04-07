import org.benchy.Benchmark
import org.benchy.GroovyTestCase

import org.multiverse.stms.beta.benchmarks.orec.OrecCommitLockUpdateDriver
import org.multiverse.stms.beta.benchmarks.orec.OrecNormalUpdateDriver
import org.multiverse.stms.beta.benchmarks.orec.OrecWriteLockUpdateDriver

def benchmark = new Benchmark();
benchmark.name = "orec_update"

def updateTestCase = new GroovyTestCase()
updateTestCase.name = "orec_update_optimistic"
updateTestCase.warmupRunIterationCount = 1
updateTestCase.operationCount = 1000 * 1000 * 1000L
updateTestCase.driver = OrecNormalUpdateDriver.class
benchmark.add(updateTestCase)

def updateWithWriteLockTestCase = new GroovyTestCase()
updateWithWriteLockTestCase.name = "orec_update_with_writelock"
updateWithWriteLockTestCase.warmupRunIterationCount = 1
updateWithWriteLockTestCase.operationCount = 1000 * 1000 * 1000L
updateWithWriteLockTestCase.driver = OrecWriteLockUpdateDriver.class
benchmark.add(updateWithWriteLockTestCase)

def updateWithCommitLockTestCase = new GroovyTestCase()
updateWithCommitLockTestCase.name = "orec_update_with_commitlock"
updateWithCommitLockTestCase.warmupRunIterationCount = 1
updateWithCommitLockTestCase.operationCount = 1000 * 1000 * 1000L
updateWithCommitLockTestCase.driver = OrecCommitLockUpdateDriver.class
benchmark.add(updateWithCommitLockTestCase)

benchmark
