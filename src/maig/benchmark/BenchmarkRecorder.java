package maig.benchmark;

import com.sun.org.apache.xpath.internal.SourceTree;
import maig.util.Stopwatch;
import maig.util.StreamGobbler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import scr.Client;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by nifa@itu.dk
 */
public class BenchmarkRecorder {
    private static Process startTorcs(String pathToTorcs) {
        String racexml = "benchmark.xml";
        try {
            Process torcs = new ProcessBuilder(pathToTorcs + "wtorcs.exe", "-r", racexml).directory(new File(pathToTorcs)).start();
            new StreamGobbler(torcs.getInputStream(), "[wtorcs.exe] ").start();
            return torcs;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static File findResult(String pathToTorcs) {
        File benchmarkResults = new File(pathToTorcs + "results/benchmark");
        long newest = 0;
        File result = null;
        for (File file : benchmarkResults.listFiles()) {
            long time = file.lastModified();
            if(time > newest) {
                newest = time;
                result = file;
            }
        }
        return result;
    }
    private static double parseResult(File path) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path);
            XPathExpression expression = XPathFactory.newInstance().newXPath().compile("//attnum[@name=\"time\"]/@val");
            Node time = (Node)expression.evaluate(document, XPathConstants.NODE);
            return Double.parseDouble( time.getNodeValue() );
        } catch (ParserConfigurationException | IOException | SAXException | XPathExpressionException e) {
            e.printStackTrace();
            return -1.0;
        }
    }

    /**
     * @param args
     *      args[0] is the path to the TORCS installation, fx "C:\\Program Files (x86)\\torcs\\" (remember to escape \ on windows)
     *      args[1] is the number of benchmarks to be run.
     *      Any additional arguments will be passed to the TORCS java client (fx the controller class name)
     */
    public static void main(String[] args) {
        String pathToTorcs = args[0]; /*"C:\\Program Files (x86)\\torcs\\"; */
        int n = Integer.parseInt(args[1]);
        String[] clientArgs = Arrays.stream(args).skip(2).toArray(String[]::new);

        Process torcs = startTorcs(pathToTorcs);
        Stopwatch total = new Stopwatch();
        double[] results = new double[n];
        for (int i = 0; i < n; i++) {
            Stopwatch single = new Stopwatch();
            System.out.println(" ***************************** " + Arrays.toString(results));
            if(!torcs.isAlive()) {
                torcs = startTorcs(pathToTorcs);
                System.out.println(" ***************************** RESTARTING TORCS " + torcs);
            }
            Client.main(clientArgs);
            File result = findResult(pathToTorcs);
            results[i] = parseResult(result);
            System.out.println(" ***************************** TIME: " + single);
        }
        System.out.println(" ***************************** " + Arrays.toString(results));
        System.out.println(" ***************************** TOTAL TIME: " + total);
    }
}
