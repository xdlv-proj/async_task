package com.xdlv.async.task;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.os.Message;
import android.util.SparseArray;

public class ProxyCommonTask implements InvocationHandler {
	static SparseArray<CommonTask<Object>> codeMap = new SparseArray<CommonTask<Object>>();

	protected Activity context;
	protected Object handler;

	public ProxyCommonTask(Activity context, Object handler) {
		this.context = context;
		this.handler = handler;
	}

	public static Object createTaskProxy(Class<?> taskClass, Activity context, Object handler) {
		try {
			InvocationHandler h = (InvocationHandler) taskClass.getConstructor(Activity.class, Object.class)
					.newInstance(context, handler);
			return Proxy.newProxyInstance(taskClass.getClassLoader(), taskClass.getInterfaces(), h);
		} catch (Exception e) {
			throw new RuntimeException("cant load proxy:" + e, e);
		}
	}

	@Override
	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		final int delay = (Integer)args[0];
		if (method.getName().equals("cancle")){
			cancle(delay);
			return null;
		}
		final int code = (Integer)args[1];
		if (method.getReturnType().equals(Message.class)) {
			CommonTask<Object> task = new CommonTask<Object>(handler, code) {
				protected void onPreExecute() {
					if (filter(code)){
						context.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								preExecute(code);
							}
						});
					}
				}
				@Override
				protected Message execute2(Object... params) throws Exception {
					return (Message) method.invoke(ProxyCommonTask.this, params);
				}
				protected void onPostExecute(Message result) {
					super.onPostExecute(result);
					synchronized (codeMap) {
						codeMap.delete(code);
					}
					if (filter(code)){
						context.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								postExecute(code);
							}
						});
					}
				}
			}.execute(delay, args);
			synchronized (codeMap) {
				codeMap.put(code, task);
			}
		} else {
			return method.invoke(this, args);
		}
		return null;
	}
	
	public void cancle(int code) {
		synchronized (codeMap) {
			CommonTask<Object> task = codeMap.get(code);
			if (task != null && !task.isCancelled()) {
				task.cancel(false);
			}
			synchronized (codeMap) {
				codeMap.delete(code);
			}
		}
	}

	protected Message obtainMessage(int code, Object obj) {
		Message message = Message.obtain();
		message.what = code;
		message.obj = obj;
		return message;
	}
	
	protected boolean filter(int code){
		return true;
	}
	protected void preExecute(int code){}
	protected void postExecute(int code){}

	static class Data{
		int delay, code;
		boolean mask;
	}
}
