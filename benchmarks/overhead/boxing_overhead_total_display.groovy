import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import static org.benchy.JGraphGraphBuilder.*

def benchmarks = searcher.findAllBenchmarks('boxing_overhead')
println("Benchy > Found ${benchmarks.size()} results")

def xySeriesDataSet = new XYSeriesCollection();

for (def benchmark in benchmarks) {
    def seriesNoBoxing = new XYSeries("${benchmark.date} no boxing");
    def seriesBoxing = new XYSeries("${benchmark.date} with boxing");

    def testcases = new LinkedList(benchmark.testCases)
    testcases.sort {it.threadCount}

    testcases.each {
        def testCase = it
        def transactionsPerSecondPerThread = testCase.average('transactionsPerSecond')

        if(testCase.withBoxing){
            seriesBoxing.add(testCase.threadCount, transactionsPerSecondPerThread)
        }else{
            seriesNoBoxing.add(testCase.threadCount, transactionsPerSecondPerThread)
        }
    }

    xySeriesDataSet.addSeries(seriesNoBoxing);
    xySeriesDataSet.addSeries(seriesBoxing);
}

writeLineChartAsPng(xySeriesDataSet, "Boxing overhead", "threads", "transaction/second", new File("charts/boxing_overhead_total_line_wide.png"))
writeLineChartAsPng(xySeriesDataSet, "Boxing overhead", "threads", "transaction/second", 600, new File("charts/boxing_overhead_total_line_narrow.png"))


