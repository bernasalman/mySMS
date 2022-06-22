package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class KayitActivity extends AppCompatActivity {

    private Button KayitOlusturmaButtonu;
    private EditText KullaniciMail, KullaniciSifre;
    private TextView ZatenHesapVar;

    //firebase
    private DatabaseReference kokReference;
    private FirebaseAuth mYetki;


    private ProgressDialog yukleniyorDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayit);

        //firebase
        mYetki = FirebaseAuth.getInstance();
        kokReference = FirebaseDatabase.getInstance().getReference();

        //kontroller
        KayitOlusturmaButtonu = findViewById(R.id.kayit_butonu);

        KullaniciMail = findViewById(R.id.kayit_email);
        KullaniciSifre = findViewById(R.id.kayit_sifre);

        ZatenHesapVar = findViewById(R.id.hesap_var);

        yukleniyorDialog = new ProgressDialog(this);

        // hesap olduğunda giriş sayfasına yönlendirmek için
        ZatenHesapVar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginAcivity = new Intent(KayitActivity.this,LoginActivity.class);
                startActivity(loginAcivity);
            }
        });

        KayitOlusturmaButtonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YeniHesapOlustur();
            }
        });
    }

    private void YeniHesapOlustur() {
        String email = KullaniciMail.getText().toString();
        String sifre = KullaniciSifre.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Email boş olamaz...", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(sifre))
        {
            Toast.makeText(this, "Şifre boş olamaz...", Toast.LENGTH_SHORT).show();
        }

        else
        {
            yukleniyorDialog.setTitle("Yeni hesap oluşturuluyor");
            yukleniyorDialog.setMessage("Lütfen bekleyin");
            yukleniyorDialog.setCanceledOnTouchOutside(true);
            yukleniyorDialog.show();


            mYetki.createUserWithEmailAndPassword(email,sifre)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful())
                            {
                                String mevcutKullaniciId = mYetki.getCurrentUser().getUid();
                                kokReference.child("Kullanicilar").child(mevcutKullaniciId).setValue("");

                                Intent anaSayfa = new Intent(KayitActivity.this, MainActivity.class);
                                //geriye basınca çıkış yapmamak için
                                anaSayfa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(anaSayfa);
                                finish();

                                Toast.makeText(KayitActivity.this, "Yeni hesap başarı ile oluşturuldu...",Toast.LENGTH_SHORT).show();
                                yukleniyorDialog.dismiss();
                            }
                            else
                            {
                                String mesaj = task.getException().toString();
                                Toast.makeText(KayitActivity.this, "Hata: "+mesaj+"Bilgilerinizi kontrol edin!" ,Toast.LENGTH_SHORT).show();
                                yukleniyorDialog.dismiss();
                            }
                        }
                    });
        }
    }
}