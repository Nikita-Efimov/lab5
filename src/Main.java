import java.io.*;
import sun.misc.Signal;
import sun.misc.SignalHandler;

#define println(args) System.out.println(args)
#define print(args) System.out.print(args)

public class Main {
    private static String filename;
    private static CmdWorker worker;

    static {
        initSignalHandlers();
    }

    public static void initSignalHandlers() {
        SignalHandler signalHandler = new SignalHandler() {
            @Override
            public void handle(Signal sig) {
                release();
            }
        };

        DiagnosticSignalHandler.install("TERM", signalHandler);
        DiagnosticSignalHandler.install("INT", signalHandler);
        DiagnosticSignalHandler.install("ABRT", signalHandler);
        DiagnosticSignalHandler.install("TSTP", signalHandler);
    }

    public static void release() {
        worker.printToFile(filename);
        System.exit(0);
    }

    public static void load() {
        worker.readFromFile(filename);
    }

    public static void handling() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            print("-> ");
            String cmd = reader.readLine();
            worker.doCmd(cmd);
        }
    }

    public static void main(String ... args) {
        worker = new CmdWorker();

        if (args.length > 0) {
            filename = args[0];
            load();
        } else
            filename = "storage.xml";

        try {
            handling();
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
}
