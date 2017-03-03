package com.gimbal.hello_gimbal_android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.Communication;
import com.gimbal.android.CommunicationListener;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Push;
import com.gimbal.android.Visit;

import java.util.LinkedList;
import java.util.List;

public class AppService extends Service {

    public static final String APPSERVICE_STARTED_ACTION = "appservice_started";
    private static final int MAX_NUM_EVENTS = 100;

    private PlaceEventListener placeEventListener;
    private BeaconEventListener beaconEventListener;
    private CommunicationListener communicationListener;
    private LinkedList<String> events;
    private static final int MY_NOTIFICATION_ID=1;


    @Override
    public void onCreate(){
        events = new LinkedList<>(GimbalDAO.getEvents(getApplicationContext()));

        Gimbal.setApiKey(this.getApplication(), "2b212c19-20da-4de2-a1ff-753823cd155b");
        setupGimbalPlaceManager();
        setupGimbalCommunicationManager();
    }

    private void setupGimbalCommunicationManager() {
        communicationListener = new CommunicationListener() {
            @Override
            public Notification.Builder prepareCommunicationForDisplay(Communication communication, Visit visit, int notificationId) {
                addEvent(String.format( "Communication Delivered : %s", communication.getTitle()));
                Notification.Builder myNotification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Exercise of Notification!")
                        .setContentText(String.format( "Communication Delivered : %s", communication.getTitle()))
                        .setTicker("Notification!")
                        .setWhen(System.currentTimeMillis())
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true);
                myNotification.build();
                // If you want a custom notification create and return it here
                return myNotification;
            }

            @Override
            public Notification.Builder prepareCommunicationForDisplay(Communication communication, Push push, int notificationId) {
                addEvent(String.format( "Push Communication Delivered : %s", communication.getTitle()));
                Notification.Builder myNotification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Exercise of Notification!")
                        .setContentText(String.format( "Communication Delivered : %s", communication.getTitle()))
                        .setTicker("Notification!")
                        .setWhen(System.currentTimeMillis())
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true);
                myNotification.build();
                // If you want a custom notification create and return it here
                return myNotification;
            }

            @Override
            public void onNotificationClicked(List<Communication> communications) {
                for (Communication communication : communications) {
                    if(communication != null) {
                        addEvent("Communication Clicked");
                    }
                }
            }
        };
        CommunicationManager.getInstance().addListener(communicationListener);
        CommunicationManager.getInstance().startReceivingCommunications();
    }

    private void setupGimbalPlaceManager() {
        placeEventListener = new PlaceEventListener() {

            @Override
            public void onVisitStart(Visit visit) {
                addEvent(String.format("Start Visit for %s", visit.getPlace().getName()));
                NotificationCompat.Builder myNotification = new NotificationCompat.Builder(getApplicationContext())
                        .setContentTitle("Exercise of Notification!")
                        .setTicker("Notification!")
                        .setWhen(System.currentTimeMillis())
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true);

                // Add as notification
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(MY_NOTIFICATION_ID, myNotification.build());
            }

            @Override
            public void onVisitEnd(Visit visit) {
                addEvent(String.format("End Visit for %s", visit.getPlace().getName()));
            }
        };
        PlaceManager.getInstance().addListener(placeEventListener);
        PlaceManager.getInstance().startMonitoring();
    }



    private void addEvent(String event) {
        while (events.size() >= MAX_NUM_EVENTS) {
            events.removeLast();
        }
        events.add(0, event);
        GimbalDAO.setEvents(getApplicationContext(), events);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        notifyServiceStarted();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        PlaceManager.getInstance().removeListener(placeEventListener);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyServiceStarted() {
        Intent intent = new Intent(APPSERVICE_STARTED_ACTION);
        sendBroadcast(intent);
    }
}
