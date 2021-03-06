package com.example.myapp;

import android.icu.util.Freezable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SekmeErisimAdapter extends FragmentPagerAdapter {
    public SekmeErisimAdapter(FragmentManager fm) {
        super(fm);
    }

    //hangi pozisyonun hangi sınıfı çalıştıracağı
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
            ChatsFragment chatsFragment = new ChatsFragment();
            return  chatsFragment;

            case 1:
                GruplarFragment gruplarFragment = new GruplarFragment();
                return  gruplarFragment;


            case 2:
                KisilerFragment kisilerFragment = new KisilerFragment();
                return  kisilerFragment;

            case 3:
                TaleplerFragment taleplerFragment = new TaleplerFragment();
                return taleplerFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }


    //pozisyona göre başlık ayarlama
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "sohbetler";

            case 1:
                return  "gruplar";


            case 2:
                return  "kişiler";

            case 3:
                return  "talepler";

            default:
                return null;
        }
    }
}
