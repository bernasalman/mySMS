package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import com.example.myapp.Model.Kisiler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArkadasBulActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView ArkadasBulRecyclerListesi;

    //fireebase
    private DatabaseReference KullaniciYolu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arkadas_bul);

        //Recycler
        ArkadasBulRecyclerListesi=findViewById(R.id.arkadas_bul_recyler_listesi);
        ArkadasBulRecyclerListesi.setLayoutManager(new LinearLayoutManager(this));

        //Toolbar
        mToolbar=findViewById(R.id.arkadas_bul_toolbar);
        //setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Arkadaş Bul");

        //Firebase tanımlama
        KullaniciYolu= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Başladığında

        //Sorgu-Seçenekler
        FirebaseRecyclerOptions<Kisiler> secenekler = new FirebaseRecyclerOptions.Builder<Kisiler>()
                        .setQuery(KullaniciYolu, Kisiler.class)
                        .build();

        FirebaseRecyclerAdapter<Kisiler,ArkadasBulViewHolder> adapter = new FirebaseRecyclerAdapter<Kisiler, ArkadasBulViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull ArkadasBulViewHolder holder, final int position, @NonNull Kisiler model) {
                //verilerin kullanıcı bilgilerinin yazılacağı üst kısma bağlanması
                holder.kullaniciAdi.setText(model.getAd());
                holder.kullaniciDurumu.setText(model.getDurum());
                //Picasso.get().load(model.getResim()).into(holder.profilResmi);

               //Tıklandığında
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String tıklanan_kullanici_Id_goster = getRef(position).getKey();

                        Intent profilAktivite = new Intent(ArkadasBulActivity.this,ProfilActivity.class);
                        profilAktivite.putExtra("tıklanan_kullanici_Id_goster",tıklanan_kullanici_Id_goster);
                        startActivity(profilAktivite);
                    }
                });

            }

            @NonNull
            @Override
            public ArkadasBulViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                //her satırda ne olması gerektiğini
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.kullanici_gosterme_layout,viewGroup,false);
                ArkadasBulViewHolder viewHolder = new ArkadasBulViewHolder(view);

                return viewHolder;

            }
        };

        ArkadasBulRecyclerListesi.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();


    }

    public static class ArkadasBulViewHolder extends RecyclerView.ViewHolder
    {
        TextView kullaniciAdi,kullaniciDurumu;
        CircleImageView profilResmi;

        public ArkadasBulViewHolder(@NonNull View itemView) {
            super(itemView);

            //Tanımlamalar
            kullaniciAdi=itemView.findViewById(R.id.kullanici_profil_adi);
            kullaniciDurumu=itemView.findViewById(R.id.kullanici_durumu);
            profilResmi=itemView.findViewById(R.id.kullanicilar_profil_resmi);
        }
    }
}
