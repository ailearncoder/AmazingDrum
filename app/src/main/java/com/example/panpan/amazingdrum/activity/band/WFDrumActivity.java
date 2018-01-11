package com.example.panpan.amazingdrum.activity.band;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.example.panpan.amazingdrum.BleLink;
import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.custom.MyUtil;
import com.example.panpan.amazingdrum.util.FlashLight;
import com.example.panpan.amazingdrum.util.UartDataDeal;

import java.util.HashMap;

/** 王妃鼓 */
public class WFDrumActivity extends Activity implements OnTouchListener
{
	private final int id[] = new int[] { R.id.button1, R.id.button2,
			R.id.button3, R.id.button4, R.id.button5 };
	private final Button button[] = new Button[id.length];

	private final String files[] = new String[] { "小擦", "大擦1", "大擦2",
			"底鼓", "军鼓" };
	private final String path = "/sdcard/王妃/drum/";
	private final HashMap<String, Integer> soundIdHashMap = new HashMap<String, Integer>();
	private int index = 0;
	private SoundPool soundPool;
	private final Handler handler = new Handler();
	private boolean isSlience;
	private MediaPlayer mediaPlayer;
	private BleLink bleLink=null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wf_drum);
		bleLink=WFActivity.bleLink;
		for (int i = 0; i < button.length; i++)
		{
			button[i] = (Button) findViewById(id[i]);
			button[i].setOnTouchListener(this);
		}
		if (FlashLight.init(this))
			FlashLight.startAutoFlash();
		soundPool = new SoundPool(files.length, AudioManager.STREAM_MUSIC, 0);
		int id;
		for (int i = 0; i < files.length; i++)
		{
			id = soundPool.load(path + files[i] + ".ogg", 1);
			soundIdHashMap.put(files[i], id);
		}
		initBluetooth();
		mediaPlayer = new MediaPlayer();
		try
		{
			mediaPlayer.setDataSource("/sdcard/王妃/王妃.mp3");
			mediaPlayer.prepare();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	public void setVolume(View v)
	{
		if (isSlience)
		{
			isSlience = false;
			v.setBackgroundResource(R.drawable.volume_open);
		} else
		{
			isSlience = true;
			v.setBackgroundResource(R.drawable.volume_close);
		}
	}

	private boolean isMusicPlay = false;

	public void musicPlay(View v)
	{
		if (isMusicPlay)
		{
			isMusicPlay = false;
			v.setBackgroundResource(R.drawable.music_play);
			mediaPlayer.pause();
		} else
		{
			isMusicPlay = true;
			v.setBackgroundResource(R.drawable.music_pause);
			mediaPlayer.start();
		}
	}

	private void initBluetooth()
	{
		bleLink.setBleListener(new BleLink.BleListener() {
			@Override
			public void OnDeviceStateChanged(BleLink bleLink, BleLink.DeviceState state) {

			}

			@Override
			public void OnDataReceived(BleLink bleLink, byte[] data) {
				int orderCode=data[5];
				int drumId=MyUtil.byte2Int(data[6]);//content.get(0)
				if (orderCode == 0x02)
				{
					if (!isSlience)
						play(drumId);
				}
			}
		});
		// 设置乐器为鼓
		if(bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x00, 'D')))
			MyUtil.showToast(this,"已设置乐器鼓");
		else
		{
			bleLink.dislink();
			MyUtil.showToast(this,"蓝牙连接断开");
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		FlashLight.isAutoFlash = false;
		soundPool.release();
		//bleLink.dislink();
		if (mediaPlayer != null)
			mediaPlayer.release();
		mediaPlayer = null;
	}

	private int previousButton = -1;
	private int streamID;

	private void play(int button)
	{
		FlashLight.isBegin = true;
		if (button != previousButton)
		{
			previousButton = button;
			index = 0;
		}
		// soundPool.stop(streamID);
		switch (button)
		{
			case 0:
				streamID = soundPool.play(soundIdHashMap.get(files[0]), 1, 1, 1, 0,
						1);
				break;
			case 1:
				streamID = soundPool.play(soundIdHashMap.get(files[1]), 1, 1, 1, 0,
						1);
				break;
			case 2:
				streamID = soundPool.play(soundIdHashMap.get(files[2]), 1, 1, 1, 0,
						1);
				break;
			case 3:
				streamID = soundPool
						.play(soundIdHashMap.get(files[3]), 1, 1, 1, 0, 1);
				break;
			case 4:
				streamID = soundPool.play(soundIdHashMap.get(files[4]), 1, 1, 1, 0,
						1);
				break;
			default:
				break;
		}
		if (button < 5)
		{
			view = this.button[button];
			handler.removeCallbacks(myRunnable);
			handler.removeCallbacks(myRunnable2);
			handler.post(myRunnable2);
			handler.postDelayed(myRunnable, 100);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			int id = v.getId();
			for (int i = 0; i < button.length; i++)
			{
				if (this.id[i] == id)
				{
					// play(i);
					index = 0;
					previousButton = i;
					play(previousButton);
					break;
				}
			}
			// v.performClick();
			// v.setScaleX(0.95f);
			// v.setScaleY(0.95f);
			// v.invalidate();
		}
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			// v.setScaleX(1f);
			// v.setScaleY(1f);
			// v.invalidate();
		}
		return false;
	}

	private View view;
	private final Runnable myRunnable = new Runnable()
	{

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			view.setScaleX(1f);
			view.setScaleY(1f);
			view.invalidate();
		}
	};
	private final Runnable myRunnable2 = new Runnable()
	{

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			view.setScaleX(0.95f);
			view.setScaleY(0.95f);
			view.invalidate();
		}
	};
}
