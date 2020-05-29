package com.example.coronaupdater;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ImageButton syncButton;
    private String link = "https://www.worldometers.info/coronavirus/";
    HashMap<Integer,Country> countryList = new HashMap<>();
    private TextInputEditText countryTextInput;
    private ImageButton addCountryButton;

    TextView textView;

    ListView myListView;
    ArrayList<RowItem> myRowItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        myRowItems = new ArrayList<RowItem>();
        myListView = (ListView) findViewById(R.id.countryListView);
        syncButton = findViewById(R.id.syncButton);
        countryTextInput = findViewById(R.id.countryTextInput);
        addCountryButton = findViewById(R.id.addCountryButton);

        textView = findViewById(R.id.textView);

        loadData();
        updateIndex();
        fillArrayList( );

      //  CustomAdapter myAdapter = new CustomAdapter(getApplicationContext(), myRowItems);
      //  myListView.setAdapter( myAdapter );


        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncButton.setBackgroundColor(Color.RED);
                updateIndex();
                refreshData();


            }
        });

        addCountryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               // String countryName = countryTextInput.getText().toString();
                Country newCountry = new Country(countryTextInput.getText().toString(), -1);
                new FindCountryIndexAddToListAndRefresh().execute(newCountry);
                closeKeyboard();
                countryTextInput.setText("");

            }
        });
    }

    private void fillArrayList() {

        createRows();
        refreshData();


    }


    private void saveData(){
        String savedIndexes = "";
        for(Map.Entry<Integer, Country> country : countryList.entrySet()){
            savedIndexes = savedIndexes.concat(country.getValue().index + ";");
        }
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CountrysIndex", savedIndexes.substring(0,savedIndexes.length()-1));
        editor.apply();

        String savedNames = "";
        for(Map.Entry<Integer, Country> country : countryList.entrySet()){
            savedNames = savedNames.concat(country.getValue().name + ";");
        }
        editor.putString("CountrysName", savedNames.substring(0,savedNames.length()-1));
        editor.apply();

        String savedValues = "";
        for(Map.Entry<Integer, Country> country : countryList.entrySet()){
            savedValues = savedValues.concat(country.getValue().getTote() + "_" + country.getValue().getInfizierte() + ";");
        }
        editor.putString("CountrysValue", savedValues.substring(0,savedValues.length()-1));
        editor.apply();


    }

    private void loadData(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String dataIndex = sharedPreferences.getString("CountrysIndex","13;12");
        String dataName = sharedPreferences.getString("CountrysName","Germany;France");
        String[] countrysIndex = dataIndex.split(";");
        String[] countrysName = dataName.split(";");
        for(int i= 0; i<countrysIndex.length;i++){
            Country country1 = new Country(countrysName[i],Integer.parseInt(countrysIndex[i]));
            countryList.put(countryList.size(), country1);
        }
        String dataValues = sharedPreferences.getString("CountrysValue","0_0;0_0;0_0");
        textView.setText(dataValues);
    }

    private void updateIndex(){
        for(Map.Entry<Integer, Country> country : countryList.entrySet()){
            new FindCountryIndexAndUpdate().execute(country.getValue());
        }
    }



    /**For every entry in countryList it creates a empty row
     *
     * precondition: True
     * postcondition: countryList.size() rows were added to the listView
     *
     */
    private void createRows() {
        for(Map.Entry<Integer, Country> country : countryList.entrySet()){
            RowItem row_one = new RowItem( );
            String countryName = "countryflag_" + country.getValue().name.toLowerCase().replace(" ","").replace("-", "").replace(".","") ;
            myRowItems.add(changeFlag(row_one,countryName));
        }
    }

    private RowItem changeFlag(RowItem country, String name){
        country.setFlag(getResources().getIdentifier(name,"drawable",this.getPackageName()));
        return country;
    }

    /**
     * refreshes Data of table
     *
     * precondition: Rows are already created
     * postcondition: Data is updated
     */
    private void refreshData() {
        for(Map.Entry<Integer, Country> country : countryList.entrySet()){
            new DownloadAndSetData().execute(country.getValue().index, country.getKey());
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * AsynkTask that fetches online data and updates numbers in row
     *
     * precondition: first argument hast to be the country Index, second argument is the row index
     * postcondition: data is updated in certain row
     *
     */
    class DownloadAndSetData extends AsyncTask<Integer, Integer, ArrayList<String>> {
        protected ArrayList<String> doInBackground(Integer... countryIndex) {
            ArrayList<String> data = new ArrayList<>();
            try {
                Document doc = Jsoup.connect(link).get();
                Elements row = doc.getElementsByTag("tr");
                Elements countryinformation = row.get(countryIndex[0]).children();

                for(Element info : countryinformation){
                    data.add(info.text());
                }
                data.add(countryIndex[1].toString());
                return data;
            } catch (IOException e) {


            }


            return data;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(ArrayList<String> result) {
            if(result.isEmpty()){
                Toast.makeText(MainActivity.this, "Failed to connect", Toast.LENGTH_LONG).show();
                syncButton.setBackgroundColor(Color.GREEN);
            }else {
                syncButton.setBackgroundColor(Color.GREEN);
                RowItem row = myRowItems.get(Integer.parseInt(result.get(result.size()-1)));
                row.setTote(result.get(3));
                row.setInfizierte(result.get(1));
                countryList.get(Integer.parseInt(result.get(result.size()-1))).setInfizierte(result.get(1));
                countryList.get(Integer.parseInt(result.get(result.size()-1))).setTote(result.get(3));


                CustomAdapter myAdapter = new CustomAdapter(getApplicationContext(), myRowItems);
                myListView.setAdapter( myAdapter );

            }
        }
    }

    class FindCountryIndexAddToListAndRefresh extends AsyncTask<Country, Integer, Country> {
        protected Country doInBackground(Country...countrys) {
            Document doc = null;
            try {
                doc = Jsoup.connect(link).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements row = doc.getElementsByTag("tr");
            int i = 0;
            for(Element country: row){
                if(country.children().get(0).text().equals(countrys[0].name)){
                    countrys[0].index = i;
                    break;
                }
                i++;
            }
            return countrys[0];

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Country result) {
            countryList.put(countryList.size(), result);
            RowItem row_one = new RowItem( );
            String countryName = "countryflag_" + result.name.toLowerCase().replace(" ","").replace("-", "").replace(".","") ;
            myRowItems.add(changeFlag(row_one,countryName));
            new DownloadAndSetData().execute(result.index, countryList.size()-1);
            saveData();
        }
    }

    class FindCountryIndexAndUpdate extends AsyncTask<Country, Integer, Integer> {
        protected Integer doInBackground(Country...countrys) {
            Document doc = null;
            try {
                doc = Jsoup.connect(link).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements row = doc.getElementsByTag("tr");
            int i = 0;
            for(Element country: row){
                if(country.children().get(0).text().equals(countrys[0].name)){
                    countrys[0].index = i;
                    break;
                }
                i++;
            }
            return i;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Integer result) {

        }
    }


    /**Returns Index of country from the online table
     *
     * precondition: countryName has to exist in the list
     * postcondition: correct index is returned
     *
     * @param countryName
     * @return
     * @throws IOException
     */
    protected int getCountryIndex(final String countryName) throws IOException {
        Document doc = Jsoup.connect(link).get();
        Elements row = doc.getElementsByTag("tr");

        int i = 0;
        for(Element country: row){
            if(country.children().get(0).text().equals(countryName)){
                break;
            }
            i++;
        }
        return i;
    }


}
