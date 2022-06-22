package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/*
 * A simple {@link Fragment} subclass.
 * Use the {@link GruplarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class GruplarFragment extends Fragment {

    private View grupCerceveView;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String>grupListeleri = new ArrayList<>();

    //firebase
    private DatabaseReference grupYolu;

    public GruplarFragment(){

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       grupCerceveView =  inflater.inflate(R.layout.fragment_gruplar, container, false);

       //firebase
        grupYolu = FirebaseDatabase.getInstance().getReference().child("Gruplar");

       //tanımlamalar
        listView = grupCerceveView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1, grupListeleri);
        listView.setAdapter(arrayAdapter);

        //grupları alma metodu
        GruplariAlGoster();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String mevcutGrupAdi = adapterView.getItemAtPosition(position).toString();

                //gruplardan birie basınca grup mesajlarının sayfasına yönlendirme için
                Intent grupChatActivity = new Intent(getContext(), GrupChatActivity.class);
                grupChatActivity.putExtra("grupAdi",mevcutGrupAdi);
                startActivity(grupChatActivity);
            }
        });

    return grupCerceveView;
    }

    private void GruplariAlGoster() {
        grupYolu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Set<String>set = new HashSet<>();
                Iterator iterator = snapshot.getChildren().iterator();

                while (iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }

                grupListeleri.clear();
                grupListeleri.addAll(set);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}