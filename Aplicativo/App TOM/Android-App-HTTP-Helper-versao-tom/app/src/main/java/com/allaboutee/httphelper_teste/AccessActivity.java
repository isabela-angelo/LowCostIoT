package com.allaboutee.httphelper_teste;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AccessActivity extends ListESP {

    private static final String TAG = "AccessActivity";

    private TextView txtestado, txtestado_2;
    private Switch switch_int;

    private Button button_ON, button_OFF;
    String nome;
    String portNumber = "80";
    Boolean enviou = false;


    private static boolean busy = false;
    private static String last_action = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);

        switch_int = (Switch) findViewById(R.id.switch_int);
        switch_int.setTextOn("");
        switch_int.setTextOff("");

        button_ON = (Button)findViewById(R.id.button_ON);
        button_OFF = (Button)findViewById(R.id.button_OFF);


        txtestado = (TextView)findViewById(R.id.estado);
        txtestado_2 = (TextView)findViewById(R.id.estado_2);

        /*new HttpRequestAsyncTask(
                getApplicationContext(), "=" + "estado", "192.168.1.97", ":" + portNumber, "/?"
        ).execute();*/
        //interruptor
        try {
            new HttpRequestAsyncTask(
                    getApplicationContext(), "=" + "estado", "192.168.1.95", ":" + portNumber, "/?"
            ).execute().get();
            String estado_2 = Globals.getInstance().getData(1);
            System.out.println("estado_2 " + estado_2);
            String[] parts_2 = estado_2.split("_");
            String est_2 = parts_2[1];

            if (est_2.equals("ligado")) {
                txtestado_2.setText("Azul");
                switch_int.setChecked(true);
            }
            else if (est_2.equals("desligado")) {
                txtestado_2.setText("Cinza");
                switch_int.setChecked(false);
            }
            switch_int.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.v(TAG, "AccessActivity.last_actionint" + AccessActivity.last_action);
                    Log.v(TAG, "AccessActivity.busy" + Boolean.toString(AccessActivity.busy));
                    Log.v(TAG, "switch_int.isEnabled()" + Boolean.toString(switch_int.isEnabled()));
                    if (!AccessActivity.busy) {
                        if (switch_int.isEnabled()) {
                            if (isChecked && !AccessActivity.last_action.equals("Azul")) {
                                Log.v(TAG, "aqui!");
                                button_OFF.setEnabled(false);
                                button_ON.setEnabled(false);
                                switch_int.setEnabled(false);
                                AccessActivity.busy = true;
                                enviou = enviarHTTP("on", switch_int.getContext(), "192.168.1.95");
                                if (enviou) {
                                    txtestado_2.setText("Azul");
                                    AccessActivity.last_action = "Azul";
                                }
                            } else if (!isChecked && !AccessActivity.last_action.equals("Cinza")) {
                                enviou = enviarHTTP("off", switch_int.getContext(), "192.168.1.95");
                                if (enviou) {
                                    txtestado_2.setText("Cinza");
                                    AccessActivity.last_action = "Cinza";
                                }
                            }
                            else {
                                switch_int.toggle();
                            }
                        }
                    }
                }
            });
        }
        catch (Exception e) {
            Log.v(TAG, "erro no envio: "+e);
            txtestado_2.setText("Desativado! Volte ao início e tente novamente!");
        }


        //tomada
        try {
            new HttpRequestAsyncTask(
                    getApplicationContext(), "=" + "estado", "192.168.1.96", ":" + portNumber, "/?"
            ).execute().get();
            String estado = Globals.getInstance().getData(1);
            System.out.println("estado " + estado);
            String[] parts = estado.split("_");
            String est = parts[1];

            txtestado.setText(est);
            createUI("192.168.1.96");
            setButton(est);
        }
        catch (Exception e){
            Log.v(TAG, "erro no envio: "+e);
            txtestado.setText("Desativado! Volte ao início e tente novamente!");
        }



        /*switch_int.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //if (!AccessActivity.busy || switch_int.isEnabled()) {
                if (switch_int.isEnabled()) {
                    AccessActivity.busy = true;

                    button_OFF.setEnabled(false);
                    button_ON.setEnabled(false);
                    switch_int.setEnabled(false);

                    if (isChecked) {
                        enviou = enviarHTTP("on", getApplicationContext(), "192.168.1.95");
                        if (enviou) {
                            txtestado_2.setText("Azul");
                        }
                    } else {
                        enviou = enviarHTTP("off", getApplicationContext(), "192.168.1.95");
                        if (enviou) {
                            txtestado_2.setText("Cinza");
                        }

                    }
                }
            }
        });
        createUI("192.168.1.96");*/

    }


    public void setButton(String state) {
        Log.v(TAG, "state: "+state);
        if (state.equals("ligado")) {
            button_ON.setEnabled(false);
            button_OFF.setEnabled(true);
        }
        else {
            button_OFF.setEnabled(false);
            button_ON.setEnabled(true);
        }

    }

    public void createUI(String ip) {
        if (ip.equals("192.168.1.95")) {


        }
        else if(ip.equals("192.168.1.96")) {
            //Nome do dispositivo e botoes
            button_ON.setOnClickListener(this);
            button_OFF.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        Log.v(TAG, "AccessActivity.last_actionbutton" + AccessActivity.last_action);
        if (!AccessActivity.busy) {
            if ((button_OFF.isEnabled() && !AccessActivity.last_action.equals("off")) ||
                    (button_ON.isEnabled() && !AccessActivity.last_action.equals("on"))) {

                if (view.getId() == button_OFF.getId() || view.getId() == button_ON.getId()) {
                    AccessActivity.busy = true;
                    button_OFF.setEnabled(false);
                    button_ON.setEnabled(false);
                    switch_int.setEnabled(false);
                    String parameterValue = "";

                    //Rotinas botoes on e off
                    if (view.getId() == button_ON.getId()) {
                        parameterValue = "on";


                    } else if (view.getId() == button_OFF.getId()) {
                        parameterValue = "off";

                    }

                    enviou = enviarHTTP(parameterValue, view.getContext(), "192.168.1.96");

                    if (enviou) {
                        if (parameterValue.equals("on")) {
                            AccessActivity.last_action = "on";
                            txtestado.setText("ligado");
                        } else {
                            AccessActivity.last_action = "off";
                            txtestado.setText("desligado");
                        }
                        setButton(txtestado.getText().toString());

                    }

                }
            }
        }
    }

    //Envia comando HTTP GET para o ESP
    public boolean enviarHTTP(String parameterValue, Context ctx, String ip) {

        String portNumber = "80";
        Log.v(TAG, "ip server:" + ip + "::" + "parameterValue" + parameterValue);

        // execute HTTP request
        try {
            new HttpRequestAsyncTask(
                    ctx, "=" + parameterValue, ip, ":" + portNumber, "/?pin"
            ).execute();//.get() para esperar
            Toast.makeText(AccessActivity.this,
                    "Comando enviado!",
                    Toast.LENGTH_LONG).show();
            button_OFF.setEnabled(true);
            button_ON.setEnabled(true);
            switch_int.setEnabled(true);
            AccessActivity.busy = false;
            setButton(txtestado.getText().toString());

            return true;
        }
        catch (Exception e) {
            Toast.makeText(AccessActivity.this,
                    "Comando não enviado! Tente novamente",
                    Toast.LENGTH_LONG).show();
            Log.v(TAG, "2-erro no envio: " + e);
            button_OFF.setEnabled(true);
            button_ON.setEnabled(true);
            switch_int.setEnabled(true);
            AccessActivity.busy = false;
            setButton(txtestado.getText().toString());

            return false;
        }

    }
}
