package io.github.josephdalughut.sidelock.android.gui.fragment.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.widget.AppCompatRatingBar;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import butterknife.BindView;
import io.github.josephdalughut.sidelock.android.R;
import io.github.josephdalughut.sidelock.android.gui.fragment.FragmentImpl;

/**
 * Shows info about this app
 */
public class AboutFragment extends FragmentImpl {

    /**
     * @return a new {@link AboutFragment} instance
     */
    public static AboutFragment newInstance(){
        return new AboutFragment();
    }

    @BindView(R.id.barRating) public AppCompatRatingBar barRating;
    @BindView(R.id.txtVersion) public TextView txtVersion;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_about;
    }

    @Override
    public void setupViews(View rootView) {
        barRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGooglePlayLink(getContext());
            }
        });
        barRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                openGooglePlayLink(getContext());
            }
        });

        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String version = pInfo.versionName;
            txtVersion.setText(getString(R.string.text_version) + " "+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void openGooglePlayLink(Context context){
        String url = "https://play.google.com/store/apps/details?id=io.github.josephdalughut.sidelock.android";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
