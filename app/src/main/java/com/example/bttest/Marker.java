package com.example.bttest;

import android.content.Context;
import android.widget.TextView;

import java.util.OptionalDouble;
import java.util.ArrayList;


class Marker
{
    double pozX;
    double pozY;
    double RSSI;
    String mac = "";
    int licznik = 0;
    double sumaRssi = 0;
    int dlTablicy = 200;
    private ArrayList<Integer> tablicaRSSI = new ArrayList<>();

    private BleObject obj;
    TextView tv;

    private void sredRSSI()
    {

        OptionalDouble avg = tablicaRSSI.stream().mapToDouble(i -> i).average();
        //System.out.println("Average = " + avg.getAsDouble());


    }

    boolean policzCykl(int _rssi)
    {
        tablicaRSSI.add(licznik, _rssi);
        licznik++;
        if (licznik > dlTablicy)
        {
            sredRSSI();
            licznik = 0;
            return true;
        }

        return false;
    }

    Marker(String macS, Context context, BleObject.BleListener listener)
    {
        mac = macS;
        obj = new BleObject(context, listener);
        obj.checkHardware();
        obj.connect(macS);
    }

    float dal()
    {
        return (float) Math.pow(10d, (-68.5d - this.RSSI) / 20);
    }
}
