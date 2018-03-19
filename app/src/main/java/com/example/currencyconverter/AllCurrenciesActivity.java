package com.example.currencyconverter;

/**
 * Activity handles the display of all rates for a selected currency
 * @author Simeon Kostadinov
 * */

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class AllCurrenciesActivity extends AppCompatActivity {

    //local private variables
    private TextView current;
    static private HashMap<String,String> allRates;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getResources().getConfiguration();

        //check orientation and launch appropriate xml
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_all_currencies);
        }else{
            setContentView(R.layout.activity_all_currencies_land);
        }

        //init all UI items
        current = (TextView) findViewById(R.id.current);
        String currentCurrency = getIntent().getStringExtra(MainActivity.CURRENT_CURRENCY_MESSAGE);
        current.setText(getString(R.string.currentCurrencyRate) + getString(R.string.one) + currentCurrency);


    }

    @Override
    public void onResume(){
        super.onResume();
        //call AsyncTask to display all rates
        new GetAllRates().execute();

    }

    /**
     * AsyncTask to fetch all rates for the currency and put them into a ListView
     */
    class GetAllRates extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            //get url from MainActivity
            String urlRate = getIntent().getStringExtra(MainActivity.URL_RATE_MESSAGE);
            allRates = ServerCom.getAllRates(urlRate); // get hashmap of rates from api

            /**
             * handle UI updates so no errors occur
             */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    list = (ListView) findViewById(R.id.list);
                    //custom adapter to handle the hashmap
                    Adapter adapter = new Adapter(allRates);
                    list.setAdapter(adapter);
                }
            });
            return "Success";
        }
    }
}