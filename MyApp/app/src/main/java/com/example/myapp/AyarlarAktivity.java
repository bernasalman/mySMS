package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//import android.widget.Toolbar;

import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.DataCollectionDefaultChange;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AyarlarAktivity extends AppCompatActivity {

    private Button hesapAyarlariniGuncelleme;
    private EditText kullaniciAdi, kullaniciDurumu;
    private CircleImageView kullaniciProfileResmi;

    //firebase
    private FirebaseAuth myetki;
    private DatabaseReference veriYolu;

    private String mevcutKullaniciId;

    //Tollabar
    private Toolbar ayarlarToolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar_aktivity);

        //firebase
        myetki = FirebaseAuth.getInstance();
        veriYolu = FirebaseDatabase.getInstance().getReference();

        mevcutKullaniciId = myetki.getCurrentUser().getUid();

        //kontroller
        hesapAyarlariniGuncelleme = findViewById(R.id.ayarlari_guncelle_button);
        kullaniciAdi =findViewById(R.id.kullanici_adi_ayarla);
        kullaniciDurumu =findViewById(R.id.profil_durum_ayarla);
        kullaniciProfileResmi =findViewById(R.id.profil_resmi_ayarla);

        //Toolbar
        ayarlarToolbar= findViewById(R.id.ayarlar_toolbar);
        //setSupportActionBar(ayarlarToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Hesap Ayarları");


        hesapAyarlariniGuncelleme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AyarlariGuncelle();

            }
        });

        //kullaniciAdi.setVisibility(View.INVISIBLE);
        
        KullaniciBilgisiAl();

    }

    private void KullaniciBilgisiAl() {
        veriYolu.child("Kullanicilar").child(mevcutKullaniciId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if ((snapshot.exists())&&(snapshot.hasChild("ad"))){
                    String kullaniciAdiniAl = snapshot.child("ad").getValue().toString();
                    String kullaniciDurumAl = snapshot.child("durum").getValue().toString();

                    kullaniciAdi.setText(kullaniciAdiniAl);
                    kullaniciDurumu.setText(kullaniciDurumAl);
                }
                
                else{
                    kullaniciAdi.setVisibility(View.VISIBLE);
                    Toast.makeText(AyarlarAktivity.this, "profil bilgilerinizi girin", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void AyarlariGuncelle() {
        String kullaniciAdiAyarla = kullaniciAdi.getText().toString();
        String kullaniciDurumAyarla = kullaniciDurumu.getText().toString();
        
        if(TextUtils.isEmpty(kullaniciAdiAyarla)){
            Toast.makeText(this, "Lütfen adınızı yazın", Toast.LENGTH_LONG).show();
        }

        if(TextUtils.isEmpty(kullaniciDurumAyarla)){
            Toast.makeText(this, "Lütfen durumunuzu yazın", Toast.LENGTH_LONG).show();
        }

        else{
            HashMap<String,String> profilHaritasi = new HashMap<>();
            profilHaritasi.put("uid", mevcutKullaniciId);
            profilHaritasi.put("ad", kullaniciAdiAyarla);
            profilHaritasi.put("durum", kullaniciDurumAyarla);

            veriYolu.child("Kullanicilar").child(mevcutKullaniciId).setValue(profilHaritasi)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(AyarlarAktivity.this, "Profiliniz güncellendi.", Toast.LENGTH_SHORT).show();
                                Intent anaSayfa = new Intent(AyarlarAktivity.this, MainActivity.class);
                                anaSayfa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(anaSayfa);
                                finish();
                            }
                            
                            else {
                                String mesaj = task.getException().toString();
                                Toast.makeText(AyarlarAktivity.this, "Hata:"+mesaj, Toast.LENGTH_SHORT).show();
                            }


                        }
                    });

        }
    }
}