package com.xdlv.async;

import com.xdlv.async.task.ITaskProxy;

import android.os.Message;

public interface IMainTask extends ITaskProxy{

	Message test(int delay, int code, String name);

}