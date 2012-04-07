import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import static org.benchy.JGraphGraphBuilder.*

def benchmarks = searcher.findAllBenchmarks('boxing_overhead')
println("Benchy > Found ${benchmarks.size()} results")

def categoryDataSet = new DefaultCategoryDataset();
def xySeriesDataSet = new XYSeriesCollection();

for (def benchmark in benchmarks) {
    def seriesNoBoxing = new XYSeries("${benchmark.date} no boxing");
    def seriesBoxing = new XYSeries("${benchmark.date} with boxing");

    def testcases = new LinkedList(benchmark.testCases)
    testcases.sort {it.threadCount}

    testcases.each {
        def testCase = it
        def transactionsPerSecondPerThread = testCase.average('transactionsPerThreadPerSecond')
        categoryDataSet.addValue(transactionsPerSecondPerThread, "${benchmark.date} ${testCase.name}", testCase.threadCount)

        if(testCase.withBoxing){
            seriesBoxing.add(testCase.threadCount, transactionsPerSecondPerThread)
        }else{
            seriesNoBoxing.add(testCase.threadCount, transactionsPerSecondPerThread)
        }
    }

    xySeriesDataSet.addSeries(seriesNoBoxing);
    xySeriesDataSet.addSeries(seriesBoxing);
}

writeLineChartAsPng(xySeriesDataSet, "Boxing overhead", "threads", "transaction/second/thread", new File("charts/boxing_overhead_line_wide.png"))
writeLineChartAsPng(xySeriesDataSet, "Boxing overhead", "threads", "transaction/second/thread", 600,new File("charts/boxing_overhead_line_narrow.png"))
writeBarChartAsPng(categoryDataSet, "Boxing overhead", "threads", "transaction/second/thread", new File("charts/boxing_overhead_bar.png"))


