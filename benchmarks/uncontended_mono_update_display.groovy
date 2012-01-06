import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import static org.benchy.JGraphGraphBuilder.*

def benchmarks = searcher.findAllBenchmarks('uncontended_mono_update')
println("Benchy > Found ${benchmarks.size()} results")

def categoryDataSet = new DefaultCategoryDataset();
def xySeriesDataSet = new XYSeriesCollection();

for (def benchmark in benchmarks) {
    def series = new XYSeries(benchmark.date);

    def entries = new LinkedList(benchmark.testCases)
    entries.sort {it.threadCount}

    entries.each {
        def testCase = it
        def transactionsPerSecondPerThread = testCase.average('transactionsPerSecondPerThread')
        categoryDataSet.addValue(transactionsPerSecondPerThread, benchmark.date, testCase.threadCount)
        series.add(testCase.threadCount, transactionsPerSecondPerThread)
    }

    xySeriesDataSet.addSeries(series);
}

writeLineChartAsPng(xySeriesDataSet, "Uncontended Mono Update", "threads", "transaction/second/thread", new File("charts/uncontended_mono_update_line_wide.png"))
writeLineChartAsPng(xySeriesDataSet, "Uncontended Mono Update", "threads", "transaction/second/thread", 600, new File("charts/uncontended_mono_update_line_narrow.png"))
writeBarChartAsPng(categoryDataSet, "Uncontended Mono Update", "threads", "transaction/second/thread", new File("charts/uncontended_mono_update_bar.png"))


