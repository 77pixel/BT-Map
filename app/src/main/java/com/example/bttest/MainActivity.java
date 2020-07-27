package com.example.bttest;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity implements BleObject.BleListener
{
    Marker[] ListaMarkerow = new Marker[2];
    BleObject bts;
    Bitmap bitmap;
    float bmpPixelM = 0;
    ImageView myImageView;
    double lrssi = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ProgressBar progBar = (ProgressBar) findViewById(R.id.progBar);
        progBar.setVisibility(View.VISIBLE);

        MobileAds.initialize(this, new OnInitializationCompleteListener()
        {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus)
            {
                //Log.d( "vvv",initializationStatus.getAdapterStatusMap().toString());
            }
        });

        //bts = new BleObject(this, this);
        //bts.checkHardware();
        //bts.bluetoothScanning();

        final Marker m0 = new Marker("24:6F:28:29:EA:C2",this, this);
        m0.tv = (TextView) findViewById(R.id.tv1);
        ListaMarkerow[0] = m0;

        final Marker m1 = new Marker("24:6F:28:23:8F:DE",this, this);
        m1.tv = (TextView) findViewById(R.id.tv2);
        ListaMarkerow[1] = m1;

        /*
        Marker m2 = new Marker("B8:27:EB:C9:5D:B4",this, this);
        m2.tv = (TextView) findViewById(R.id.tv3);
        ListaMarkerow[2] = m2;
        */

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mapa);
        bmpPixelM = ((float)bitmap.getHeight()) / 13;

        progBar.setVisibility(View.GONE);
        myImageView = (ImageView) findViewById(R.id.imageView);
        myImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void deviceFound(BluetoothDevice device)
    {
       // Log.d("vvv", "MAM" + device.getAddress().toString());
    }

    @Override
    public void updateRssi(BluetoothDevice device, int rssi)
    {
        for (Marker m : ListaMarkerow)
        {
            if (m.mac.equals(device.getAddress()))
            {
               if (m.policzCykl(rssi))
               {

               }



                if (m.licznik >= 200)
                {


                    m.RSSI = m.sumaRssi/m.licznik;

                    String rs = String.format("%.02f", m.dal());
                    String txt =
                            "mac: "    + device.getAddress() +
                            ", rssi: " + String.format("%.02f", m.RSSI) +
                            ", odl: "  + rs;

                    m.tv.setText(txt);
                    m.licznik  = 0;
                    m.sumaRssi = 0;
                    lrssi = m.RSSI;
                    rysuj();

                    int[] y = new int[10];


                }
                else {

                    m.stopLicz(rssi);
                    m.sumaRssi += rssi;

                    if ( drrssi - 1 > lrssi)
                    {
                       // m.sumaRssi += lrssi;
                       // Log.d("vvv","1111");
                    }
                    else {

                        lrssi = rssi;
                        Log.d("vvv","222");
                    }
                }
            }
        }
    }

    private void rysuj()
    {
        //rysuj
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas tempCanvas = new Canvas(mutableBitmap);
        tempCanvas.drawBitmap(bitmap, 0, 0, null);

        Paint myPaint = new Paint();
        myPaint.setColor(Color.RED);
        tempCanvas.drawCircle( (ListaMarkerow[1].dal() + 6.8F)*bmpPixelM,
                (12-ListaMarkerow[0].dal())*bmpPixelM,
                65,
                myPaint);
        myImageView.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));


    }
}
