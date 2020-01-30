package com.khemraj.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    static  ArrayList<String> list = new ArrayList<String>();;
    static  ArrayList <LatLng> locations = new ArrayList<LatLng>();;
    static ArrayAdapter<String> arrayAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.khemraj.memorableplaces", Context.MODE_PRIVATE);

        ArrayList<String> lattitudes = new ArrayList<String>();
        ArrayList<String> longitudes = new ArrayList<String>();


        lattitudes.clear();
        longitudes.clear();
        list.clear();
        locations.clear();


        try{
            list = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("location",ObjectSerializer.serialize(new ArrayList<String>())));
            lattitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lattitude",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitude",ObjectSerializer.serialize(new ArrayList<String >())));

        }catch (Exception e){
            e.printStackTrace();
        }

        if(lattitudes.size()>0 && longitudes.size()>0 && list.size()>0){
            if(lattitudes.size() == longitudes.size() && lattitudes.size()== list.size()){
                for(int i=0;i<lattitudes.size();i++){
                    locations.add(new LatLng(Double.parseDouble(lattitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
        }else {
            locations.add(new LatLng(0,0));
            list.add("+ Add New Place to Your Favourites");
        }

        listView = findViewById(R.id.listview);


        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,list);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),Map.class);
                intent.putExtra("name",position);
                startActivity(intent);
            }
        });

    }
}
