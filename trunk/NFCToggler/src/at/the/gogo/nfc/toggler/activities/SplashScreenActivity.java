package at.the.gogo.nfc.toggler.activities;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import at.the.gogo.nfc.toggler.R;

public class SplashScreenActivity extends Activity {
    protected boolean _active     = true;
    protected int     _splashTime = 1000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        final ImageView splashImage = (ImageView) findViewById(R.id.splashImage);

        splashImage.post(new Runnable() {
            @Override
            public void run() {
                splashImage.startAnimation(AnimationUtils
                        .loadAnimation(SplashScreenActivity.this,
                                android.R.anim.slide_in_left)); // R.anim.splash
            }
        });

        // thread for displaying the SplashScreen
        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (_active && (waited < _splashTime)) {
                        sleep(100);
                        if (_active) {
                            waited += 100;
                        }                        
                    }
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    finish();

                    startActivity(new Intent(SplashScreenActivity.this,
                            NFCTogglerPreferencesActivity.class));
//                    stop();
                }
            }
        };
        splashTread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _active = false;
        }
        return true;
    }
}