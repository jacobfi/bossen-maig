package maig.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
/*
 * From: http://stackoverflow.com/a/1732506
 */
public class StreamGobbler extends Thread {
    InputStream is;
    String prefix;

    // reads everything from is until empty.
    public StreamGobbler(InputStream is, String prefix) {
        this.is = is;
        this.prefix = prefix;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ( (line = br.readLine()) != null) {
                System.out.println(prefix + line);
                Thread.sleep(50);
            }

        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        }
    }
}
