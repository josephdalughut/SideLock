package io.github.josephdalughut.sidelock.android.service;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RemoteViews;

import io.github.josephdalughut.sidelock.android.R;
import io.github.josephdalughut.sidelock.android.cache.SharedPreferencesHelper;
import io.github.josephdalughut.sidelock.android.notifications.NotificationChannelManager;
import io.github.josephdalughut.sidelock.android.utils.LockManager;

/**
 * Service handler for Locking the device screen
 */
public class LockService extends Service {

    private static final String LOG_TAG = LockService.class.getSimpleName();

    /*
    Actions for intent
     */
    public static final String ACTION_LOCK = LockService.class.getSimpleName() + ".LOCK";
    public static final String ACTION_UNLOCK = LockService.class.getSimpleName() + ".UNLOCK";
    public static final String ACTION_START = LockService.class.getSimpleName() + ".START";
    public static final String ACTION_STOP = LockService.class.getSimpleName() + ".STOP";
    public static final String ACTION_FOREGROUND = LockService.class.getSimpleName() + ".UI.FOREGROUND";
    public static final String ACTION_BACKGROUND = LockService.class.getSimpleName() + ".UI.BACKGROUND";
    public static final String ACTION_LOCKED = LockService.class.getSimpleName() + ".LOCKED";

    /**
     * @see {@link LockService#showNotification()}
     */
    public static final String NOTIFICATION_CHANNEL_ID = "Default";

    //Id for dispached notifications
    private static final int NOTIFICATION_ID = 777;

    //ref to lock manager
    private LockManager lockManager;

    //broadcast receiver to trigger actions via intents
    private BroadcastReceiver lockBroadcastReceiver;

    //button that's rendered on the ui
    private View lockButton;

    /**
     * Starts the {@link LockService}
     * @param context
     * @param isUiInForground true if called from ui
     */
    public static void start(Context context, boolean isUiInForground) {
        Intent intent = new Intent(context, LockService.class);
        intent.putExtra(ACTION_FOREGROUND, isUiInForground);
        context.startService(intent);
    }

    /**
     * Stops the {@link LockService}
     * @param context
     */
    public static void stop(Context context){
        context.stopService(new Intent(context, LockService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null)
            return super.onStartCommand(intent, flags, startId);

        boolean isUiInForground = intent.getBooleanExtra(ACTION_FOREGROUND, false);

        lockManager = new LockManager(this);
        lockBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(ACTION_START)){
                    //unimplemented
                }else if(action.equals(ACTION_STOP)){
                    stopSelf();
                }else if(action.equals(ACTION_LOCK)){
                    lockManager.lock();
                }else if(action.equals(ACTION_UNLOCK)){
                    //unimplemented
                }else if(action.equals(ACTION_FOREGROUND)){
                    onForeground(true);
                }else if(action.equals(ACTION_BACKGROUND)){
                    onForeground(false);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_START);
        intentFilter.addAction(ACTION_STOP);
        intentFilter.addAction(ACTION_LOCK);
        intentFilter.addAction(ACTION_UNLOCK);
        intentFilter.addAction(ACTION_FOREGROUND);
        intentFilter.addAction(ACTION_BACKGROUND);

        registerReceiver(lockBroadcastReceiver, intentFilter);

        showNotification();
        drawButton(isUiInForground);

        return super.onStartCommand(intent, flags, startId);
    }

    //draws the lock button on ui
    private void drawButton(boolean isUiInForground){
        if(!canDrawOverlays(this) || !SharedPreferencesHelper.getInstance(this).getBoolean(
                SharedPreferencesHelper.KEY_SIDE_LOCK, false))
            return;

        ContextThemeWrapper ctx = new ContextThemeWrapper(this, R.style.Base_AppTheme);
        LayoutInflater inflater = LayoutInflater.from(ctx);

        lockButton = inflater.inflate(R.layout.lay_widget, null);

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner

        if(isUiInForground){
            float[] floats = getLockButtonForegroundScreenLocation();
            params.x = (int) floats[0];
            params.y = (int) floats[1];
        }else{
            SharedPreferences preferences = SharedPreferencesHelper.getInstance(this);
            params.x = (int) preferences.getInt(SharedPreferencesHelper.KEY_BUTTON_X, 0);
            params.y = (int) preferences.getInt(SharedPreferencesHelper.KEY_BUTTON_Y, 50);
        }

        //Add the view to the window
        final WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(lockButton, params);

        FloatingActionButton fab = lockButton.findViewById(R.id.child);
        fab.show();

        if(!isUiInForground){
            settleButtonToSide();
        }

        lockButton.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                lockButton.setTranslationX(0);

                PropertyValuesHolder x = PropertyValuesHolder.ofFloat("scaleX", v.getScaleX(), 1.5f);
                PropertyValuesHolder y = PropertyValuesHolder.ofFloat("scaleY", v.getScaleY(), 1.5f);

                ValueAnimator animator = ObjectAnimator.ofPropertyValuesHolder(v.findViewById(R.id.child), x, y);
                animator.setDuration(200);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.start();

                lockButton.setOnTouchListener(new View.OnTouchListener() {
                    private int initialX;
                    private int initialY;
                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:

                                //remember the initial position.
                                initialX = params.x;
                                initialY = params.y;


                                //get the touch location
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                //Calculate the X and Y coordinates of the view.
                                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                                params.y = initialY + (int) (event.getRawY() - initialTouchY);


                                //Update the layout with new X & Y coordinate
                                mWindowManager.updateViewLayout(lockButton, params);
                                return true;
                            case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL:
                                PropertyValuesHolder x = PropertyValuesHolder.ofFloat("scaleX", v.getScaleX(), 1f);
                                PropertyValuesHolder y = PropertyValuesHolder.ofFloat("scaleY", v.getScaleY(), 1f);

                                ValueAnimator animator = ObjectAnimator.ofPropertyValuesHolder(v.findViewById(R.id.child), x, y);
                                animator.setDuration(200);
                                animator.setInterpolator(new DecelerateInterpolator());
                                animator.start();

                                settleButtonToSide();

                                v.setOnTouchListener(null);
                                int last_x = params.x;
                                int last_y = params.y;
                                SharedPreferencesHelper.getInstance(LockService.this).edit()
                                        .putInt(SharedPreferencesHelper.KEY_BUTTON_X, last_x)
                                        .putInt(SharedPreferencesHelper.KEY_BUTTON_Y, last_y).apply();
                                v.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sendBroadcast(new Intent(ACTION_LOCKED));
                                        lockManager.lock();
                                    }
                                });
                                return false;
                        }
                        return false;
                    }
                });
                return true;
            }
        });

        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(ACTION_LOCKED));
                lockManager.lock();
            }
        });
    }

    /**
     * Settles the lock button to the extreme left/right edge of the screen
     */
    private void settleButtonToSide(){

        //get window manager & params
        final WindowManager.LayoutParams params = (WindowManager.LayoutParams) lockButton.getLayoutParams();
        final WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //current position
        int currentX = params.x;

        //inflate displayMetrics instance with window dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);

        //if the current position is less than half the screen, it's on the left
        boolean left = currentX < (displayMetrics.widthPixels / 2);
        int newX = left ? 0 : displayMetrics.widthPixels;
        float fabMargin = getResources().getDimensionPixelSize(R.dimen.margin_page);
        float translationX = left ? -fabMargin : fabMargin;

        //animate lockButton to edge
        ValueAnimator x = ValueAnimator.ofInt(params.x, newX);
        x.setDuration(300);
        x.setInterpolator(new LinearOutSlowInInterpolator());

        ValueAnimator tX = ObjectAnimator.ofFloat(lockButton, "translationX", lockButton.getTranslationX(), translationX);
        tX.setDuration(300);
        tX.setInterpolator(new LinearOutSlowInInterpolator());

        x.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedX = (int) animation.getAnimatedValue();
                params.x = animatedX;
                mWindowManager.updateViewLayout(lockButton, params);
            }
        });

        tX.start();
        x.start();
    }

    /**
     * Updates the {@link #lockButton} position when the app is either in foreground or background
     * @param foreground true if app is now in foreground, false otherwise
     */
    private void onForeground(final boolean foreground){
        if(lockButton == null) //button isn't initialized, return
            return;

        //get params and window manager
        final WindowManager.LayoutParams params = (WindowManager.LayoutParams) lockButton.getLayoutParams();
        final WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int newX, newY;

        if(foreground){ //app is now in foreground
            int last_x = params.x;
            int last_y = params.y;

            //cache the last position of the button so that we can use it as the
            //preferred location for the user on background
            SharedPreferencesHelper.getInstance(this).edit()
                    .putInt(SharedPreferencesHelper.KEY_BUTTON_X, last_x)
                    .putInt(SharedPreferencesHelper.KEY_BUTTON_Y, last_y).apply();


            float[] floats = getLockButtonForegroundScreenLocation();
            newX = (int) floats[0];
            newY = (int) floats[1];
            lockButton.setTranslationX(0);


        }else{ //app is in background

            //get cached preferred location
            SharedPreferences preferences = SharedPreferencesHelper.getInstance(this);
            newX = preferences.getInt(SharedPreferencesHelper.KEY_BUTTON_X, 0);
            newY = preferences.getInt(SharedPreferencesHelper.KEY_BUTTON_Y, 50);
        }


        //animate lockButton

        ValueAnimator x = ValueAnimator.ofInt(params.x, newX);
        ValueAnimator y = ValueAnimator.ofInt(params.y, newY);
        x.setDuration(250);
        y.setDuration(250);
        x.setInterpolator(new LinearOutSlowInInterpolator());
        y.setInterpolator(new LinearOutSlowInInterpolator());

        x.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedX = (int) animation.getAnimatedValue();
                params.x = animatedX;
                //don't updateViewLayout here, it runs almost in sync with the guy below.
                //no point, just waste of cpu
            }
        });

        y.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedY = (int) animation.getAnimatedValue();
                params.y = animatedY;
                mWindowManager.updateViewLayout(lockButton, params);
            }
        });

        Animator.AnimatorListener listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mWindowManager.updateViewLayout(lockButton, params);
                if(!foreground){
                    settleButtonToSide();
                }
            }
        };

        x.addListener(listener);
        y.addListener(listener);

        x.start();
        y.start();

    }

    /**
     * @return coordinates where the {@link android.support.design.widget.FloatingActionButton} would be
     * laid out on the {@link io.github.josephdalughut.sidelock.android.gui.fragment.main.MainFragment} screen
     */
    public float[] getLockButtonForegroundScreenLocation(){

        //margin the fab by this
        float margin = getResources().getDimensionPixelSize(R.dimen.margin_page);

        float halfWidth = lockButton.getWidth();
        if(halfWidth == 0){
            halfWidth = lockButton.getMeasuredWidth();
        }
        halfWidth = halfWidth / 2f;

        float finalMargin = margin + halfWidth;

        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        float newX = metrics.widthPixels - finalMargin;
        float newY = metrics.heightPixels - finalMargin;

        return new float[]{newX, newY};
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lockManager = null;

        //remove lock button
        if(lockButton != null){
            WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            try{
                mWindowManager.removeViewImmediate(lockButton);
            }catch (Exception ignored){

            }
            lockButton = null;
        }

        try{ //remove broadcast
            unregisterReceiver(lockBroadcastReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
        getNotificationMgr(this).cancel(NOTIFICATION_ID);
    }

    /**
     * Shows action buttons on the notification trays to control app
     */
    private void showNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannelManager.CHANNEL_CONTROLS.channelId);
        builder.setPriority(NotificationCompat.PRIORITY_MIN); //set to lowest priority so it's shown below all other notifications

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.lay_service_actions);

        //lock button
        remoteViews.setOnClickPendingIntent(R.id.btnLock, PendingIntent.getBroadcast(this,
                0, new Intent(ACTION_LOCK), 0));

        //stop button
        remoteViews.setOnClickPendingIntent(R.id.btnStop, PendingIntent.getBroadcast(this,
                1, new Intent(ACTION_STOP), 0));

        builder.setCustomContentView(remoteViews).setCustomBigContentView(remoteViews);

        builder.setSmallIcon(R.drawable.ic_stat_cellphone_key);

        //issue notification
        NotificationChannelManager.CHANNEL_CONTROLS.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * @param context
     * @return true if the {@link LockService} is running, false otherwise
     *
     * Adapted from <a href="https://stackoverflow.com/a/5921190">StackOverflow</a>
     */
    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LockService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param context
     * @return the {@link android.app.NotificationManager} (compat)
     */
    public static NotificationManagerCompat getNotificationMgr(Context context){
        return NotificationManagerCompat.from(context);
    }

    /**
     * @param context
     * @return true if this app has been granted the {@link Settings#ACTION_MANAGE_OVERLAY_PERMISSION}
     * to draw on the screen. Required to draw the lock button
     */
    public static boolean canDrawOverlays(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(context);
    }

}
