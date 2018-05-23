package com.att.cso.opendxl;

import java.util.HashMap;
import java.util.Map;

import com.att.cso.opendxl.jython.client.JythonFactory;
import com.att.cso.opendxl.jython.client.exceptions.DxlJythonException;
import com.att.cso.opendxl.jython.client.interfaces.DxlCallbackInterface;
import com.att.cso.opendxl.jython.client.interfaces.DxlProviderInterface;

public class ServiceProviderMain {
    public static boolean SHUTDOWN_RECEIVED = false;

    public static void main(String[] args) {

        Service1Thread service1Thread = new Service1Thread();
        service1Thread.start();

        Service2Thread service2Thread = new Service2Thread();
        service2Thread.start();

        while (!SHUTDOWN_RECEIVED) {
            System.out.println("Waiting for message for service...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Exited service loop...");

        service1Thread.getInterface().stop();
        try {
            service1Thread.join();
        } catch (InterruptedException e) {
        }

        service2Thread.getInterface().stop();
        try {
            service2Thread.join();
        } catch (InterruptedException e) {
        }

        System.out.println("Issued stop to Python loop");
    }
}

class Service1Thread extends Thread {
    private String configFile = "./dxlclient.config";
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
            dxl.start(configFile, "/att/test/dxl",
                    "/my/service/test/dxlJythonTest",
                    dxlCallback);
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

class Service2Thread extends Thread {
    private String configFile = "./dxlclient.config";
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
            Map<String, DxlCallbackInterface> callbacksByTopic = new HashMap<>();
            callbacksByTopic.put("/my/service/test/dxlJythonTest1",
                    dxlCallback);
            callbacksByTopic.put("/my/service/test/dxlJythonTest2",
                    dxlCallback);
            dxl.start(configFile, "/att/test/service2", callbacksByTopic);
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
