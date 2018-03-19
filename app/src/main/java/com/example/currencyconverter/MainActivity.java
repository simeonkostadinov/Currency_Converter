package com.example.currencyconverter;

/**
 * MainActivity class that does all the functionality the program needs
 * @author Simeon Kostadinov
 * */

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    //final variables
    public static String URL_RATE_MESSAGE = "url rate message";
    public static String CURRENT_CURRENCY_MESSAGE = "current currency message";

    //local private variables
    private Button showRates;
    private TextView forDate;
    private TextView rateInfo;
    private Button convert;
    private Button pickDate;
    private Button switchButton;
    private TextView date;
    private TextView newAmount;
    private EditText amount;
    private Spinner amountSpinner;
    private Spinner newAmountSpinner;
    private TextView to;
    private double amountToConvert;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private int year;
    private int day;
    private int month;
    private SimpleDateFormat formatter;
    private String currentDate;
    private final String PATTERN = "yyyy-MM-dd";
    private static ArrayList<String> spinnerValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getResources().getConfiguration();

        //check if device is connected, if not, force stop the app
        if(!ServerCom.isNetworkAvailable(this)) {
           // Toast.makeText(this, "No Internet connection", Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
            finish();
            System.exit(0);
        }

        //check orientation and launch appropriate xml
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_main);
        }else{
            setContentView(R.layout.activity_main_land);
        }

        //init all UI items
        showRates = (Button) findViewById(R.id.showRates);
        switchButton = (Button) findViewById(R.id.switchButton);
        rateInfo = (TextView) findViewById(R.id.rateInfo);
        forDate = (TextView) findViewById(R.id.forDate);
        date = (TextView) findViewById(R.id.date);
        newAmount = (TextView) findViewById(R.id.newAmount);
        amount = (EditText) findViewById(R.id.amount);
        amountSpinner = (Spinner) findViewById(R.id.amountSpinner);
        newAmountSpinner = (Spinner) findViewById(R.id.newAmountSpinner);
        to = (TextView) findViewById(R.id.to);
        convert = (Button) findViewById(R.id.convert);
        pickDate = (Button) findViewById(R.id.pickDate);

        //call AsyncTask to populate spinners with values
        new PopulateSpinner().execute();

        //set initial date for getting rates to current date
        Date current = new Date();
        formatter = new SimpleDateFormat(PATTERN);
        currentDate = formatter.format(current); //formatter for url
        date.setText(currentDate);

        /**
         * pickDate creates a DatePicker and allows for a date to be picked
         */
        pickDate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        /**
         * Listener to capture the date from the DatePicker and also set the date textview
         */
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String selectedDate;
                if(month<10){
                    currentDate = year + "-" + "0" + month + "-" + dayOfMonth;
                    selectedDate= year + "-" + "0" + month + "-" + dayOfMonth;
                }else{
                    currentDate = year + "-" + month + "-" + dayOfMonth;
                    selectedDate= year + "-" + month + "-" + dayOfMonth;
                }

                date.setText(selectedDate);
                convert.performClick();
            }
        };

        /**
         * convert converts gets the value from the input field and calls the
         * AsyncTask to convert to the new selected rate
         */
        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            newAmount.setText("converting..."); // for when it has delays fetching from api
            //check if there is input
            if(amount.getText().toString().trim().length() > 0 ){
                try{
                    String amountValue = amount.getText().toString();
                    amountToConvert = Double.parseDouble(amountValue);
                    new ConvertTask().execute(); //task does the conversion and updates variables
                    new RateInfo().execute(); //updates rateInfo text to rate for currency
                }catch (NumberFormatException ex){
                    amount.setError("Not a number! Enter valid number");
                    newAmount.setText("");
                }
            }else {
                amount.setError("You need to enter a number!");
            }
            }
        });

        /**
         * showRates calls method showAllRates that opens a new Activity based on url
         */
        showRates.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String url = "http://api.fixer.io/" + currentDate + "?base=" + amountSpinner.getSelectedItem().toString();
                showAllRates(url, amountSpinner.getSelectedItem().toString());
            }
        });

    }

    /**
     * RateInfo returns the current exchange rate between the selected currencies
     */
    class RateInfo extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            Double newRate = getRate();
            String text;
            if(amountSpinner.getSelectedItem().toString().equals(newAmountSpinner.getSelectedItem().toString())){
                text = "1 " + amountSpinner.getSelectedItem().toString() + " = "
                        + "1" + " " + newAmountSpinner.getSelectedItem().toString();
            }else{
                 text = "1 " + amountSpinner.getSelectedItem().toString() + " = "
                    + newRate + " " + newAmountSpinner.getSelectedItem().toString();
            }
            rateInfo.setText(text);
            return "Success";
        }
    }

    /**
     * PopulateSpinner populates both spinners from a jsonObject returned from the api
     */
    class PopulateSpinner extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            spinnerValues = spinnerValues(); // call to method that returns the values

            //init addapter to store values
            final ArrayAdapter<String> adapt = new ArrayAdapter<String>(MainActivity.this,
                    R.layout.support_simple_spinner_dropdown_item,spinnerValues);
            adapt.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            /**
             * updating the UI so no errors occur
             */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    amountSpinner.setAdapter(adapt);
                    newAmountSpinner.setAdapter(adapt);

                    /**
                     * switchButton switches the currencies i.e switching the spinner values
                     * between each other and does the conversion again for the new input
                     */
                    switchButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            int pos1 = amountSpinner.getSelectedItemPosition();
                            int pos2 = newAmountSpinner.getSelectedItemPosition();
                            amountSpinner.setSelection(pos2);
                            newAmountSpinner.setSelection(pos1);
                            if(!newAmount.getText().toString().equalsIgnoreCase("converting...")){
                                amount.setText(newAmount.getText());
                            }
                            convert.performClick();
                        }
                    });
                }
            });

            return "Success";
        }
    }

    /**
     * ConvertTask handles the conversion from one rate to another
     */
    class ConvertTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            //obtaining the converted rate with a call to the convert method
            Double rounded = Math.round(convert(amountToConvert)*100D)/100D;
            final String text = rounded.toString();

            /**
             * updating the UI so no errors occur
             */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    newAmount.setText(text);
                }
            });

            return "Success";
        }
    }

    /**
     * Method converts the input currency to the selected currency based on rates from api
     * @param amount the amount to be converted
     * @return Double the converted amount
     */
    private Double convert(Double amount){
        try {
            //get url based on date and initial currency
            String url = "http://api.fixer.io/" + currentDate + "?base=" + amountSpinner.getSelectedItem().toString();
            //handle the json parsing
            JSONObject jo = new JSONObject(ServerCom.getJSONfromURL(url));
            JSONObject joRates = jo.getJSONObject("rates");
            //extract the rate value needed for the conversion
            Double rate = joRates.getDouble(newAmountSpinner.getSelectedItem().toString());
            //convert
            amount = amount*rate;
            return amount;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return amount;
    }

    /**
     * Gets current rate based on the date and initial currency
     * @return Double the rate that is looked for
     */
    private Double getRate(){
        Double rate = 0.0;
        try {
            //get url based on date and initial currency
            String url = "http://api.fixer.io/" + currentDate + "?base="
                    + amountSpinner.getSelectedItem().toString();
            //handle json parsing
            JSONObject jo = new JSONObject(ServerCom.getJSONfromURL(url));
            JSONObject joRates = jo.getJSONObject("rates");
            //extract the rate needed
            rate = joRates.getDouble(newAmountSpinner.getSelectedItem().toString());
            return rate;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rate;
    }

    /**
     * Retrieves spinner values based on all the currencies the api offers
     * @return ArrayList of spinner values
     */
    private ArrayList<String> spinnerValues(){
        ArrayList<String> values = new ArrayList<String>();
        try {
            //use any url, only the currencies are needed
            String url = "http://api.fixer.io/latest?";
            //handle json parsing
            JSONObject jo = new JSONObject(ServerCom.getJSONfromURL(url));
            JSONObject rates= jo.getJSONObject("rates");
            Iterator<?> keys = rates.keys(); //to iterate the json Object
            values.add(jo.getString("base")); //add the base since it`s not in rates
            while(keys.hasNext()){
                String key = (String) keys.next();
                values.add(key); //the keys are the currencies
            }
            return values;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * Makes an intet and starts a new activity that displays all rates for the selected currency
     * @param urlRate url for the rates of the currency to be retrieved
     * @param currentCurrency the currencies for which rates should be retrieved
     */
    private void showAllRates(String urlRate, String currentCurrency) {
        Intent intent = new Intent(this, AllCurrenciesActivity.class);
        intent.putExtra(URL_RATE_MESSAGE, urlRate);
        intent.putExtra(CURRENT_CURRENCY_MESSAGE, currentCurrency);
        startActivity(intent);
    }
}

