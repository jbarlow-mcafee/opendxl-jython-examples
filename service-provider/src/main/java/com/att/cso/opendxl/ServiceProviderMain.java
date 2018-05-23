package com.att.cso.opendxl;

import com.att.cso.opendxl.jython.client.JythonFactory;
import com.att.cso.opendxl.jython.client.exceptions.DxlJythonException;
import com.att.cso.opendxl.jython.client.interfaces.DxlProviderInterface;

public class ServiceProviderMain {
    public static boolean SHUTDOWN_RECEIVED = false;

    public static void main(String[] args) {

        ServiceThread thread = new ServiceThread();
        thread.start();

        while (!SHUTDOWN_RECEIVED) {
            System.out.println("Waiting for message for service...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Exited service loop...");

        thread.getInterface().stop();
        System.out.println("Issued stop to Python loop");
    }

}

class ServiceThread extends Thread {
    private String configFile = "./dxlclient.config";
    private String topic = "/my/service/test/dxlJythonTest";
    private DxlProviderInterface dxl = null;

    public void run() {
        JythonFactory jf = JythonFactory.getInstance();

        dxl = jf.getDxlProviderInterface();
        if (dxl == null) {
            System.out.println("Unable to get the Jython provider object");
            System.exit(0);
        }

        DxlCallbackImplementer dxlCallback = new DxlCallbackImplementer();
        try {
            dxl.start(configFile, "/att/test/service", topic, dxlCallback);
        } catch (DxlJythonException e) {
            System.out.println("Exception caught starting the provider");
            System.out.println("ErrorCode: " + e.getErrorCode() + "  Error message: '" + e.getMessage() + "'");
            dxl.stop();
            System.exit(0);
        }
    }

    public DxlProviderInterface getInterface() {
        return dxl;
    }

}
