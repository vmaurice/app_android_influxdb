package com.example.sam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static char[] token = "KMLedHKFWBc9c0K5gyMyC0jmqIi1u-A5XoJxoSqjidL4lslZKXKIfl2s87QIQDuS5LUrSRLKlW12b6_nbUUPLw==".toCharArray();
    private static String org = "vincent.maurice@outlook.fr";
    private static String bucket = "sensors";

    Button bt, bt2;

    protected TextView txt_temp, txt_lum;
    protected String temp_value = "";
    protected String lum_value = "";

    private TextView batteryTxt;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryTxt.setText("Pourcentage de la batterie " +String.valueOf(level) + "%");
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, Settings.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, About.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.Theme_Sam_Dark);
        else
            setTheme(R.style.Theme_Sam);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("My application");

        batteryTxt = this.findViewById(R.id.home_battery);

        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


        this.bt = (Button) this.findViewById(R.id.home_but_tmp);
        this.bt2 = (Button) this.findViewById(R.id.home_but_lum);

        this.txt_temp = this.findViewById(R.id.home_txt_tmp);
        this.txt_lum = this.findViewById(R.id.home_txt_lum);


        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, Temps.class));
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, Lum.class));
            }
        });

        new Thread(new Runnable() {
            public void run() {

                InfluxDBClient influxDBClient = InfluxDBClientFactory.create("https://eu-central-1-1.aws.cloud2.influxdata.com", token, org, bucket);

                //System.out.println(influxDBClient.getBucketsApi().findBuckets());

                List<FluxTable> tables;


                while (true) {

                    try {

                        String flux = "from(bucket: \"sensors\") |> range(start: 0) |> last() |> filter(fn: (r) => r[\"topic\"] == \"sensors/kerno/bedroom/temperature\")";


                        tables = influxDBClient.getQueryApi().query(flux);


                        for (FluxTable fluxTable : tables) {
                            List<FluxRecord> records = fluxTable.getRecords();
                            for (FluxRecord fluxRecord : records) {
                                //System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
                                temp_value = fluxRecord.getValueByKey("_value").toString();
                            }
                        }

                        System.out.println("La température est de " + temp_value + "°C");


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txt_temp.setText("La température est de " + temp_value + "°C");
                            }
                        });


                        flux = "from(bucket: \"sensors\") |> range(start: 0) |> last() |> filter(fn: (r) => r[\"topic\"] == \"sensors/kerno/bedroom/luminosity\")";


                        tables = influxDBClient.getQueryApi().query(flux);


                        for (FluxTable fluxTable : tables) {
                            List<FluxRecord> records = fluxTable.getRecords();
                            for (FluxRecord fluxRecord : records) {
                                //System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
                                lum_value = fluxRecord.getValueByKey("_value").toString();
                            }
                        }

                        System.out.println("La luminosité est de " + lum_value + " lux");


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txt_lum.setText("La luminosité est de " + lum_value + " lux");
                            }
                        });

                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }



                }



                //influxDBClient.close();

            }
        }).start();

    }
}