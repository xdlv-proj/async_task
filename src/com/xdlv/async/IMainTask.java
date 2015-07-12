package com.xdlv.async;

import android.os.Message;

import com.xdlv.async.task.ITaskProxy;

public interface IMainTask extends ITaskProxy{

	Message test(int delay, int code, String name);

}