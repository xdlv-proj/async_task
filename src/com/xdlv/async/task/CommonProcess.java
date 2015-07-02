package com.xdlv.async.task;

import java.lang.reflect.Method;

import android.app.Activity;
import android.os.Message;
import android.util.SparseArray;

public class CommonProcess {
	protected Activity context;
	protected Object handler;
	static SparseArray<CommonTask<Object>> codeMap = new SparseArray<CommonTask<Object>>();
	static {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int size, i;
				CommonTask<Object> task;
				while (true) {
					synchronized (codeMap) {
						try {
							codeMap.wait(1000);
						} catch (InterruptedException e) {
						}
						size = codeMap.size();
						for (i = 0; i < size; i++) {
							task = codeMap.valueAt(i);
							if (task.executable()) {
								task.execute(task.para);
								task.executed(true);
							}
						}
					}
				}
			}
		}).start();
	}
	
	protected void preExecute(String name){}
	protected void postExecute(String name){}
	protected boolean filter(String name){
		return true;
	}

	public CommonProcess(Activity context, Object handler) {
		this.context = context;
		this.handler = handler;
	}

	public void request(final String name, long delay, final Object... paras) {
		int code = (Integer) paras[0];
		CommonTask<Object> task = new CommonTask<Object>(handler, code) {
			protected void onPreExecute() {
				if (filter(name)){
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							preExecute(name);
						}
					});
					
				}
			};
			@Override
			protected Message execute2(Object... params) throws Exception {
				return invokeMethodByName(name, params);
			}

			@Override
			protected void onPostExecute(Message result) {
				super.onPostExecute(result);
				synchronized (codeMap) {
					codeMap.delete(code);
				}
				if (filter(name)){
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							postExecute(name);
						}
					});
				}
			}
		}.delay(delay).para(paras);
		synchronized (codeMap) {
			codeMap.put(code, task);
			codeMap.notifyAll();
		}
	}

	private Message invokeMethodByName(String name, Object... pars) throws Exception {
		Method[] methods = getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getName().equals(name)) {
				method.setAccessible(true);
				return (Message) method.invoke(this, pars);
			}
		}
		return obtainMessage(-2, null);
	}

	public void cancle(int code) {
		synchronized (codeMap) {
			CommonTask<Object> task = codeMap.get(code);
			if (task != null && !task.isCancelled()) {
				task.cancel(false);
			}
			synchronized (codeMap) {
				codeMap.delete(code);
				codeMap.notifyAll();
			}
		}
	}

	protected Message obtainMessage(int code, Object obj) {
		Message message = Message.obtain();
		message.what = code;
		message.obj = obj;
		return message;
	}
}
