package com.example.currencyconverter;

/**
 * Utility class to handle server communications
 * @author Simeon Kostadinov
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class ServerCom {

    /**
     * Method that gets a JSONObject as a String from the api request
     * @param url url for the specific date and currency
     * @return a JSONObject as a String
     */
    public static String getJSONfromURL(String url){
        HttpURLConnection connection = null;
        try{
            URL u = new URL(url);
            //connection management
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-length","0");
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            //connection management - end

            //status if the connection is successful or not
            int status = connection.getResponseCode();
            switch (status){
                case 200:
                case 201:
                    //BufferedReader to read the input stream
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    //StringBuilder to parse the input to a String so it can be used
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while((line=reader.readLine())!=null){
                        builder.append(line).append("\n");
                    }
                    reader.close();
                    return builder.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            //close the connection
            if(connection != null){
                try{
                    connection.disconnect();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Method to retriev a hashmap of all rates from the JSONObject
     * @param url the url of the currencie for which rates should be retrieved
     * @return a HashMap with all currencies and rates for the selected currency
     */
    public static HashMap<String,String> getAllRates(String url){
        HashMap<String,String> values = new HashMap<String,String>();
        try {
            //parse JSON string
            JSONObject jo = new JSONObject(ServerCom.getJSONfromURL(url));
            JSONObject rates= jo.getJSONObject("rates");
            Iterator<?> keys = rates.keys(); // to iterate the json object
            while(keys.hasNext()){
                //adding to hasmap both keys and values
                String key = (String) keys.next();
                String value = rates.getString(key);
                values.put(key,value);
            }
            return values;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * Method checks if connection is made or not
     * @param context the context for which it`s called
     * @return boolean true if connected, false if not
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
    }

}
