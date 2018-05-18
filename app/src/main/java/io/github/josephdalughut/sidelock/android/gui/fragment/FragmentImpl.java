package io.github.josephdalughut.sidelock.android.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.josephdalughut.sidelock.android.R;
import io.github.josephdalughut.sidelock.android.gui.activity.Activity;

/**
 * Abstracted reusable fragment.
 */
public abstract class FragmentImpl extends Fragment {

    //the view for this fragment
    private View rootView;

    @Nullable @BindView(R.id.btnBack) public ImageButton btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, rootView);
        if(btnBack != null){
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getAppActivity().onBackPressed();
                }
            });
        }

        setupViews(rootView);
        return rootView;
    }

    public View getRootView() {
        return rootView;
    }

    /**
     * @return the layout resource file for this fragment
     */
    public abstract int getLayoutId();

    /**
     * @return {@link Activity} instance housing this fragment
     */
    public Activity getAppActivity(){
        return (Activity) getActivity();
    }

    /**
     * Setup the fragment within this method.
     * @param rootView the view for this fragment
     */
    public abstract void setupViews(View rootView);

}
