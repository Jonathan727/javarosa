package org.javarosa.reminders.util;

import java.io.IOException;
import java.util.Timer;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IDaemon;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.reminders.properties.ReminderPropertyRules;
import org.javarosa.reminders.storage.ReminderRMSUtility;
import org.javarosa.reminders.thread.ReminderBackgroundService;

public class ReminderNotifierDaemon implements IDaemon {
	public static final int period = 1000 * 60 * 5;
	
	public static final String DEFAULT_NAME = "Reminder Notifier Daemon";

	INotificationReceiver notificationReceiver;

	Timer timer;
	boolean running = false;

	public String getName() {
		return "Reminders Daemon";
	}

	public void restart() {
		if(isRunning()) {
			stop();
		} else {
			start();
		}
		
	}
	public void start() {
		String enabledProperty = JavaRosaServiceProvider.instance()
				.getPropertyManager().getSingularProperty(
						ReminderPropertyRules.REMINDERS_ENABLED_PROPERTY);
		if (ReminderPropertyRules.REMINDERS_ENABLED.equals(enabledProperty)) {
			if (!running) {
				timer = new Timer();
				ReminderBackgroundService service = new ReminderBackgroundService();
				service.setReminderNotifier(this);
				timer.schedule(service, 0, period);
				running = true;
			}
		}
	}

	public void stop() {
		if (running) {
			timer.cancel();
			running = false;
		}
	}

	/**
	 * @return the reminders
	 */
	public Vector getReminders() {
		ReminderRMSUtility reminderRms = (ReminderRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(ReminderRMSUtility.getUtilityName());
		try {
			return reminderRms.getReminders();
		} catch (IOException e) {
			this.stop();
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			this.stop();
			e.printStackTrace();
		} catch (InstantiationException e) {
			this.stop();
			e.printStackTrace();
		} catch (DeserializationException e) {
			this.stop();
			e.printStackTrace();
		}
		return new Vector();
	}

	/**
	 * @return the notificationReceiver
	 */
	public INotificationReceiver getNotificationReceiver() {
		return notificationReceiver;
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * @param notificationReceiver
	 *            the notificationReceiver to set
	 */
	public void setNotificationReceiver(
			INotificationReceiver notificationReceiver) {
		this.notificationReceiver = notificationReceiver;
	}

	public void remindersExpired(Vector expiredReminders) {
		if (notificationReceiver != null) {
			notificationReceiver.receiveReminders(expiredReminders);
		}
	}
}