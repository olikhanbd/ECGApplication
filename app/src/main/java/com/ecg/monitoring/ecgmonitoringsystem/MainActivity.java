package com.ecg.monitoring.ecgmonitoringsystem;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    GraphicalView mChart;
    int i = 0, cnt = 0;
    int byteCount, value, readBufferPosition;
    byte[] readBuffer;
    LinearLayout chartContainer;
    XYSeries visitsSeries;
    XYMultipleSeriesDataset dataset;
    Calendar calendar = Calendar.getInstance();
    String string;
    XYSeriesRenderer visitsRenderer;
    XYMultipleSeriesRenderer multiRenderer;
    Button b1, b2, b3, b4, b5, b6;
    BluetoothDevice device;
    BluetoothSocket socket;
    Thread thread;
    OutputStream outputStream;
    InputStream inputStream;
    boolean gotYou, Connected, stopThread;
    EditText mailAddr, nameOfPreshent;
    Bitmap bitmap1;
    TextView tvw;
    String mailAddrS, mailSub;
    Uri imageUri;
    String root;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button) findViewById(R.id.button);
        b2 = (Button) findViewById(R.id.button2);
        b3 = (Button) findViewById(R.id.button3);
        b4 = (Button) findViewById(R.id.button4);
        b5 = (Button) findViewById(R.id.button6);
        b6 = (Button) findViewById(R.id.button5);
        mailAddr = (EditText) findViewById(R.id.editText);
        Connected = gotYou = false;
        BA = BluetoothAdapter.getDefaultAdapter();

        tvw = (TextView) findViewById(R.id.textView2);
        nameOfPreshent = (EditText) findViewById(R.id.editText2);
        chartContainer = (LinearLayout) findViewById(R.id.chart_container);

        setupChart();
    }


    void getInfo() {
        mailAddrS = mailAddr.getText().toString();

        mailSub = nameOfPreshent.getText().toString();


    }


    private void setupChart() {


        // Creating an  XYSeries for Visits
        visitsSeries = new XYSeries("ECG");

        // Creating a dataset to hold each series
        dataset = new XYMultipleSeriesDataset();
        // Adding Visits Series to the dataset
        dataset.addSeries(visitsSeries);

        // Creating XYSeriesRenderer to customize visitsSeries
        visitsRenderer = new XYSeriesRenderer();
        visitsRenderer.setColor(Color.BLACK);
        //        visitsRenderer.setPointStyle(PointStyle.CIRCLE);
        //        visitsRenderer.setFillPoints(true);
        visitsRenderer.setLineWidth(1);

        visitsRenderer.setDisplayChartValues(false);

        // Creating a XYMultipleSeriesRenderer to customize the whole chart
        multiRenderer = new XYMultipleSeriesRenderer();

        multiRenderer.setChartTitle("ECG");
        multiRenderer.setXTitle("point");
        multiRenderer.setYTitle("amplitude");
        multiRenderer.setZoomButtonsVisible(false);
        multiRenderer.setShowLegend(false);
        multiRenderer.setShowGrid(true);
        multiRenderer.setGridColor(Color.RED);
        multiRenderer.setXLabels(100);
//                multiRenderer.setInScroll(true);

        multiRenderer.setYLabels(100);
        multiRenderer.setShowLabels(false);
        multiRenderer.getOrientation();
        multiRenderer.setXAxisMin(0);
        multiRenderer.setXAxisMax(400);

        multiRenderer.setYAxisMin(150);
        multiRenderer.setYAxisMax(600);
//                multiRenderer.setBarSpacing(2);


        // Adding visitsRenderer to multipleRenderer
        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
        // should be same
        multiRenderer.addSeriesRenderer(visitsRenderer);

        // Getting a reference to LinearLayout of the MainActivity Layout


        mChart = (GraphicalView) ChartFactory.getLineChartView(this, dataset, multiRenderer);
        mChart.setBackgroundColor(Color.WHITE);

        // Adding the Line Chart to the LinearLayout
        chartContainer.addView(mChart);


    }


    public void on(View v) {
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turning on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v) {
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turning off", Toast.LENGTH_LONG).show();
    }

    //    public  void visible(View v){
//        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        startActivityForResult(getVisible, 0);
//    }
    public void stopData(View v) {
        if (!stopThread) {
            stopThread = true;

            // thread.stop();
            try {
                socket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "YOU ARE NOT CONNECTED", Toast.LENGTH_SHORT).show();
                Connected = false;
            }
        }

    }

    void saveGraph() {
        mChart.setDrawingCacheEnabled(true);
        bitmap1 = Bitmap.createBitmap(mChart.getDrawingCache());
        mChart.setDrawingCacheEnabled(false);
        root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/DCIM");
        myDir.mkdirs();
        //int date,month,year,hour,min,sec;
        String fname = nameOfPreshent.getText() + ".PNG";
        File file = new File(myDir, fname);
        Toast.makeText(getApplicationContext(), "saving", Toast.LENGTH_SHORT).show();
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap1.compress(Bitmap.CompressFormat.PNG, 100, out);

            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    public void startData(View v) {

        saveGraph();

        File myDir1 = new File(root + "/DCIM" + "/" + nameOfPreshent.getText() + ".PNG");
        getInfo();
        String EmailAddrs[] = {mailAddrS};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, EmailAddrs);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mailSub + "  daily Report");
        emailIntent.setType("image/jpeg");
        //imageUri = Uri.fromFile(myDir1);
        imageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", myDir1);
        emailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        startActivity(emailIntent);


    }

    public void list(View v) {
        if (!BA.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Blutooth off plz turn on first", Toast.LENGTH_LONG).show();
        } else {
            pairedDevices = BA.getBondedDevices();

            for (BluetoothDevice iterator : pairedDevices) {
                //String targetDevicename = "H-C-2010-06-01";
                String targetDevicename = "RIMON";
                if (iterator.getName().equals(targetDevicename)) {
                    Toast.makeText(getApplicationContext(), "GOT YOU", Toast.LENGTH_SHORT).show();
                    gotYou = true;
                    device = iterator;
                    break;
                }
            }

            if (gotYou) {

                Toast.makeText(getApplicationContext(), "GOT YOU TRUE", Toast.LENGTH_SHORT).show();
                Connected = true;
                try {
                    socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
                    socket.connect();

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "YOU ARE NOT CONNECTED", Toast.LENGTH_SHORT).show();
                    Connected = false;
                }
                if (Connected) {

                    cnt();
                    beginListenForData();
                }
            }

        }

    }


    void cnt() {
        Toast.makeText(getApplicationContext(), "YOU ARE CONNECTED", Toast.LENGTH_SHORT).show();
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Cant get inp", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Cant get opt", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10;//This is the ASCII code for a newline character
        readBufferPosition = 0;
        stopThread = false;
        readBuffer = new byte[1024];
        //buffer = new byte[1024];
        thread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        byteCount = inputStream.available();

                        if (byteCount >= 0) {

//                            byte rByte[]= new byte[byteCount];
//                            string = rByte.toString();
//                            if(string.charAt(0) == 'r') {
//


                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            for (int count = 0; count < byteCount; count++) {

                                byte b = rawBytes[count];
                                Log.e("b", b+"");
                                if (b == delimiter) {

                                    Log.e("hello", "hello");

                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    string = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
//                        string = new String(rawBytes, "UTF-8");
//                        tvw.setText(rawBytes.toString());

//                            }
                                    handler.post(new Runnable() {
                                        public void run() {


                                            if (string.length() > 2) {

                                                value = Integer.parseInt(string.trim());
//                                    tvw.setText("A="+value +'\n'+ "cnt=" + i);
                                                i++;
//
//                                            tvw.setText("A=" + string + '\n' + "cnt=" + i);
//                                            i++;
//                                        }
//                                    if(string.length()<4) {

                                                if (i >= 380) {

                                                    multiRenderer.setXAxisMin(i - 380);
                                                    multiRenderer.setXAxisMax(i + 20);
                                                }
                                                visitsSeries.add(i, value);

                                                mChart.repaint();
                                            }
//                                    else{
//                                        tvw.setText("wrong Data" );
//                                       // Toast.makeText(getApplicationContext(),"too much",Toast.LENGTH_SHORT).show();
//                                    }
//                                    Toast.makeText(getApplicationContext(),string,Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    //Log.e("readbuffer", readBufferPosition+"");
                                    //Log.e("b", b+"");
                                    readBuffer[readBufferPosition++] = b;
                                }

                            }
                        }
                    } catch (IOException ex) {
                        Toast.makeText(getApplicationContext(), "can't read", Toast.LENGTH_SHORT).show();
                        // stopThread = true;
                    }


                }
            }
        });

        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    public void save(View view) {


        saveGraph();

    }


}
