package com.att.cso.opendxl;

import com.att.cso.opendxl.jython.client.JythonFactory;
import com.att.cso.opendxl.jython.client.exceptions.DxlJythonException;
import com.att.cso.opendxl.jython.client.interfaces.DxlPublisherInterface;

public class EventPublisherMain {
    public static boolean MSG_RECEIVED = false;

    public static void main(String[] args) {
        String configFile = "./dxlclient.config";
        String topic = "/my/event/test/dxlJythonTest";
        String message = "This is a test event from OpendxlEventPublisher";

        JythonFactory jf = JythonFactory.getInstance();

        DxlPublisherInterface dxl = jf.getDxlPublisherInterface();
        if (dxl == null) {
            System.out.println("Was unable to get the Jython object");
            System.exit(0);
        }

        String result;

        try {
            dxl.connect(configFile);
            if (!dxl.isConnected()) {
                System.out.println("Something bad happened and we weren't able to connect");
                System.exit(0);
            }
        } catch (DxlJythonException e) {
            System.out.println("Caught a DxlJythonException trying to connect, exiting program");
            System.exit(0);
        }


        try {
            result = dxl.sendMessage(topic, message);
            System.out.println("Result: '" + result + "' for message '" + message + "'");

            try {
                Thread.sleep(10000); // 10 seconds
            } catch (InterruptedException e) {
            }

            result = dxl.sendMessage(topic, "Stop listener");
            System.out.println("Stop message result: '" + result + "'");
        } catch (DxlJythonException e) {
            System.out.println("Exception caught trying to send messages on the DXL broker");
        }

        dxl.disconnect();
        if (dxl.isConnected()) {
            System.out.println("Something bad happened and we may still be connected to the broker");
        }
    }
}
