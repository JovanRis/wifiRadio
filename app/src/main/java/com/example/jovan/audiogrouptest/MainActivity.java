package com.example.jovan.audiogrouptest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    InetAddress localInetAddress = null;
    //Log.i("local Address", localInetAddress.toString());

    AudioStream audioStream = null;     //stream do eden klient
    AudioStream audioStream2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            localInetAddress = InetAddress.getByAddress(getLocalIPAddress());
            Log.i("local Address", localInetAddress.toString());
            audioStream = new AudioStream(localInetAddress);    //inicijalizacija na stream
            audioStream2 = new AudioStream(localInetAddress);
        } catch (Exception e) {
        Log.e("----------------------", e.toString());
        e.printStackTrace();
        }


        byte[] adresa = getLocalIPAddress();        //funkcoja za naogajne na lokalnata ipv4 adresa od wlan0

        final TextView localIP = (TextView) findViewById(R.id.txtView_LocalAddress);
        final EditText destAddress = (EditText) findViewById(R.id.txtEdit_destinationAddress);
        final EditText destPort = (EditText) findViewById(R.id.editText_destinationPort);
        final EditText destAddress2 = (EditText) findViewById(R.id.txtEdit_destAddress2);
        final EditText destPort2 = (EditText) findViewById(R.id.txtEdit_destPort2);
        destAddress.setText("192.168.");        //setirajne na pocetnite vrednosti od ip adresata
        destAddress2.setText("192.168.");
        String viewstring = "";
        for(int i=0;i<2;i++){
            viewstring += 256+adresa[i];
            viewstring += ".";
        }

        viewstring += adresa[2]+".";
        viewstring += adresa[3]+":";
        viewstring +=+audioStream.getLocalPort();

        localIP.setText(viewstring);        // prikazuvajne na lokalnata adresa i port za audioStream





        Button button = (Button) findViewById(R.id.btn_startStream);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                       // funkcija koja se povikuva na klik na Start Stream
                String address = destAddress.getText().toString();
                String[] parts = address.split("\\.");
                byte[] byteAddress = new byte[4];
                for(int i=0;i<parts.length;i++){
                    byteAddress[i] = (byte) Integer.parseInt(parts[i]);
                }
                int port = Integer.parseInt(destPort.getText().toString());

                int port2 =0;                       //ovie 2 promenlivi se namesteni po default vo slicaj ako ne e porteben vtor klient
                byte[] byteAddress2 = new byte[4];
                try {                               // ako nema adresa i port za vtor klient ovoj del frla exception

                String address2 = destAddress2.getText().toString();
                String[] parts2 = address2.split("\\.");
                byteAddress2 = new byte[4];
                for(int i=0;i<parts2.length;i++){
                    byteAddress2[i] = (byte) Integer.parseInt(parts2[i]);
                }


                    port2 = Integer.parseInt(destPort2.getText().toString());
                }
                catch (Exception e){
                    Log.e("----------------------", e.toString());
                }
                startStream(byteAddress,port,byteAddress2,port2);       // so ova se startuva stream

            }
        });



    }

    /*
    * funkcija koja go vrsi prenosot na audio
    * na vlez prima parametri za adresa i port na 2 klienti
    * */

    public void startStream(byte[] destinationAddress,int destinationPort,byte[] destinationAddress2,int destinationPort2){
        try {
            AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.setMode(AudioManager.MODE_IN_COMMUNICATION);          //postavuvanje parametri na hardverot za audio (mikrofon i zvucnik)
            AudioGroup audioGroup = new AudioGroup();
            audioGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);       //

            audioStream.setCodec(AudioCodec.PCMU);                      //postavuvanje codec za kodiranje na zvukot
            audioStream.setMode(RtpStream.MODE_NORMAL);                 //postavuvanje protokol za prenos
            //set receiver(vlc player) machine ip address(please update with your machine ip)
            audioStream.associate(InetAddress.getByAddress(new byte[]{destinationAddress[0], destinationAddress[1], destinationAddress[2], destinationAddress[3]}), destinationPort);
            audioStream.join(audioGroup);                               //dodavanje na prviot stream vo grupata

            if(destinationPort2 != 0)                                   // ako e setiran vtoriot port da se dodadi i toj vo audio grupata
            {
                audioStream2.setCodec(AudioCodec.PCMU);
                audioStream2.setMode(RtpStream.MODE_NORMAL);
                audioStream2.associate(InetAddress.getByAddress(new byte[]{destinationAddress2[0], destinationAddress2[1], destinationAddress2[2], destinationAddress2[3]}), destinationPort2);
                audioStream2.join(audioGroup);
            }






        } catch (Exception e) {
            Log.e("----------------------", e.toString());
            e.printStackTrace();
        }
    }

    /* funkcija za zemanje na ipv4 adresa od momentalnata mreza na koja e povrzan klientot
    *  se zema lista od site mrezni interfejsi na telefonot i se odbira ip adresata so
    *  soodvetno ime. WiFi mrezata e pretstavena so wlan0 interfejsot, i od toj interfejs se
    *  zema IPv4 adresata.
    * */
    public static byte[] getLocalIPAddress ()
    {
        byte ip[]=null;
        try {
            boolean breaker = false;
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                if(breaker == true)
                    break;
                NetworkInterface intf = (NetworkInterface)en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress)enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ip= inetAddress.getAddress();
                        String className = inetAddress.getClass().toString();
                        String interfaceName = intf.getName();
                        if(interfaceName.equals("wlan0") && className.equals("class java.net.Inet4Address")){
                            breaker = true;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.i("SocketException ", ex.toString());
        }
        return ip;

    }

    /* koga se zatvora aplikacijata audio hardverot se vraka vo prvobitna sostojba
    *  (inaku nemozi da se koristi glasniot zvucnik)
    * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.setMode(AudioManager.MODE_NORMAL);
    }


}