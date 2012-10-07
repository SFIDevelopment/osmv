package at.the.gogo.parkoid;

import android.app.Application;
import at.the.gogo.parkoid.util.CoreInfoHolder;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

public class ParkoidApplication extends Application {

    CoreInfoHolder cih;

    @Override
    public void onCreate() {
        super.onCreate();

        initSingletons();
    }

    protected void initSingletons() {

        LocationLibrary.initializeLibrary(getBaseContext());
        cih = CoreInfoHolder.getInstance();
    }

    public CoreInfoHolder getCoreInformationHolder() {
        return cih;
    }

}
