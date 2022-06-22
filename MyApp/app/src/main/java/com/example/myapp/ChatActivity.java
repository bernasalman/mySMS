package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.Adapter.MesajAdaptor;
import com.example.myapp.Model.Mesajlar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String IdMesajiAlici, AdMesajiAlici,IdMesajGonderen;

    private TextView kullaniciAdi,kullaniciSonGorulmsi;
    private CircleImageView kullaniciResmi;
    private ImageView sohbeteGondermeOku;

    private ImageButton mesajGondermeButtonu;
    private EditText GirilenMesanMetni;

    //Toolbar
    private Toolbar SohbetToolbar;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference mesajYolu,kullaniciYolu;

    private final List<Mesajlar> mesajlarList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MesajAdaptor mesajAdaptor;
    private RecyclerView kullaniciMesajlariListesi;

    private String kaydedilenAktifZaman, kaydedilenAktifTarih;
    private String kontrolcu="", myUrl="";
    private StorageTask yuklemeGorevi;


    //Progress
    private ProgressDialog yuklemeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Chat fragmentten gelen Intenti al
        IdMesajiAlici = getIntent().getExtras().get("kullanici_id_ziyaret").toString();
        AdMesajiAlici = getIntent().getExtras().get("kullanici_adi_ziyaret").toString();

        //Tanımlamalar
        kullaniciAdi = findViewById(R.id.kullanici_adi_gosterme_chat_activity);
        kullaniciResmi = findViewById(R.id.kullanicilar_profil_resmi_chat_activity);
        mesajGondermeButtonu = findViewById(R.id.mesaj_gonder_btn);
        GirilenMesanMetni = findViewById(R.id.girilen_mesaj);

        mesajAdaptor = new MesajAdaptor(mesajlarList);
        kullaniciMesajlariListesi = findViewById(R.id.kullanicilarin_ozel_mesajlarinin_listesi);
        linearLayoutManager = new LinearLayoutManager(this);
        kullaniciMesajlariListesi.setLayoutManager(linearLayoutManager);
        kullaniciMesajlariListesi.setAdapter(mesajAdaptor);

        yuklemeBar = new ProgressDialog(this);

        //TAKVİM
        Calendar calendar = Calendar.getInstance();
        //Tarih formatı
        SimpleDateFormat aktifTarih = new SimpleDateFormat("MMM dd, yyyy");
        kaydedilenAktifTarih = aktifTarih.format(calendar.getTime());

        //Saat formatı
        SimpleDateFormat aktifZaman = new SimpleDateFormat("hh:mm a");
        kaydedilenAktifZaman = aktifZaman.format(calendar.getTime());

        //Firebase
        mYetki = FirebaseAuth.getInstance();
        mesajYolu = FirebaseDatabase.getInstance().getReference();
        kullaniciYolu = FirebaseDatabase.getInstance().getReference();
        IdMesajGonderen = mYetki.getCurrentUser().getUid();

        //Kontrollere Intentle gelenleri aktarma
        kullaniciAdi.setText(AdMesajiAlici);

        //Mesaj gönderme butonuna tıklandığında
        mesajGondermeButtonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MesajGonder();

            }
        });
    }

        @Override
        protected void onStart() {

            super.onStart();

            //Veri tabanından verileri çekme
            mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            //Veri tabanından veriyi alıp modele aktarma
                            Mesajlar mesajlar = dataSnapshot.getValue(Mesajlar.class);

                            //Modeli listeye ekleme
                            mesajlarList.add(mesajlar);

                            mesajAdaptor.notifyDataSetChanged();

                            //Scrollview ayarlama
                            kullaniciMesajlariListesi.smoothScrollToPosition(kullaniciMesajlariListesi.getAdapter().getItemCount());

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }


    private void  MesajGonder(){
            //Mesajı kontrolden alma
            String mesajMetni = GirilenMesanMetni.getText().toString();

            if (TextUtils.isEmpty(mesajMetni)) {
                Toast.makeText(this, "Mesaj yazmanız gerekiyor!", Toast.LENGTH_SHORT).show();
            }
            else {
                String mesajGonderenYolu = "Mesajlar/" + IdMesajGonderen + "/" + IdMesajiAlici;
                String mesajAlanYolu = "Mesajlar/" + IdMesajiAlici + "/" + IdMesajGonderen;

                DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici).push();
                String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();

                Map mesajMetniGovdesi = new HashMap();
                mesajMetniGovdesi.put("mesaj", mesajMetni);
                mesajMetniGovdesi.put("tur", "metin");
                mesajMetniGovdesi.put("kimden", IdMesajGonderen);
                mesajMetniGovdesi.put("kime", IdMesajiAlici);
                mesajMetniGovdesi.put("mesajID", mesajEklemeId);
                mesajMetniGovdesi.put("zaman", kaydedilenAktifZaman);
                mesajMetniGovdesi.put("tarih", kaydedilenAktifTarih);

                Map mesajGovdesiDetaylari = new HashMap();
                mesajGovdesiDetaylari.put(mesajGonderenYolu + "/" + mesajEklemeId, mesajMetniGovdesi);
                mesajGovdesiDetaylari.put(mesajAlanYolu + "/" + mesajEklemeId, mesajMetniGovdesi);

                mesajYolu.updateChildren(mesajGovdesiDetaylari).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(ChatActivity.this, "Mesaj Gönderildi!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ChatActivity.this, "Mesaj gönderme hatalı!!!", Toast.LENGTH_SHORT).show();
                        }

                        GirilenMesanMetni.setText("");

                    }
                });
            }
        }
    }


