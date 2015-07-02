package com.xdlv.async;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.xdlv.async.task.Proc;

public class MainActivity extends Activity {
	MainTask mainTask = new MainTask(this, this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mainTask.request("download", 5, R.layout.activity_main,"download");
		mainTask.request("fast", 0, R.id.action_settings, "fast");
		
		findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mainTask.request("download", 5, R.layout.activity_main,"download");
			}
		});
		
	}
	
	@Proc({R.layout.activity_main, R.id.action_settings})
	void proc(Message msg){
		Toast.makeText(this, msg.obj + "", Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mainTask.cancle(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
