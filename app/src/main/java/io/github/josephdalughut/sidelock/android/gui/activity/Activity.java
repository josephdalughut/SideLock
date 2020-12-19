package io.github.josephdalughut.sidelock.android.gui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;

import io.github.josephdalughut.sidelock.android.R;
import io.github.josephdalughut.sidelock.android.gui.fragment.main.MainFragment;
import io.github.josephdalughut.sidelock.android.service.LockService;

/**
 * Default activity
 */
public class Activity extends AppCompatActivity{

    //receives lock intent to close itself
    private BroadcastReceiver onLockReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        replace(MainFragment.newInstance());
        onLockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        getApplicationContext().registerReceiver(onLockReceiver, new IntentFilter(LockService.ACTION_LOCK));
    }

    //call super so this is passed down to fragments
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            getApplicationContext().unregisterReceiver(onLockReceiver);
        }catch (Exception ignored){}
    }

    /**
     * Adds a {@link Fragment} to this activity
     * @param fragment
     */
    public void add(Fragment fragment){
        getSupportFragmentManager().beginTransaction().add(R.id.layFragmentContainer, fragment)
                .addToBackStack(null).commitAllowingStateLoss();
    }

    /**
     * Replaces a {@link Fragment} to this activity
     * @param fragment
     */
    public void replace(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.layFragmentContainer, fragment)
                .commitAllowingStateLoss();
    }


}
