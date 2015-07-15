package com.xdlv.async.task;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Message;

import com.xdlv.async.log.FileLogUtils;

abstract class CommonTask<Params> extends AsyncTask<Params, Object, Message> {

	static ScheduledExecutorService exec = Executors.newScheduledThreadPool(5);
	static FileLogUtils logger = FileLogUtils.getInstance("CommonTask");
	
    static final String TAG = "TASK_ERROR";
    private Object handler;
    TaskListener listener;
    private Object bundle;
    int code;
    long createTime = System.currentTimeMillis();
    long delay;
    boolean executed = false;
    Object[] para;

    public CommonTask(Object handler, int code,Object... par) {
        super();
        this.handler = handler;
        this.code = code;
    }
    boolean executable(){
        if (executed){
            return false;
        }
        if (delay ==0 ){
            return true;
        }
        return (System.currentTimeMillis() - createTime) / 1000 > delay;
    }
    public CommonTask<Params> bundle(Object  bundle){
        this.bundle = bundle;
        return this;
    }
    public CommonTask<Params> delay(long delay){
        this.delay = delay;
        return this;
    }
    public CommonTask<Params> para(Object[] para){
        this.para = para;
        return this;
    }
    public CommonTask<Params> executed(boolean executed){
        this.executed = executed;
        return this;
    }
    
    public CommonTask<Params> execute(int delay, Params ...params){
    	executeOnExecutor(exec, delay, params);
    	return this;
    }

    protected Message obtainMessage(Object obj) {
        return obtainMessage(code,obj);
    }

    protected Message obtainFinishMessage(JSONObject jsonObject) throws JSONException {
        return obtainMessage(code,
                jsonObject.getBoolean("success") ?
                        Boolean.TRUE : jsonObject.getString("msg"));
    }
    private Message obtainMessage(int code, Object obj){
        Message message = Message.obtain();
        message.what = code;
        if (bundle != null){
            message.obj = new Object[]{obj,bundle};
        } else {
            message.obj = obj;
        }
        return message;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //invokeMethod(getMethod(0,Pre.class),code);
    }

    @Override
    protected Message doInBackground(Params... params) {
        try {
            return execute2(params);
        } catch (Throwable e) {
            return obtainMessage(-code, e);
        }
    }

    protected abstract Message execute2(Params... params) throws Exception;


    @Override
    protected void onProgressUpdate(Object... values) {
        invokeMethod(getMethod(code, Prog.class), values);
    }

    @Override
    protected void onPostExecute(Message result) {
        //invokeMethod(getMethod(0,Post.class),code);
        // what is negative when an error occurs, print the stack of error fist
        if (result.what < 0 || result.obj instanceof Throwable) {
            e("An error occurs", (Throwable) result.obj);
        }
        invokeMethod(getMethod(result.what, Proc.class),result);
        result.recycle();
    }

    private void invokeMethod(Method method, Object obj){
        if (method != null) {
            try {
                method.invoke(handler, obj);
            } catch (Throwable e) {
                e("invoke method error", e);
            }
        } else {
            i("failed to find the method:" + obj, null);
        }
    }

    private Method getMethod(int what, Class<? extends Annotation> annotationClass) {
        List<Method> methodList = new ArrayList<Method>();
        Collections.addAll(methodList,handler.getClass().getDeclaredMethods());
        // try to add these methods belong to the parent if an error raises up.
        if (what < 0) {
            Collections.addAll(methodList, handler.getClass().getMethods());
        }
        int[] values;
        Annotation annotation;
        for (Method method : methodList) {
            annotation = method.getAnnotation(annotationClass);
            if (annotation == null){
                continue;
            }

            if (annotationClass.equals(Proc.class)){
                values = ((Proc)annotation).value();
            } else if (annotationClass.equals(Prog.class)){
                values = ((Prog)annotation).value();
            } else if (annotationClass.equals(Pre.class)) {
                values = ((Pre) annotation).value();
            } else if (annotationClass.equals(Post.class)) {
                values = ((Post) annotation).value();
            } else {
                continue;
            }
            for(int v : values){
                if (v == what){
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }

    protected void e(String msg, Throwable e) {
        logger.e(msg, e);
    }

    protected void i(String msg, Throwable e) {
    	logger.i(msg, e);
    }

    public static interface TaskListener{
        void preEveryTask(int code);
        void postEveryTask(int code);
    }
}
