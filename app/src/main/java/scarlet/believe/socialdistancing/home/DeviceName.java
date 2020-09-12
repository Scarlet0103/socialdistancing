package scarlet.believe.socialdistancing.home;

import android.net.wifi.p2p.WifiP2pManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class DeviceName {

    public void setDeviceName(WifiP2pManager manager, WifiP2pManager.Channel channel, String devName){
        try{
            Class[] paramTypes = new Class[3];
            paramTypes[0] = WifiP2pManager.Channel.class;
            paramTypes[1] = String.class;
            paramTypes[2] = WifiP2pManager.ActionListener.class;
            Method setDeviceName = manager.getClass().getMethod(
                    "setDeviceName", paramTypes);
            setDeviceName.setAccessible(true);

            Object arglist[] = new Object[3];
            arglist[0] = channel;
            arglist[1] = devName;
            arglist[2] = new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            };

            setDeviceName.invoke(manager, arglist);

        }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

}
