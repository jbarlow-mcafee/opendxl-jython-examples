package com.att.cso.opendxl;

import com.att.cso.opendxl.jython.client.DxlMessage;
import com.att.cso.opendxl.jython.client.interfaces.DxlCallbackInterface;

public class DxlCallbackImplementer implements DxlCallbackInterface {

    @Override
    public String callbackEvent(DxlMessage message) {
        System.out.print(message.printDxlMessage());
        if (message.getPayload().contains("Stop listener"))
            ServiceProviderMain.SHUTDOWN_RECEIVED = true;
        return "Successful";
    }
}
