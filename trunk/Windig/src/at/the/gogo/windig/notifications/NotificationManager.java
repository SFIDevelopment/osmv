package at.the.gogo.windig.notifications;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class NotificationManager {

	private static Bus notificationBus;

	private NotificationManager() {

	}

	public static Bus getNotificationBus() {
		if (notificationBus == null) {
			notificationBus = new Bus(ThreadEnforcer.ANY);
		}
		return notificationBus;
	}

	public static void register(Object object) {
		getNotificationBus().register(object);
	}

	public static void deregister(Object object) {
		getNotificationBus().register(object);
	}

	public static void post(Object object) {
		getNotificationBus().post(object);
	}

}
