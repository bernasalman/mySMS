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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button girisButonu, telefonGirisButonu;
    private EditText KullaniciMail, KullaniciSifre;
    private TextView hesapAlma, sifreUnutma;

    //firebase
    private FirebaseUser mevcutKullanici;
    private FirebaseAuth mYetki;

    private ProgressDialog girisDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //kontroller
        girisButonu = findViewById(R.id.giris_butonu);
        telefonGirisButonu = findViewById(R.id.tel_giris_butonu);

        KullaniciMail = findViewById(R.id.giris_email);
        KullaniciSifre = findViewById(R.id.giris_sifre);

        hesapAlma = findViewById(R.id.yeni_hesap_acma);
        sifreUnutma = findViewById(R.id.sifre_unutma_baglantisi);

        mYetki = FirebaseAuth.getInstance();
        mevcutKullanici = mYetki.getCurrentUser();

        girisDialog = new ProgressDialog(this);

        //yeni hesap oluştur dendiğinde kayıt sayfasına yönlendirmek için
        hesapAlma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent kayitActivityIntent = new Intent(LoginActivity.this, KayitActivity.class);
                startActivity(kayitActivityIntent);
            }
        });

        girisButonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KullaniciyaGirisİzniVer();
            }
        });
    }

    private void KullaniciyaGirisİzniVer()
    {
        String email = KullaniciMail.getText().toString();
        String sifre = KullaniciSifre.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Email boş olamaz!", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(sifre))
        {
            Toast.makeText(this, "Şifre boş olamaz!", Toast.LENGTH_SHORT).show();
        }

        else
        {
            girisDialog.setTitle("Giriş yapılıyor");
            girisDialog.setMessage("Lütfen bekleyin");
            girisDialog.setCanceledOnTouchOutside(true);
            girisDialog.show();

            mYetki.signInWithEmailAndPassword(email,sifre)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                Intent anaSayfa = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(anaSayfa);

                                Toast.makeText(LoginActivity.this, "Giriş Başarılı",Toast.LENGTH_SHORT).show();
                                girisDialog.dismiss();
                            }
                            else
                            {
                                String mesaj = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Hata: "+mesaj+"bilgilerinizi kontrol edin" ,Toast.LENGTH_SHORT).show();
                                girisDialog.dismiss();
                            }
                        }
                        }
                    );
        }

    }

    @Override
    protected void onStart(){
        super.onStart();
        if (mevcutKullanici != null){
            KullaniciAnaActivityeGonder();
        }
    }

    //kullanıcı giriş yapmak istediğinde açılacak ana sayfaya yönlendirme
    private void KullaniciAnaActivityeGonder() {
        Intent AnaActivityIntent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(AnaActivityIntent);
    }
}
