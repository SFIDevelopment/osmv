package at.the.gogo.parkoid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.activities.MapActivity;
import at.the.gogo.parkoid.util.CoreInfoHolder;

public class ParkuhrFragment extends LocationListenerFragment {

    TextView              mapButton;
    TextView              bigParkButton;

    private long          m_when;
    // private Timer m_timer;
    private int           m_lastRemainingTime = -1;
    private boolean       timerIsRunning      = false;
    private final Handler mHandler            = new Handler();

    public static ParkuhrFragment newInstance() {
        final ParkuhrFragment fragment = new ParkuhrFragment();

        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // setHasOptionsMenu(true);

        setUpdateVPZ(false);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.parkuhr, null);

        mapButton = (TextView) view.findViewById(R.id.showmap);

        mapButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                showMap();
            }
        });

        bigParkButton = (TextView) view.findViewById(R.id.parkuhr);

        bigParkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                parken();
            }
        });

        initializeGUI(view);

        return view;
    }

    private void showMap() {
        final Intent intent = new Intent(getActivity(), MapActivity.class);
        startActivity(intent);
    }

    private void parken() {

        if (!timerIsRunning) {
            Toast.makeText(CoreInfoHolder.getInstance().getContext(),
                    R.string.DLGYES, Toast.LENGTH_SHORT).show();

            // CountdownTask ct = new CountdownTask(
            // System.currentTimeMillis() + 30 * 60 * 1000);
            // ct.start(60);

            m_when = System.currentTimeMillis() + (1 * 60 * 1000);
            timerIsRunning = true;
            bigParkButton.setBackgroundDrawable(getActivity().getResources()
                    .getDrawable(R.drawable.widget_appwidget_bg_ok));

            try {

                mHandler.removeCallbacks(hMyTimeTask);
                mHandler.postDelayed(hMyTimeTask, 1000); // delay 1 second
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } else {
            // entparken !
            bigParkButton.setText(R.string.timer_notime);
            bigParkButton.setBackgroundDrawable(getActivity().getResources()
                    .getDrawable(R.drawable.widget_appwidget_bg_ok));
            timerIsRunning = false;
            // stopTimer();
            mHandler.removeCallbacks(hMyTimeTask);
        }

    }

    @Override
    public void zoneLevelUpdate(final Boolean inZone) {
        super.zoneLevelUpdate(inZone);
        if (inZone != null) {
            if (inZone) {
                bigParkButton.setBackgroundDrawable(getActivity()
                        .getResources().getDrawable(
                                R.drawable.widget_appwidget_bg_nok));
            } else {
                bigParkButton.setBackgroundDrawable(getActivity()
                        .getResources().getDrawable(
                                R.drawable.widget_appwidget_bg_ok));
            }
        } else {
            bigParkButton.setBackgroundDrawable(getActivity().getResources()
                    .getDrawable(R.drawable.widget_appwidget_bg));
        }
    }

    private final Runnable hMyTimeTask = new Runnable() {

                                           public void refresh() {
                                               int remainingTime = (int) ((m_when - System
                                                       .currentTimeMillis())
                                               // SystemClock
                                               // .elapsedRealtime())
                                               / 1000);
                                               if (remainingTime <= 0) {
                                                   remainingTime = 0;

                                                   bigParkButton
                                                           .setText(R.string.timer_invalid);
                                                   bigParkButton
                                                           .setBackgroundDrawable(getActivity()
                                                                   .getResources()
                                                                   .getDrawable(
                                                                           R.drawable.widget_appwidget_bg_nok));
                                                   timerIsRunning = false;
                                                   // stopTimer();
                                                   mHandler.removeCallbacks(this);

                                               } else {
                                                   mHandler.postDelayed(this,
                                                           1000);

                                                   /*
                                                    * only update the view if
                                                    * anything has changed
                                                    */
                                                   if (m_lastRemainingTime == remainingTime) {
                                                       return;
                                                   }

                                                   m_lastRemainingTime = remainingTime;

                                                   final int seconds = remainingTime % 60;
                                                   final int minutes = (remainingTime / 60) % 60;
                                                   final int hours = remainingTime / 3600;

                                                   final String time = String
                                                           .format("%02d:%02d:%02d",
                                                                   hours,
                                                                   minutes,
                                                                   seconds);

                                                   bigParkButton.setText(time);
                                               }
                                           }

                                           @Override
                                           public void run() {

                                               refresh();
                                           }
                                       };

    // public class CountdownTask {
    //
    // private final long m_when;
    //
    // private Timer m_timer;
    //
    // private int m_lastRemainingTime = -1;
    //
    // private static final String TAG = "CountdownTask";
    // private static final boolean LOGD = false;
    //
    // private class CountdownTimerTask extends TimerTask {
    //
    // @Override
    // public void run() {
    // refresh();
    // }
    //
    // }
    //
    // public CountdownTask(final long when) {
    // m_when = when;
    // }
    //
    // public void start(int interval) {
    // stopTimer();
    // m_timer = new Timer();
    // if (interval == 1) {
    // interval = 200;
    // } else {
    // interval = interval * 1000;
    // }
    // m_timer.scheduleAtFixedRate(new CountdownTimerTask(), 0, interval);
    // }
    //
    // public void refresh() {
    // int remainingTime = (int) ((m_when - SystemClock.elapsedRealtime()) /
    // 1000);
    // if (remainingTime <= 0) {
    // remainingTime = 0;
    //
    // bigParkButton.setText(R.string.timer_invalid);
    //
    // stopTimer();
    // }
    //
    // /* only update the view if anything has changed */
    // if (m_lastRemainingTime == remainingTime) {
    // if (LOGD) {
    // Log.d(TAG, "No update!");
    // }
    // return;
    // }
    //
    // m_lastRemainingTime = remainingTime;
    //
    // // final int seconds = remainingTime % 60;
    // final int minutes = (remainingTime / 60) % 60;
    // final int hours = remainingTime / 3600;
    //
    // final String time = String.format("%02d:%02d", hours, minutes);
    //
    // if (LOGD) {
    // Log.d(TAG, "Update: " + time);
    // }
    //
    // bigParkButton.setText(time);
    // }
    //
    // public void reset() {
    // stopTimer();
    //
    // }
    //
    // public void stop() {
    // stopTimer();
    // }
    //
    // private void stopTimer() {
    // if (m_timer == null) {
    // return;
    // }
    // m_timer.cancel();
    // m_timer = null;
    // }
    //
    // }

}
