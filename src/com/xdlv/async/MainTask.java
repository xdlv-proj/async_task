package com.xdlv.async;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Message;
import android.util.Log;

import com.xdlv.async.task.CommonProcess;

public class MainTask extends CommonProcess{

	AlertDialog alertDialog = null;
	public MainTask(Activity context, Object handler) {
		super(context, handler);
	}
	
	protected void preExecute(String name) {
		Log.e("cc", Thread.currentThread().getName());
		if (alertDialog == null){
			alertDialog = new AlertDialog.Builder(context).setTitle("loading...").create();
		}
		alertDialog.show();
		
	}
	protected void postExecute(String name) {
		Log.e("cc", Thread.currentThread().getName());
		alertDialog.dismiss();
	}
	protected boolean filter(String name) {
		return name.equals("download");
	}
	
	Message download(int code, String name) throws Exception{
		Thread.sleep(5000);
		Log.e("Common", "download" );
		return obtainMessage(code, name);
	}
	
	Message fast(int code, String name) throws Exception{
		return obtainMessage(code, name);
	}

}
