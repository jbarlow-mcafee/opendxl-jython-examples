package com.att.cso.opendxl;

import java.io.File;

import com.att.cso.opendxl.jython.client.DxlMessage;
import com.att.cso.opendxl.jython.client.JythonFactory;
import com.att.cso.opendxl.jython.client.exceptions.DxlJythonException;
import com.att.cso.opendxl.jython.client.interfaces.DxlRequesterInterface;

public class ServiceRequesterMain {
    public static boolean MSG_RECEIVED = false;

    public static void main(String[] args) {
        String configFile = "./dxlclient.config";
        String topic = "/my/service/test/dxlJythonTest";
        String message = "This is a test event from OpendxlServiceRequester";


        JythonFactory jf = JythonFactory.getInstance();

        DxlRequesterInterface dxl = jf.getDxlRequesterInterface();
        if (dxl == null) {
            System.out.println("Was unable to get the Jython object");
            System.exit(0);
        }

        DxlMessage result;

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
            int idx = 0;
            for (idx = 0; idx < 10; idx++) {
                result = dxl.sendMessage(topic, message + " " + idx);
                System.out.println("Results:");
                System.out.print(result.printDxlMessage());
            }

            try {
                Thread.sleep(10000); // 10 seconds
            } catch (InterruptedException e) {
            }

            result = dxl.sendMessage(topic, "Stop listener");
            System.out.println("Stop message result:");
            System.out.print(result.printDxlMessage());
        } catch (DxlJythonException e) {
            System.out.println("Exception caught trying to send messages on the DXL broker");
        }

        dxl.disconnect();
        if (dxl.isConnected()) {
            System.out.println("Something bad happened and we may still be connected to the broker");
        }
    }
}
