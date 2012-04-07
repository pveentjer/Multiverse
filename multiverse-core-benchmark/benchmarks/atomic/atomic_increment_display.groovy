import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import static org.benchy.JGraphGraphBuilder.writeBarChartAsPng
import static org.benchy.JGraphGraphBuilder.writeLineChartAsPng

def benchmarks = searcher.findAllBenchmarks('atomic_increment')
println("Benchy > Found ${benchmarks.size()} results")

def categoryDataSet = new DefaultCategoryDataset()
def xySeriesDataSet = new XYSeriesCollection()
def totalXySeriesDataSet = new XYSeriesCollection();

for (def benchmark in benchmarks) {
    def series = new XYSeries(benchmark.date)
    def totalSeries = new XYSeries(benchmark.date)

    def entries = new LinkedList(benchmark.testCases)
    entries.sort {it.threadCount}

    entries.each {
        def testCase = it
        def transactionsPerSecondPerThread = testCase.average('transactionsPerSecondPerThread')
        categoryDataSet.addValue(transactionsPerSecondPerThread, benchmark.date, testCase.threadCount)
        series.add(testCase.threadCount, transactionsPerSecondPerThread)

	def transactionsPerSecond = testCase.average('transactionsPerSecond')
        totalSeries.add(testCase.threadCount, transactionsPerSecond)
    }

    xySeriesDataSet.addSeries(series)
    totalXySeriesDataSet.addSeries(totalSeries);
}

writeLineChartAsPng(xySeriesDataSet, "Atomic Increment", "threads", "transaction/second/thread", new File("charts/atomic_increment_line_wide.png"))
writeLineChartAsPng(xySeriesDataSet, "Atomic Increment", "threads", "transaction/second/thread", 600, new File("charts/atomic_increment_line_narrow.png"))
writeLineChartAsPng(totalXySeriesDataSet, "Atomic Increment", "threads", "transaction/second", new File("charts/atomic_increment_line_total_wide.png"))
writeLineChartAsPng(totalXySeriesDataSet, "Atomic Increment", "threads", "transaction/second", 600, new File("charts/atomic_increment_line_total_narrow.png"))
writeBarChartAsPng(categoryDataSet, "Atomic Increment", "threads", "transaction/second/thread", new File("charts/atomic_increment_bar.png"))
