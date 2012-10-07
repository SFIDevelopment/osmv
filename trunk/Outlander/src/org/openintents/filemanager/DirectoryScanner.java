package org.openintents.filemanager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openintents.filemanager.util.FileUtils;
import org.outlander.utils.Ut;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class DirectoryScanner extends Thread {

    private final File currentDirectory;
    boolean            cancel;

    private Context    context;
    private Handler    handler;

    // Cupcake-specific methods
    static Method      formatter_formatFileSize;

    static {
        initializeCupcakeInterface();
    }

    DirectoryScanner(final File directory, final Context context,
            final Handler handler) {
        super("Directory Scanner");
        currentDirectory = directory;
        this.context = context;
        this.handler = handler;
    }

    private void clearData() {
        // Remove all references so we don't delay the garbage collection.
        context = null;
        handler = null;
    }

    @Override
    public void run() {
        Ut.d("Scanning directory " + currentDirectory);

        final File[] files = currentDirectory.listFiles();

        int totalCount = 0;

        if (cancel) {
            Ut.d("Scan aborted");
            clearData();
            return;
        }

        if (files == null) {
            Ut.d("Returned null - inaccessible directory?");
            totalCount = 0;
        } else {
            totalCount = files.length;
        }

        Ut.d("Counting files... (total count=" + totalCount + ")");

        int progress = 0;

        /** Dir separate for sorting */
        final List<IconifiedText> listDir = new ArrayList<IconifiedText>(
                totalCount);

        /** Files separate for sorting */
        final List<IconifiedText> listFile = new ArrayList<IconifiedText>(
                totalCount);

        /** SD card separate for sorting */
        final List<IconifiedText> listSdCard = new ArrayList<IconifiedText>(3);

        if (files != null) {
            for (final File currentFile : files) {
                if (cancel) {
                    // Abort!
                    Ut.d("Scan aborted while checking files");
                    clearData();
                    return;
                }

                progress++;
                updateProgress(progress, totalCount);

                /*
                 * if (currentFile.isHidden()) { continue; }
                 */
                if (currentFile.isDirectory()) {
                    listDir.add(new IconifiedText(currentFile.getName(),
                            "Directory", null));
                } else {
                    String size = "";

                    final String ext = FileUtils.getExtension(currentFile
                            .getName());
                    if (ext.equalsIgnoreCase(".kml")
                            || ext.equalsIgnoreCase(".gpx")) {

                        try {
                            size = (String) DirectoryScanner.formatter_formatFileSize
                                    .invoke(null, context, currentFile.length());
                        } catch (final Exception e) {
                            // The file size method is probably null (this is
                            // most
                            // likely not a Cupcake phone), or something else
                            // went wrong.
                            // Let's fall back to something primitive, like just
                            // the number
                            // of KB.
                            size = Long.toString(currentFile.length() / 1024);
                            size += " KB";

                            // Technically "KB" should come from a string
                            // resource,
                            // but this is just a Cupcake 1.1 fallback, and KB
                            // is universal
                            // enough.
                        }

                        listFile.add(new IconifiedText(currentFile.getName(),
                                size, null));
                    }
                }
            }
        }

        Ut.d("Sorting results...");

        // Collections.sort(mListSdCard);
        Collections.sort(listDir);
        Collections.sort(listFile);

        // add parent dir to top of list
        if (currentDirectory.getParentFile() != null) {
            listDir.add(0, new IconifiedText("...", "Up to directory", null));
        }

        if (!cancel) {
            Ut.d("Sending data back to main thread");

            final DirectoryContents contents = new DirectoryContents();

            contents.listDir = listDir;
            contents.listFile = listFile;
            contents.listSdCard = listSdCard;

            final Message msg = handler
                    .obtainMessage(FileManagerActivity.MESSAGE_SHOW_DIRECTORY_CONTENTS);
            msg.obj = contents;
            msg.sendToTarget();
        }

        clearData();
    }

    private void updateProgress(final int progress, final int maxProgress) {
        // // Only update the progress bar every n steps...
        // if ((progress % PROGRESS_STEPS) == 0) {
        // // Also don't update for the first second.
        // long curTime = SystemClock.uptimeMillis();
        //
        // if (curTime - operationStartTime < 1000L) {
        // return;
        // }
        //
        // // Okay, send an update.
        // Message msg =
        // handler.obtainMessage(FileManagerActivity.MESSAGE_SET_PROGRESS);
        // msg.arg1 = progress;
        // msg.arg2 = maxProgress;
        // msg.sendToTarget();
        // }
    }

    private static void initializeCupcakeInterface() {
        try {
            DirectoryScanner.formatter_formatFileSize = Class.forName(
                    "android.text.format.Formatter").getMethod(
                    "formatFileSize", Context.class, long.class);
        } catch (final Exception ex) {
            // This is not cupcake.
            return;
        }
    }
}
