package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.Model.Kisiler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class TaleplerFragment extends Fragment {

        private View TaleplerFragmentView;

        private RecyclerView taleplerListem;

        //firebase
        private DatabaseReference SohbetTalepleriYolu,KullanicilarYolu,SohbetlerYolu;
        private FirebaseAuth mYetki;

        private String aktifKullaniciId;

        public TaleplerFragment() {
            // Required empty public constructor
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            TaleplerFragmentView= inflater.inflate(R.layout.activity_talepler_fragment, container, false);

            //Firebase
            mYetki=FirebaseAuth.getInstance();
            aktifKullaniciId=mYetki.getCurrentUser().getUid();
            SohbetTalepleriYolu= FirebaseDatabase.getInstance().getReference().child("Sohbet Talebi");
            KullanicilarYolu= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
            SohbetlerYolu= FirebaseDatabase.getInstance().getReference().child("Sohbetler");

            //Recyler
            taleplerListem= TaleplerFragmentView.findViewById(R.id.chat_talepleri_listesi);
            taleplerListem.setLayoutManager(new LinearLayoutManager(getContext()));

            return TaleplerFragmentView;
        }

        @Override
        public void onStart() {
            super.onStart();

            FirebaseRecyclerOptions<Kisiler> secenekler = new FirebaseRecyclerOptions.Builder<Kisiler>()
                    .setQuery(SohbetTalepleriYolu.child(aktifKullaniciId),Kisiler.class)
                    .build();

            FirebaseRecyclerAdapter<Kisiler,TaleplerViewHolder> adapter = new FirebaseRecyclerAdapter<Kisiler, TaleplerViewHolder>(secenekler) {
                @Override
                protected void onBindViewHolder(@NonNull final TaleplerViewHolder holder, int position, @NonNull Kisiler model) {

                    //Buttonlar?? g??sterme
                    holder.itemView.findViewById(R.id.talep_kabul_buttonu).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.talep_iptal_buttonu).setVisibility(View.VISIBLE);


                    //Taleplerin hepsini alma
                    final String kullanici_id_listesi = getRef(position).getKey();

                    DatabaseReference talepTuruAl = getRef(position).child("talep_turu").getRef();

                    talepTuruAl.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            //e??er talepte veri varsa
                            if (dataSnapshot.exists())
                            {
                                String tur=dataSnapshot.getValue().toString();

                                if (tur.equals("al??nd??"))
                                {
                                    KullanicilarYolu.child(kullanici_id_listesi).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            //Veri taban??ndan verileri al??p de??i??kenlere aktarma
                                            final String talepKullaniciAdi = dataSnapshot.child("ad").getValue().toString();
                                            final String talepKullaniciDurumu = dataSnapshot.child("durum").getValue().toString();

                                            //??ekilen verileri ilgili kontrollere aktarma
                                            holder.kullaniciAdi.setText(talepKullaniciAdi);
                                            holder.kullaniciDurumu.setText("kullan??c?? senle ileti??im kurmak istiyor");

                                            //her sat??ra t??kland??????nda
                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    CharSequence secenekler[] = new CharSequence[]
                                                            {
                                                                    "Kabul",
                                                                    "??ptal"
                                                            };

                                                    //Alertdialog
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle(talepKullaniciAdi+" Chat Talebi");

                                                    builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            //sohbet talebi kabul edildiyse
                                                            if (which == 0)
                                                            {
                                                                SohbetlerYolu.child(aktifKullaniciId).child(kullanici_id_listesi).child("Sohbetler")
                                                                        .setValue("Kaydedildi").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful())
                                                                        {
                                                                            SohbetlerYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                    .child("Sohbetler").setValue("Kaydedildi")
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                            if (task.isSuccessful())
                                                                                            {
                                                                                                SohbetTalepleriYolu.child(aktifKullaniciId).child(kullanici_id_listesi)
                                                                                                        .removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                                if (task.isSuccessful())
                                                                                                                {
                                                                                                                    SohbetTalepleriYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                                                            .removeValue()
                                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                                                    Toast.makeText(getContext(), "Sohbet kaydedildi..", Toast.LENGTH_LONG).show();

                                                                                                                                }
                                                                                                                            });
                                                                                                                }

                                                                                                            }
                                                                                                        });
                                                                                            }

                                                                                        }
                                                                                    });
                                                                        }

                                                                    }
                                                                });

                                                            }

                                                            //sohbet talebi red edildiyse
                                                            if (which == 1)
                                                            {
                                                                SohbetTalepleriYolu.child(aktifKullaniciId).child(kullanici_id_listesi)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    SohbetTalepleriYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                    Toast.makeText(getContext(), "Sohbet silindi..", Toast.LENGTH_LONG).show();

                                                                                                }
                                                                                            });
                                                                                }

                                                                            }
                                                                        });

                                                            }

                                                        }
                                                    });

                                                    builder.show();

                                                }


                                            });

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                                else if (tur.equals("gonderildi"))
                                {
                                    Button talep_gonderme_btn = holder.itemView.findViewById(R.id.talep_kabul_buttonu);
                                    talep_gonderme_btn.setText("Talep Gonderildi");

                                    //??ptal butonunu g??r??nmez yapma
                                    holder.itemView.findViewById(R.id.talep_iptal_buttonu).setVisibility(View.INVISIBLE);

                                    //Yap????t??r
                                    KullanicilarYolu.child(kullanici_id_listesi).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            //Veri taban??ndan verileri al??p de??i??kenlere aktarma
                                            final String talepKullaniciAdi = dataSnapshot.child("ad").getValue().toString();
                                            final String talepKullaniciDurumu = dataSnapshot.child("durum").getValue().toString();

                                            //??ekilen verileri ilgili kontrollere aktarma
                                            holder.kullaniciAdi.setText(talepKullaniciAdi);
                                            holder.kullaniciDurumu.setText("sen "+ talepKullaniciAdi+ " adl?? kullan??c??ya talep g??nderdin");

                                            //her sat??ra t??kland??????nda
                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    CharSequence secenekler[] = new CharSequence[]
                                                            {
                                                                    "Chat Talebini ??ptal Et"
                                                            };

                                                    //Alertdialog
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle("Mevcut Chat Talebi Var");

                                                    builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            if (which == 0)
                                                            {
                                                                SohbetTalepleriYolu.child(aktifKullaniciId).child(kullanici_id_listesi)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    SohbetTalepleriYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                    Toast.makeText(getContext(), "Chat talebiniz silindi..", Toast.LENGTH_LONG).show();

                                                                                                }
                                                                                            });
                                                                                }

                                                                            }
                                                                        });

                                                            }

                                                        }
                                                    });

                                                    builder.show();

                                                }


                                            });

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                @NonNull
                @Override
                public TaleplerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.kullanici_gosterme_layout,viewGroup,false);

                    TaleplerViewHolder holder = new TaleplerViewHolder(view);

                    return holder;

                }
            };

            taleplerListem.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            adapter.startListening();
        }

        public static class TaleplerViewHolder extends RecyclerView.ViewHolder
        {
            //Kontroller
            TextView kullaniciAdi, kullaniciDurumu;
            CircleImageView profilResmi;
            Button KabulButtonu,IptalButtonu;

            public TaleplerViewHolder(@NonNull View itemView) {
                super(itemView);

                //Kontrol tan??mlamalar??
                kullaniciAdi=itemView.findViewById(R.id.kullanici_profil_adi);
                kullaniciDurumu=itemView.findViewById(R.id.kullanici_durumu);
                profilResmi=itemView.findViewById(R.id.kullanicilar_profil_resmi);
                KabulButtonu=itemView.findViewById(R.id.talep_kabul_buttonu);
                IptalButtonu=itemView.findViewById(R.id.talep_iptal_buttonu);
            }
        }
    }

