import sun.misc.Signal;
import sun.misc.SignalHandler;

#define println(args) System.out.println(args)

public class DiagnosticSignalHandler implements SignalHandler {
    // Static method to install the signal handler
    public static void install(String signalName, SignalHandler handler) {
        Signal signal = new Signal(signalName);
        DiagnosticSignalHandler diagnosticSignalHandler = new DiagnosticSignalHandler();
        SignalHandler oldHandler = Signal.handle(signal, diagnosticSignalHandler);
        diagnosticSignalHandler.setHandler(handler);
        diagnosticSignalHandler.setOldHandler(oldHandler);
    }
    private SignalHandler oldHandler;
    private SignalHandler handler;

    private DiagnosticSignalHandler() {}

    private void setOldHandler(SignalHandler oldHandler) {
        this.oldHandler = oldHandler;
    }

    private void setHandler(SignalHandler handler) {
        this.handler = handler;
    }

    // Signal handler method
    @Override
    public void handle(Signal sig) {
        // Go to new string after SIG print
        println();
        // println("Signal handler called for signal " + sig);
        try {
            handler.handle(sig);

            // Chain back to previous handler, if one exists
            if (oldHandler != SIG_DFL && oldHandler != SIG_IGN)
                oldHandler.handle(sig);

        } catch (Exception e) {
            println("Signal handler failed, reason " + e);
        }
    }
}
