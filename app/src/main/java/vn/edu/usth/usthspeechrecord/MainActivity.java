package vn.edu.usth.usthspeechrecord;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.RequestQueue;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.usthspeechrecord.fragments.EditFragment;
import vn.edu.usth.usthspeechrecord.fragments.RecordFragment;
import vn.edu.usth.usthspeechrecord.fragments.VoteFragment;
import vn.edu.usth.usthspeechrecord.utils.VolleySingleton;

public class MainActivity extends AppCompatActivity {

    private static final String main_url =  "https://voiceviet.itrithuc.vn/api/v1";
    final int REQUEST_PERMISSION_CODE = 1000;
    public String mToken = "";
    RequestQueue mQueue;

    TabLayout mTabLayout;
    ViewPager viewPager;

    private int[] tabIcons = {
            R.drawable.ic_mic_black_24dp,
            R.drawable.ic_thumbs_up_down_black_24dp,
            R.drawable.ic_mode_edit_black_24dp
    };

    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //set status bar color
        setStatusColor();
        mTabLayout = findViewById(R.id.btm_nav_bar);
        viewPager = findViewById(R.id.container);
        mQueue = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue();

        //set actionbar color
        getSupportActionBar().setTitle("USTHSpeechRecord");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.recommended_lighter)));

        if (!checkPermissionFromDevice()) {
            requestPermission();
        }

        deleteFolder();

        Login();

    }
    public void setStatusColor(){
        Window window = getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(RecordFragment.newInstance(mToken), getString(R.string.sound_recording));
        adapter.addFragment(VoteFragment.newInstance(mToken), getString(R.string.voted));
        adapter.addFragment(EditFragment.newInstance(mToken), getString(R.string.edit));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
//                        getSupportActionBar().setTitle("Record");
                        break;
                    case 1:
//                        getSupportActionBar().setTitle("Vote");
                        break;
                    case 2:
//                        getSupportActionBar().setTitle("Edit");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }
    private void setupTabIcons() {
        if (mTabLayout!=null){
            if (mTabLayout.getTabAt(0)!=null)
                mTabLayout.getTabAt(0).setIcon(tabIcons[0]);
            if (mTabLayout.getTabAt(1)!=null)
                mTabLayout.getTabAt(1).setIcon(tabIcons[1]);
            if (mTabLayout.getTabAt(2)!=null)
                mTabLayout.getTabAt(2).setIcon(tabIcons[2]);
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    public boolean checkPermissionFromDevice() {
        int write_exteral_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int internet_result = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        return write_exteral_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED && internet_result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.aciton_bar_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                if (mToken!=null){
                    Toast.makeText(this, "You logged in", Toast.LENGTH_SHORT).show();
                }else {
                    LoginOnWeb();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void LoginOnWeb() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET
        },REQUEST_PERMISSION_CODE);
    }
    private void Login() {
        mToken = getIntent().getStringExtra("TOKEN");
        setupViewPager(viewPager);
        mTabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    private void deleteFolder() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,"VoiceDownload");
        file.delete();
        file = new File(filepath,"Audio");
        file.delete();
    }
}
