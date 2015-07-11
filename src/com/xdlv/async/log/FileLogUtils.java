package com.xdlv.async.log;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;

import android.os.Environment;
import android.util.Log;

public class FileLogUtils {
	static String TAG = "FileLogUtils";
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
	static File logFile;
	String tag;

	public static void init(String dir, String fileName) {
		File sdcard = Environment.getExternalStorageDirectory();
		File logDir = new File(sdcard, dir);
		if (!logDir.exists() && !logDir.mkdirs()) {

			Log.e(TAG, "can not create log dir:" + logDir.getAbsolutePath());
			return;
		}
		logFile = new File(logDir, fileName);
	}

	private FileLogUtils(String tag) {
		this.tag = tag;
	}

	public static FileLogUtils getInstance(String tag) {
		return new FileLogUtils(tag);
	}

	public void i(String content, Throwable e) {
		Log.i(tag, content, e);
		if (logFile == null) {
			return;
		}
		flushFile(Level.INFO,tag,content,e);
	}
	public void e(String content, Throwable e) {
		Log.e(tag, content, e);
		if (logFile == null) {
			return;
		}
		flushFile(Level.WARNING,tag,content,e);
	}

	private static synchronized void flushFile(Level level, String tag,String content, Throwable e) {
		PrintStream ps = null;
		try {
			if (logFile.exists() && logFile.length() > 2 * 1024 * 1024){
				logFile.delete();
			}
			if (!logFile.exists() && !logFile.createNewFile()){
				Log.e(TAG, "can not create new logFiles:" + content);
				return;
			}
			ps = new PrintStream(logFile);
			ps.append(String.format("%s %s %s %s\n",(level == Level.INFO 
					? "INFO" : "ERROR"), sdf.format(new Date()),tag, content));
			if (e != null){
				e.printStackTrace(ps);
			}
			ps.append("\n");
			ps.flush();
		} catch (Exception ex) {
			Log.e(TAG, "flush file error",ex);
		} finally {
			if (ps != null){
				ps.close();
			}
		}
	}
}
