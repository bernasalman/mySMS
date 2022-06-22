package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import com.example.myapp.Model.Sifreleme;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class GrupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton mesajGondermeButonu;
    private EditText kullaniciMesajiGirdisi;
    private ScrollView mScrollView;
    private TextView metinMesajlarınıGoster;

    //firebase
    private FirebaseAuth mYetki;
    private DatabaseReference kullaniciYolu, grupAdiYolu, grupMesajAnahtariYolu;

    //Intent Değişkeni
    private String mevcutGrupAdi, aktifKullaniciAdi, aktifKullaniciId, aktifTarih, aktifZaman;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grup_chat);

        //intent al
        mevcutGrupAdi = getIntent().getExtras().get("grupAdi").toString();
        Toast.makeText(this, mevcutGrupAdi, Toast.LENGTH_SHORT).show();


        //firebase tanımlama
        mYetki = FirebaseAuth.getInstance();
        aktifKullaniciId = mYetki.getCurrentUser().getUid();
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        grupAdiYolu = FirebaseDatabase.getInstance().getReference().child("Gruplar").child(mevcutGrupAdi);


        //tanımlamalar
        mToolbar = findViewById(R.id.grup_chat_bar_layout);
        //setSupportActionBar(mToolbar); //burayı ekleyince hata alıyor
        getSupportActionBar().setTitle(mevcutGrupAdi);

        mesajGondermeButonu = findViewById(R.id.mesaj_gonderme_button);
        kullaniciMesajiGirdisi = findViewById(R.id.grup_mesaji_girdisi);
        metinMesajlarınıGoster = findViewById(R.id.grup_chat_metni_gösterme);
        mScrollView = findViewById(R.id.my_scroll_view);

        //kullanıcı bilgisini almak için
        KullaniciBilgisiAl();

        //butona basınca mesajı veritabanına kayıt etmek için
        mesajGondermeButonu.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                
                MesajiVeritabaninaKaydet();
                kullaniciMesajiGirdisi.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        grupAdiYolu.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if(snapshot.exists()){
                    mesajlariGoster(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if(snapshot.exists()){
                    mesajlariGoster(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mesajlariGoster(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String sohbetTarihi = (String) ((DataSnapshot)iterator.next()).getValue();
            String sohbetMesaji = (String) ((DataSnapshot)iterator.next()).getValue();
            String sohbetAdi = (String) ((DataSnapshot)iterator.next()).getValue();
            String sohbetZamani = (String) ((DataSnapshot)iterator.next()).getValue();

            metinMesajlarınıGoster.append(sohbetAdi+ ":\n"+ sohbetMesaji +"\n" +sohbetZamani+"   "+sohbetTarihi+ "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);


        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void MesajiVeritabaninaKaydet() {
        String mesaj = kullaniciMesajiGirdisi.getText().toString();
        String mesajAnahtari = grupAdiYolu.push().getKey(); //sürekli alt satıra göndermek için

        if(TextUtils.isEmpty(mesaj)){
            Toast.makeText(this, "mesaj boş olamaz", Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar tarihTak = Calendar.getInstance();
            SimpleDateFormat aktifTarihFormat = new SimpleDateFormat("MMM dd, yyyy");
            aktifTarih = aktifTarihFormat.format(tarihTak.getTime());

            Calendar zamanTak = Calendar.getInstance();
            SimpleDateFormat aktifZamanFormat = new SimpleDateFormat("hh:mm:ss a");
            aktifZaman = aktifZamanFormat.format(zamanTak.getTime());

            HashMap<String,Object>grupMesajAnahtari= new HashMap<>();
            grupAdiYolu.updateChildren(grupMesajAnahtari);

            grupMesajAnahtariYolu = grupAdiYolu.child(mesajAnahtari);

            //gönderilen mesajın veritabanında bilgisini tutmak için
            HashMap<String,Object> mesajBilgisiMap = new HashMap<>();

            //mesaj metninin şifrelenmesi
            String sifreliMesaj ="";
            Sifreleme sifreleme = new Sifreleme();
            try {
                sifreliMesaj = sifreleme.encryptString(mesaj,"sifreli");
            } catch (Exception e) {
                e.printStackTrace();
            }

            mesajBilgisiMap.put("ad",aktifKullaniciAdi);
            //mesajBilgisiMap.put("mesaj",mesaj);
            mesajBilgisiMap.put("mesaj",mesaj+ "   :     "+sifreliMesaj);
            mesajBilgisiMap.put("tarih",aktifTarih);
            mesajBilgisiMap.put("zaman",aktifZaman);

            grupMesajAnahtariYolu.updateChildren(mesajBilgisiMap);

        }

    }


    private void KullaniciBilgisiAl() {
        kullaniciYolu.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //kullanıcı varsa gruplara idsi ile aktarsın
                if(snapshot.exists()){
                    aktifKullaniciAdi = snapshot.child("ad").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}