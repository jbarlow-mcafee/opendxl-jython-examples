package com.att.cso.opendxl;

import java.io.File;

import com.att.cso.opendxl.jython.client.JythonFactory;
import com.att.cso.opendxl.jython.client.exceptions.DxlJythonException;
import com.att.cso.opendxl.jython.client.interfaces.DxlListenerInterface;

public class EventListenerMain {
    public static boolean SHUTDOWN_RECEIVED = false;

    public static void main(String[] args) {

        System.out.println("Path: " + (new File(".")).getAbsolutePath());

        EventThread thread = new EventThread();
        thread.start();

        while (!SHUTDOWN_RECEIVED) {
            System.out.println("Waiting for message for service...");
            try {
                Thread.sleep(10000); // 10 seconds
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Exited event listener loop...");

        thread.getInterface().stop();
        System.out.println("Issued stop to Python loop");
    }

}

class EventThread extends Thread {
    String configFile = "./dxlclient.config";
    String topic = "/my/event/test/dxlJythonTest";
    private DxlListenerInterface dxl = null;

    public void run() {
        JythonFactory jf = JythonFactory.getInstance();

        dxl = jf.getDxlListenerInterface();
        if (dxl == null) {
            System.out.println("Unable to get the Jython provider object");
            System.exit(0);
        }

        DxlCallbackImplementer dxlCallback = new DxlCallbackImplementer();
        try {
            dxl.start(configFile, topic, dxlCallback);
        } catch (DxlJythonException e) {
            System.out.println("Exception caught starting the provider");
            System.out.println("ErrorCode: " + e.getErrorCode() + "  Error message: '" + e.getMessage() + "'");
            dxl.stop();
            System.exit(0);
        }
    }

    public DxlListenerInterface getInterface() {
        return dxl;
    }
}
