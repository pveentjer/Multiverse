import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import static org.benchy.JGraphGraphBuilder.*

def benchmarks = searcher.findAllBenchmarks('counter')
println("Benchy > Found ${benchmarks.size()} results")

def categoryDataSet = new DefaultCategoryDataset();
def xySeriesDataSet = new XYSeriesCollection();

for (def benchmark in benchmarks) {
    def seriesNoDirtyCheck = new XYSeries("${benchmark.date} without dirty check");
    def seriesDirtyCheck = new XYSeries("${benchmark.date} with dirty check");

    def testcases = new LinkedList(benchmark.testCases)
    testcases.sort {it.threadCount}

    testcases.each {
        def testCase = it
        def transactionsPerSecondPerThread = testCase.average('transactionsPerSecondPerThread')
        categoryDataSet.addValue(transactionsPerSecondPerThread, "${benchmark.date} ${testCase.name}", testCase.threadCount)

        if(testCase.dirtyCheck){
            seriesDirtyCheck.add(testCase.threadCount, transactionsPerSecondPerThread)
        }else{
            seriesNoDirtyCheck.add(testCase.threadCount, transactionsPerSecondPerThread)
        }
    }

    xySeriesDataSet.addSeries(seriesNoDirtyCheck);
    xySeriesDataSet.addSeries(seriesDirtyCheck);
}

writeLineChartAsPng(xySeriesDataSet, "Contended Counter", "threads", "transaction/second/thread", new File("charts/contended_counter_line_wide.png"))
writeLineChartAsPng(xySeriesDataSet, "Contended Counter", "threads", "transaction/second/thread", 600, new File("charts/contended_counter_line_narrow.png"))
writeBarChartAsPng(categoryDataSet, "Contended Counter", "threads", "transaction/second/thread", new File("charts/contended_counter_update_bar.png"))


