package io.github.josephdalughut.sidelock.android.core;

/**
 * {@link android.app.Application} instance extension.
 * Mostly <b>unused</b>.
 */
public class Application extends android.app.Application {

    static Application sINSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        sINSTANCE = this;
    }

    /**
     * @return global application instance.
     */
    public static Application getInstance(){
        return sINSTANCE;
    }

}
