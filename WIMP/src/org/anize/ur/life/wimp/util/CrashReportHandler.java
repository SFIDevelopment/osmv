package org.anize.ur.life.wimp.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import android.preference.PreferenceManager;

import com.bugsense.trace.BugSenseHandler;

public class CrashReportHandler implements UncaughtExceptionHandler {
	private final Context m_context;

	public static void attach(final Activity context) {
		Thread.setDefaultUncaughtExceptionHandler(new CrashReportHandler(
				context));
	}

	// /////////////////////////////////////////// implementation

	private CrashReportHandler(final Activity context) {
		m_context = context;
	}

	@Override
	public void uncaughtException(final Thread thread, final Throwable exception) {
		final StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(m_context);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString("error", stackTrace.toString());
		editor.commit();

		if (Util.USEBUGSENSE) {
			if (exception instanceof Exception)
				BugSenseHandler.sendException((Exception) exception);
		}

		// from RuntimeInit.crash()
		Process.killProcess(Process.myPid());
		System.exit(-1);
	}

}
