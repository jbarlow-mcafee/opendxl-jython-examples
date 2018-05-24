package com.att.cso.opendxl;

import java.io.File;

import com.att.cso.opendxl.jython.client.DxlMessage;
import com.att.cso.opendxl.jython.client.JythonFactory;
import com.att.cso.opendxl.jython.client.exceptions.DxlJythonException;
import com.att.cso.opendxl.jython.client.interfaces.DxlCallbackInterface;
import com.att.cso.opendxl.jython.client.interfaces.DxlClientInterface;
import com.att.cso.opendxl.jython.client.interfaces.DxlListenerInterface;
import com.att.cso.opendxl.jython.client.interfaces.DxlPublisherInterface;

public class BasicEventMain {
    public static String EVENT_TOPIC = "/isecg/sample/basicevent";
    public static int TOTAL_EVENTS  = 1000;

    public static void main(String[] args) {
        String configFile = "./dxlclient.config";

        JythonFactory jf = JythonFactory.getInstance();

        DxlClientInterface dxlClient = jf.getDxlClientInterface();
        if (dxlClient == null) {
            System.out.println("Was unable to get the Jython DXL client object");
            System.exit(0);
        }

        String result;

        try {
            dxlClient.connect(configFile);
            if (!dxlClient.isConnected()) {
                System.out.println("Something bad happened and we weren't able to connect");
                System.exit(0);
            }
        } catch (DxlJythonException e) {
            System.out.println("Caught a DxlJythonException trying to connect, exiting program");
            System.exit(0);
        }

        DxlPublisherInterface dxlPublisher = jf.getDxlPublisherInterface();
        if (dxlPublisher == null) {
            System.out.println("Was unable to get the Jython DXL Publisher object");
            System.exit(0);
        }
        dxlPublisher.setClient(dxlClient);

        EventThread thread = new EventThread(dxlClient);
        thread.start();

        try {
            for (int i = 0; i < TOTAL_EVENTS; i++) {
                dxlPublisher.sendMessage(BasicEventMain.EVENT_TOPIC,
                        Integer.toString(i));
            }

            System.out.println("Waiting for events to be received");
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
            System.out.println("Finished waiting for events");
        } catch (DxlJythonException e) {
            System.out.println("Exception caught trying to send messages on the DXL broker: " + e.getMessage());
            thread.stopListener();
        }

        dxlClient.destroy();
        if (dxlClient.isConnected()) {
            System.out.println("Something bad happened and we may still be connected to the broker");
        }
    }
}

class DxlCallbackImplementer implements DxlCallbackInterface {
    private DxlListenerInterface listener;
    private int eventsReceived;

    DxlCallbackImplementer(DxlListenerInterface listener) {
        this.eventsReceived = 0;
        this.listener = listener;
    }

    @Override
    public String callbackEvent(DxlMessage message) {
        System.out.print(message.printDxlMessage());
        eventsReceived++;
        if (eventsReceived >= BasicEventMain.TOTAL_EVENTS) {
            this.listener.stop();
        }
        return "Successful";
    }
}

class EventThread extends Thread {
    private DxlListenerInterface dxlListener = null;
    private DxlClientInterface dxlClient = null;

    EventThread(DxlClientInterface dxlClient) {
        this.dxlClient = dxlClient;

        JythonFactory jf = JythonFactory.getInstance();

        dxlListener = jf.getDxlListenerInterface();
        if (dxlListener == null) {
            System.out.println("Unable to get the Jython provider object");
            System.exit(0);
        }
    }

    public void run() {
        DxlCallbackImplementer dxlCallback = new DxlCallbackImplementer(
                dxlListener);
        try {
            dxlListener.start(dxlClient, BasicEventMain.EVENT_TOPIC,
                    dxlCallback);
        } catch (DxlJythonException e) {
            System.out.println("Exception caught starting the provider");
            System.out.println("ErrorCode: " + e.getErrorCode() + "  Error message: '" + e.getMessage() + "'");
            dxlListener.stop();
            System.exit(0);
        }
    }

    public void stopListener() {
        dxlListener.stop();
    }
}
