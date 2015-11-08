package com.allaboutee.httphelper_teste;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ConnectNetwork {

    private static ConnectNetwork mInstance = null;
    private WifiManager wifiManager;
    private Context ctx;
    private Object sync;
    private ConnectNetwork(){}
    private boolean isInRange;
    private String desiredSSID;
    private boolean connected = false;

    public static ConnectNetwork getInstance(){
        if(mInstance == null)mInstance = new ConnectNetwork();
        return mInstance;
    }
    public void setContext(Context ctx){
        this.ctx=ctx;
        wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        this.sync=new Object();
    }

    public boolean conectarRede(String ssid) {
        this.desiredSSID=ssid;
        if(isSSDEqual(wifiManager.getConnectionInfo().getSSID(), this.desiredSSID)){
            System.out.println("Já conectado em "+ssid);
            return true;
        }
        connected = false;
        System.out.println("Tentando conectar em "+ssid);
        System.out.println("checando se a rede está ao alcance");
        if(!isInRange()){
            System.out.println("Rede fora de alcance");
            return false;
        }
        System.out.println("Rede ao alcance");
        System.out.println("Tentando conectar");
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && isSSDEqual(i.SSID,desiredSSID)) {
                if(!wifiManager.disconnect())return false;
                if(!wifiManager.enableNetwork(i.networkId, true))return false; //Desativa todas as outras redes, menos a desejada.
                if(!wifiManager.reconnect())return false;
                synchronized (sync) {
                    try {
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                        ctx.registerReceiver(BroadcastReceiver_connectionEstabilished, intentFilter);
                        sync.wait();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
                return connected;
            }

        }
        try {
            System.out.println("rede não configurada:" + desiredSSID);
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = "\"" + desiredSSID + "\"";
            config.preSharedKey = "\"" + "12345678" + "\"";
            config.status = WifiConfiguration.Status.ENABLED;
            int id = wifiManager.addNetwork(config);
            wifiManager.enableNetwork(id, true);
            wifiManager.saveConfiguration();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private boolean isInRange(){
        synchronized (sync) {
            try {
                wifiManager.startScan();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                ctx.registerReceiver(BroadcastReceiver_scanWIFI, intentFilter);
                Log.i("teste", "antes");
                sync.wait();
                Log.i("teste", "antes2");
            } catch (InterruptedException e) {
                return false;
            }
        }
        return this.isInRange;
    }

    private BroadcastReceiver BroadcastReceiver_scanWIFI = 	new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("teste", "teste");
            boolean _isInRange = false;
            isInRange = false;
            List<ScanResult> conexoes_ao_alcance = wifiManager.getScanResults();
            for (ScanResult i : conexoes_ao_alcance) {
                if (i.SSID != null && isSSDEqual(i.SSID, desiredSSID)) {
                    _isInRange = true;
                }
            }
            ctx.unregisterReceiver(this);
            isInRange=_isInRange;
            synchronized (sync) {
                sync.notify();
            }
        }
    };

    private BroadcastReceiver BroadcastReceiver_connectionEstabilished = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mWifi.isConnected() && isSSDEqual(wifiManager.getConnectionInfo().getSSID().toString(),desiredSSID)) {
                    connected=true;
                    ctx.unregisterReceiver(this);
                    synchronized (sync) {
                        sync.notify();
                    }
                }else{
                    connected=false;
                }
            }
        }
    };


    private boolean isSSDEqual(String str1, String str2){
        if(str1==null || str2==null)return false;
        if(str1.startsWith("\"") && str1.endsWith("\""))str1 = str1.substring(1, str1.length()-1);
        if(str2.startsWith("\"") && str2.endsWith("\""))str2 = str2.substring(1, str2.length()-1);
        return str1.equals(str2);
    }
}
