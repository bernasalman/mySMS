package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    //toolbar tanımlama
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private SekmeErisimAdapter mySekmeErisimAdapter;

    //firebase
    private FirebaseUser mevcutKullaici;
    private FirebaseAuth mYetki;
    private DatabaseReference kullanicilarReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mToolbar = findViewById(R.id.ana_sayfa_toolbar);
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
        kullanicilarReference = FirebaseDatabase.getInstance().getReference();

    }


    @Override
    protected void onStart(){
        super.onStart();

        if (mevcutKullaici == null){
            KullaniciLoginActivityeGonder();
        }

        else{
            KullaniciDogrula();
        }
        
    }

    private void KullaniciDogrula() {
        String mevcutKullaniciId = mYetki.getCurrentUser().getUid();

        kullanicilarReference.child("Kullanicilar").child(mevcutKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //kullanıcının adı varsa
                if((snapshot.child("ad").exists())){
                    Toast.makeText(MainActivity.this, "Hoşgeldiniz", Toast.LENGTH_SHORT).show();
                }

                /*//kullanıcının adı yoksa ayarlar sayfasına yönlendirme
                else {
                    Intent ayarlar = new Intent(MainActivity.this, AyarlarAktivity.class);
                    ayarlar.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(ayarlar);
                    finish();
                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //kullanıcı uygulamayı açtığında karşısına giriş ekranına yönlendirme
    private void KullaniciLoginActivityeGonder() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.secenekler_menu,menu);

        return true;
    }

    //menüdeki üç nokta özellikleri(çıkış yap, ayarlar, grup oluştur..)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()== R.id.ana_arkadas_bulma_secenegi){
            Intent arkadasBul = new Intent(MainActivity.this,ArkadasBulActivity.class);
            startActivity(arkadasBul);
        }

        if(item.getItemId()== R.id.ana_ayarlar_secenegi){
            Intent ayar = new Intent(MainActivity.this, AyarlarAktivity.class);
            startActivity(ayar);
        }

        if(item.getItemId()== R.id.ana_cikis_secenegi){
            mYetki.signOut();
            Intent giris = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(giris);
        }

        if(item.getItemId()== R.id.ana_grup_olustur_secenegi){
            yeniGrupTalebi();

        }

        return true;
    }

    //yeni grup oluşturmak için açılan penceredeki işlevselliği 
    private void yeniGrupTalebi() {

        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Grup Adi Giriniz");

        final EditText grupAdiAlani = new EditText(MainActivity.this);
        grupAdiAlani.setHint("Örnek : Üç Silahşörler");
        builder.setView(grupAdiAlani);

        builder.setPositiveButton("oluştur", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String grupAdi = grupAdiAlani.getText().toString();
                if (TextUtils.isEmpty(grupAdi)) {

                    Toast.makeText(MainActivity.this, "Grup adı baoş olamaz", Toast.LENGTH_SHORT).show();
                }

                //grup adı boş değilse veritabanında grubu yaratmak için
                else {
                    YeniGrupOlustur(grupAdi);
                }
            }
        });

        builder.setNegativeButton("iptal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void YeniGrupOlustur(String grupAdi) {
        kullanicilarReference.child("Gruplar").child(grupAdi).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, grupAdi+"adlı grup oluşturuldu", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}