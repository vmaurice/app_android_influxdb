package com.example.sam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Lum extends AppCompatActivity {

    GraphView graph;

    private static char[] token = "KMLedHKFWBc9c0K5gyMyC0jmqIi1u-A5XoJxoSqjidL4lslZKXKIfl2s87QIQDuS5LUrSRLKlW12b6_nbUUPLw==".toCharArray();
    private static String org = "vincent.maurice@outlook.fr";
    private static String bucket = "sensors";

    Button b_submit;
    Spinner listPast;

    String past = "0";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(Lum.this, Settings.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(Lum.this, About.class));
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

        setTitle("Luminosité");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lum);

        new Thread(new Runnable() {
            public void run() {

                InfluxDBClient influxDBClient = InfluxDBClientFactory.create("https://eu-central-1-1.aws.cloud2.influxdata.com", token, org, bucket);

                //System.out.println(influxDBClient.getBucketsApi().findBuckets());

                List<FluxTable> tables;

                graph = (GraphView) findViewById(R.id.lum_graph);

                while (true) {

                    String flux = "from(bucket: \"sensors\") |> range(start: "+ past + ") |> filter(fn: (r) => r[\"topic\"] == \"sensors/kerno/bedroom/luminosity\")";

                    try {


                        tables = influxDBClient.getQueryApi().query(flux);

                        /*
                        for (FluxTable fluxTable : tables) {
                            List<FluxRecord> records = fluxTable.getRecords();
                            for (FluxRecord fluxRecord : records) {
                                System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
                            }
                        }

                         */



                        graph.removeAllSeries();


                        graph.getViewport().setYAxisBoundsManual(true);
                        graph.getViewport().setMaxY(200);

                        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getBaseContext(), DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT, new Locale("FR", "fr"))));


                        //graph.getGridLabelRenderer().setNumHorizontalLabels(3);
                        graph.getGridLabelRenderer().setHorizontalLabelsAngle(45);

                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();


                        for (FluxTable fluxTable : tables) {
                            List<FluxRecord> records = fluxTable.getRecords();
                            for (FluxRecord fluxRecord : records) {
                                Date d = new Date(fluxRecord.getTime().toEpochMilli());
                                series.appendData( new DataPoint(d,(double)fluxRecord.getValueByKey("_value")),true,records.size());
                            }
                        }



                        graph.addSeries(series);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        java.util.concurrent.TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                //influxDBClient.close();

            }
        }).start();

        this.listPast = (Spinner) findViewById(R.id.lum_spinner);
        this.b_submit = (Button) findViewById(R.id.lum_button);

        listPast.setSelection(7);

        b_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = listPast.getSelectedItemPosition();
                if (i == 0)
                    past = "-5m";
                else if (i == 1)
                    past = "-15m";
                else if (i == 2)
                    past = "-30m";
                else if (i == 3)
                    past = "-1h";
                else if (i == 4)
                    past = "-4h";
                else if (i == 5)
                    past = "-12h";
                else if (i == 6)
                    past = "-24h";
                else
                    past = "0";

                System.out.println(">>> search before : " + (String)past);
            }
        });


    }
}