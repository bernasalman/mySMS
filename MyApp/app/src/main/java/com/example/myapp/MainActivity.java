package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    //toolbar tanımlama
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private SekmeErisimAdapter mySekmeErisimAdapter;

    //firebase
    private FirebaseUser mevcutKullaici;
    private FirebaseAuth mYetki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.ana_sayfa_toolbar);
        //setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("MyApp");

        myViewPager = findViewById(R.id.ana_sekmeler_pager);
        mySekmeErisimAdapter = new SekmeErisimAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(mySekmeErisimAdapter);

        myTabLayout= findViewById(R.id.ana_sekmeler);
        myTabLayout.setupWithViewPager(myViewPager);

        //firebase
        mYetki = FirebaseAuth.getInstance();
        mevcutKullaici = mYetki.getCurrentUser();

    }

    @Override
    protected void onStart(){
        super.onStart();
        if (mevcutKullaici == null){
            KullaniciLoginActivityeGonder();
        }
        
    }

    //kullanıcı uygulamayı açtığında karşısına giriş ekranına yönlendirme
    private void KullaniciLoginActivityeGonder() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.secenekler_menu,menu);

        return true;
    }

    //menüdeki üç nokta özellikleri
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()== R.id.ana_arkadas_bulma_secenegi){

        }

        if(item.getItemId()== R.id.ana_ayarlar_secenegi){

        }

        if(item.getItemId()== R.id.ana_cikis_secenegi){
            mYetki.signOut();
            Intent giris = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(giris);
        }

        return true;
    }
}