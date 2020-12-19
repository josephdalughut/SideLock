package io.github.josephdalughut.sidelock.android.gui.fragment.main;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wooplr.spotlight.SpotlightView;

import butterknife.BindView;
import io.github.josephdalughut.sidelock.android.R;
import io.github.josephdalughut.sidelock.android.broadcast.OnBootReceiver;
import io.github.josephdalughut.sidelock.android.cache.SharedPreferencesHelper;
import io.github.josephdalughut.sidelock.android.gui.activity.Activity;
import io.github.josephdalughut.sidelock.android.gui.fragment.FragmentImpl;
import io.github.josephdalughut.sidelock.android.service.LockService;
import io.github.josephdalughut.sidelock.android.utils.Disclosures;
import io.github.josephdalughut.sidelock.android.utils.LockManager;
import ru.noties.markwon.Markwon;

/**
 * Main UI shown on the first start of the application.
 *
 * @see <a href="http://www.androidhive.info/2016/11/android-floating-widget-like-facebook-chat-head/">ChatHead Tutorial</a>
 * on how to draw chat heads
 *
 */
public class MainFragment extends FragmentImpl {

    private static final int REQUEST_CODE_OVERLAY = 404;

    /**
     * Request code for device admin grant
     */
    public static final int REQUEST_CODE_GRANT_ADMIN = 101;
    public static final int REQUEST_CODE_GRANT_ADMIN_AUTO = 102;

    /*
        FIND VIEWS
     */

    //shows options menu
    @BindView(R.id.btnOptions) public ImageButton btnOptions;

    //handles enabling service
    @BindView(R.id.layEnabled) public View layEnabled;
    @BindView(R.id.switchEnabled) public SwitchCompat switchEnabled;
    @BindView(R.id.txtEnabled) public TextView txtEnabled;

    //handles enabling auto startup
    @BindView(R.id.layAuto) public View layAuto;
    @BindView(R.id.switchAuto) public SwitchCompat switchAuto;

    //locks screen
    @BindView(R.id.fabLock) public FloatingActionButton fabLock;

    @BindView(R.id.laySideLock) public View laySideLock;
    @BindView(R.id.switchSideLock) public SwitchCompat switchSideLock;

    //receive actions
    private BroadcastReceiver broadcastReceiver;


    private LockManager lockManager;

    public static MainFragment newInstance(){
        return new MainFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void setupViews(View rootView) {

        //show options menu
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.inflate(R.menu.menu_main);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.menuAbout:
                                getAppActivity().add(AboutFragment.newInstance());
                                return true;
                            case R.id.menuRate:
                                AboutFragment.openGooglePlayLink(getContext());
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        //register broadcast receiver to get notified when stopped from service
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onEnabled(false);
            }
        };
        getContext().registerReceiver(broadcastReceiver, new IntentFilter(LockService.ACTION_STOP));

        layEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lockManager.isDeviceAdmin()){
                    requestDeviceAdmin(REQUEST_CODE_GRANT_ADMIN);
                    return;
                }
                if(LockService.isRunning(getContext())){
                    LockService.stop(getContext());
                    onEnabled(false);
                }else {
                    LockService.start(getContext(), true);
                    onEnabled(true);
                }
            }
        });

        lockManager = new LockManager(getContext());

        layAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lockManager.isDeviceAdmin()){
                    requestDeviceAdmin(REQUEST_CODE_GRANT_ADMIN_AUTO);
                    return;
                }
                switchAuto.setChecked(!switchAuto.isChecked());
                OnBootReceiver.setEnabled(getContext(), switchAuto.isChecked());
            }
        });

        switchAuto.setChecked(SharedPreferencesHelper.getInstance(getContext()).getBoolean(SharedPreferencesHelper.KEY_BOOT_ENABLED, false));


        switchSideLock.setChecked(LockService.canDrawOverlays(getContext()) && SharedPreferencesHelper.getInstance(getContext()).getBoolean(SharedPreferencesHelper.KEY_SIDE_LOCK, false));
        laySideLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!LockService.canDrawOverlays(getContext())){
                    requestScreenOverlay();
                    return;
                }
                refreshSideLockSwitch();
            }
        });


        refreshServiceEnabled();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!= Activity.RESULT_OK)
            return;
        if(requestCode == REQUEST_CODE_GRANT_ADMIN) {
            LockService.start(getContext(), true);
            onEnabled(true);
        }else if(requestCode == REQUEST_CODE_GRANT_ADMIN_AUTO){
            switchAuto.setChecked(!switchAuto.isChecked());
            OnBootReceiver.setEnabled(getContext(), switchAuto.isChecked());
        }else if(requestCode == REQUEST_CODE_OVERLAY && LockService.canDrawOverlays(getContext())){
            refreshSideLockSwitch();
        }
    }


    /**
     * Called when the Floating button feature is enabled
     */
    private void refreshSideLockSwitch(){
        switchSideLock.setChecked(!switchSideLock.isChecked());

        //cache
        SharedPreferencesHelper.getInstance(getContext()).edit().putBoolean(SharedPreferencesHelper.KEY_SIDE_LOCK,
                switchSideLock.isChecked()).apply();

        // show dragging info
        if(switchSideLock.isChecked()){
            showDragDropInfo();
        }

        if(LockService.isRunning(getContext())){
            LockService.stop(getContext()); //stop the service
            LockService.start(getContext(), true); //start the service again to redraw Floating Lock button
        }
    }

    //display an initial tutorial on how to drag and drop the Floating Lock button
    private void showDragDropInfo(){
        SpotlightView spotlightView = new SpotlightView.Builder(getAppActivity())
                .introAnimationDuration(400)
                .enableRevealAnimation(true)
                .performClick(true)
                .fadeinTextDuration(400)
                .headingTvColor(getResources().getColor(R.color.appGreen))
                .headingTvSize(32)
                .headingTvText(getString(R.string.text_this_is_draggable))
                .subHeadingTvColor(Color.WHITE)
                .subHeadingTvSize(16)
                .subHeadingTvText(getString(R.string.text_this_is_draggable_info))
                .maskColor(Color.parseColor("#dc000000"))
                .target(fabLock)
                .lineAnimDuration(400)
                .lineAndArcColor(getResources().getColor(R.color.colorAccent))
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .enableDismissAfterShown(true)
                .usageId("DRAG_DROP") //UNIQUE ID
                .show();
    }

    //for enabled Switch
    private void refreshServiceEnabled(){
        if(!lockManager.isDeviceAdmin()) {
            onEnabled(false);
            return;
        }
        onEnabled(LockService.isRunning(getContext()));
    }

    private void onEnabled(boolean enabled){
        txtEnabled.setText(getString(enabled ? R.string.text_enabled : R.string.text_disabled));
        switchEnabled.setChecked(enabled);
//        if(enabled){
//            fabLock.show();
//        }else{
//            fabLock.hide();
//        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestScreenOverlay(){
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.lay_alert_textview, null);
        TextView txtMessage = customView.findViewById(R.id.txtMessage);
        Markwon.setMarkdown(txtMessage, Disclosures.OVERLAY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setView(customView)
                .setTitle(R.string.text_permissions_required)
                .setPositiveButton(R.string.text_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getContext().getPackageName()));
                        startActivityForResult(intent, REQUEST_CODE_OVERLAY);
                    }
                }).setNegativeButton(R.string.text_back, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void requestDeviceAdmin(final int requestCode){

        View customView = LayoutInflater.from(getContext()).inflate(R.layout.lay_alert_textview, null);
        TextView txtMessage = customView.findViewById(R.id.txtMessage);
        Markwon.setMarkdown(txtMessage, Disclosures.DEVICE_ADMIN);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setView(customView)
                .setTitle(R.string.text_permissions_required)
                .setPositiveButton(R.string.text_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        lockManager.requestDeviceAdmin(MainFragment.this, requestCode);
                    }
                }).setNegativeButton(R.string.text_back, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        //tell the lock service we are now in foreground
        getContext().sendBroadcast(new Intent(LockService.ACTION_FOREGROUND));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //tell the lock service we are now in background
        getContext().sendBroadcast(new Intent(LockService.ACTION_BACKGROUND));
        try{
            getContext().unregisterReceiver(broadcastReceiver);
        }catch (Exception ignored){

        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //tell the lock service we are now in background
        getContext().sendBroadcast(new Intent(LockService.ACTION_BACKGROUND));
    }
}
