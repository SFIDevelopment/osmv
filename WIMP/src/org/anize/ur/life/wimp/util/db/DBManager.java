package org.anize.ur.life.wimp.util.db;

import android.content.Context;

public class DBManager {

	protected final Context mCtx;
	private final Database mDatabase;

	public DBManager(final Context ctx) {
		super();
		mCtx = ctx;
		mDatabase = new Database(ctx);
	}

	public Database getDatabase() {
		return mDatabase;
	}

	public void freeDatabases() {
		mDatabase.freeDatabases();
	}

	public void beginTransaction() {
		mDatabase.beginTransaction();
	}

	public void rollbackTransaction() {
		mDatabase.rollbackTransaction();
	}

	public void commitTransaction() {
		mDatabase.commitTransaction();
	}

}
