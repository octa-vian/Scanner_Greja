package co.id.gmedia.octavian.scannerapkgreja;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import co.id.gmedia.octavian.scannerapkgreja.fragment.FragmentEvent;
import co.id.gmedia.octavian.scannerapkgreja.fragment.FragmentJadwal;

public class MainActivityHome extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private Activity activity;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 1);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        activity = this;
        setContentView(R.layout.activity_main_home);
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        //memberi listener pada saat item bottom terpilih
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        //first Load
        loadfragment(new FragmentJadwal());
        clearFragmentBackStack();
    }

    private boolean loadfragment(Fragment fragment) {
        {
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.nav_host_fragment, fragment);
            trans.commitAllowingStateLoss();
        }
        return false;
    }

    public void clearFragmentBackStack() {
        FragmentManager fh = getSupportFragmentManager();
        for (int i = 0; i < fh.getBackStackEntryCount() - 1; i++) {
            fh.popBackStack();
        }
    }

    /**
     * Called when an item in the bottom navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item and false if the item should not be
     * selected. Consider setting non-selectable items as disabled preemptively to make them
     * appear non-interactive.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_ibadah:
                fragment = new FragmentJadwal();
                loadfragment(fragment);
                break;
            case R.id.btn_event:
                fragment = new FragmentEvent();
                loadfragment(fragment);
                break;
        }
        return true;
    }

}