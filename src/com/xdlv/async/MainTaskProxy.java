package com.xdlv.async;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Message;
import android.util.Log;

import com.xdlv.async.task.ProxyCommonTask;

public class MainTaskProxy extends ProxyCommonTask implements IMainTask{

	AlertDialog alertDialog = null;
	public MainTaskProxy(Activity context, Object handler) {
		super(context,handler);
	}
	
	public Message test(int delay,int code,String name){
		return obtainMessage(code, name);
	}
	
	protected void preExecute(int name) {
		Log.e("cc", Thread.currentThread().getName());
		if (alertDialog == null){
			alertDialog = new AlertDialog.Builder(context).setTitle("loading...").create();
		}
		alertDialog.show();
		
	}
	protected void postExecute(int name) {
		Log.e("cc", Thread.currentThread().getName());
		alertDialog.dismiss();
	}
	protected boolean filter(int code) {
		return true;
	}
	
	
}
