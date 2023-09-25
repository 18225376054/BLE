package com.example.android_park;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.android_park.ui.my.MyFragment;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

public class UdpReceiverThread extends Thread {
    private boolean isRunning;
    private DatagramSocket socket;
    private byte[] buffer;
    private int port;


    public static String xValue = "106.552870";
    public static String yValue = "29.742240";

    public static String x = "0";
    public static String y = "0";




    public UdpReceiverThread(int port) {
        this.port = port;
        buffer = new byte[1024];
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void run() {
        isRunning = true;
        try {
            socket = new DatagramSocket(port);


            while (isRunning) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());

                if (!message.equals("")){
                    // 提取 "longitude" 对应的数值
                    int xStartIndex = message.indexOf("\"longitude\"") + 12; // 找到 "longitude" 的起始索引，加上字符数跳过冒号和空格
                    int xEndIndex = message.indexOf(',', xStartIndex); // 找到逗号的索引，表示数值的结束位置
                    xValue = message.substring(xStartIndex, xEndIndex).trim().replaceAll("\\s", "");

                    // 提取 "latitude" 对应的数值
                    int yStartIndex = message.indexOf("\"latitude\"") + 12; // 找到 "latitude" 的起始索引，加上字符数跳过冒号和空格
                    int yEndIndex = message.indexOf(',', yStartIndex);
                    yValue = message.substring(yStartIndex, yEndIndex).trim().replaceAll("\\s", "");

                    // 提取 "x" 对应的数值
                    int xStart = message.indexOf("\"x\"") + 5; // 找到 "latitude" 的起始索引，加上字符数跳过冒号和空格
                    int xEnd = message.indexOf(',', xStart);
                    x = message.substring(xStart, xEnd).trim().replaceAll("\\s", "");

                    // 提取 "y" 对应的数值
                    int yStart = message.indexOf("\"y\"") + 5; // 找到 "latitude" 的起始索引，加上字符数跳过冒号和空格
                    int yEnd = message.indexOf(',', yStart);
                    y = message.substring(yStart, yEnd).trim().replaceAll("\\s", "");

                    MyFragment.textView_my.setText(xValue + "," + yValue + "  ;  " + x + "," + y);
                    //MyFragment.textView_my.setText(message);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public void stopReceiver() {
        isRunning = false;
        interrupt();
    }
}
