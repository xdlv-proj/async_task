package com.xdlv.async;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.xdlv.async.task.Proc;
import com.xdlv.async.task.ProxyCommonTask;

public class MainActivity extends Activity {
	IMainTask task = ProxyCommonTask.createTaskProxy(MainTaskProxy.class,IMainTask.class ,this, this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout);
		
		findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				task.test(0, R.id.button1, "¡¢º¥÷¥––");
			}
		});
		
		findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				task.test(5, R.id.button2, "—”≥Ÿ5√Î÷¥––");
			}
		});
	}
	
	@Proc({R.id.button1,R.id.button2})
	void procMessage(Message msg){
		Toast.makeText(this, msg.obj + "", Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		task.cancle(R.id.button2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
