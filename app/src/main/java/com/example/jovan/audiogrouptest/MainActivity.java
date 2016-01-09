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

    AudioStream audioStream = null;
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
            audioStream = new AudioStream(localInetAddress);
            audioStream2 = new AudioStream(localInetAddress);
        } catch (Exception e) {
        Log.e("----------------------", e.toString());
        e.printStackTrace();
        }


        byte[] adresa = getLocalIPAddress();

        final TextView localIP = (TextView) findViewById(R.id.txtView_LocalAddress);
        final EditText destAddress = (EditText) findViewById(R.id.txtEdit_destinationAddress);
        final EditText destPort = (EditText) findViewById(R.id.editText_destinationPort);
        destAddress.setText("192.168.0.105");
        String viewstring = "";
        for(int i=0;i<2;i++){
            viewstring += 256+adresa[i];
            viewstring += ".";
        }

        viewstring += adresa[2]+".";
        viewstring += adresa[3]+":";
        viewstring +=+audioStream.getLocalPort();

        localIP.setText(viewstring);





        Button button = (Button) findViewById(R.id.btn_startStream);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = destAddress.getText().toString();
                String[] parts = address.split("\\.");
                byte[] byteAddress = new byte[4];
                for(int i=0;i<parts.length;i++){
                    byteAddress[i] = (byte) Integer.parseInt(parts[i]);
                }
                int port = Integer.parseInt(destPort.getText().toString());
                startStream(byteAddress,port);
                byte[] testaddr = new byte[]{(byte)192, (byte)168, (byte)0, (byte)105 };
                startStream(testaddr,22222);

            }
        });



    }

    public void startStream(byte[] destinationAddress,int destinationPort){
        try {
            AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
            AudioGroup audioGroup = new AudioGroup();
            audioGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);

            audioStream.setCodec(AudioCodec.PCMU);
            audioStream.setMode(RtpStream.MODE_NORMAL);
            //set receiver(vlc player) machine ip address(please update with your machine ip)
            audioStream.associate(InetAddress.getByAddress(new byte[]{destinationAddress[0], destinationAddress[1], destinationAddress[2], destinationAddress[3]}), destinationPort);
            audioStream.join(audioGroup);

            /*audioStream2.setCodec(AudioCodec.PCMU);
            audioStream2.setMode(RtpStream.MODE_NORMAL);
            //set receiver(vlc player) machine ip address(please update with your machine ip)
            audioStream2.associate(InetAddress.getByAddress(new byte[]{(byte)192, (byte)168, (byte)0, (byte)105 }), 22222);
            audioStream2.join(audioGroup);*/




        } catch (Exception e) {
            Log.e("----------------------", e.toString());
            e.printStackTrace();
        }
    }


    public static byte[] getLocalIPAddress () {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.setMode(AudioManager.MODE_NORMAL);
    }


}