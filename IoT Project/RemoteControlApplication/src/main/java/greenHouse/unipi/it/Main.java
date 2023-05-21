package greenHouse.unipi.it;

import greenHouse.unipi.it.threads.CLIThread;
import greenHouse.unipi.it.threads.PollingDBThread;

public class Main {
    public static void main(String args[]){
        CLIThread cliThread = new CLIThread();
        PollingDBThread pollingDBThread = new PollingDBThread();

        cliThread.start();
        pollingDBThread.start();
    }
}
