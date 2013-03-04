package com.lm.mymusicplayer.service;

import java.io.IOException;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.lm.mymusicplayer.R;
import com.lm.mymusicplayer.view.MusicPlayActivity;

/**
 * ���ַ���Service,�ڸ�Service��������ֵĲ��ż����ſ��ƹ���
 * 
 * @author liumeng
 */
public class MusicService extends Service implements
		MediaPlayer.OnCompletionListener {
	// ��ǰ����ʱ��
	private static final String MUSIC_CURRENT = "music.currenttime";
	// ������ʱ��
	private static final String MUSCI_DURATION = "music.duration";
	//
	private static final String MUSIC_UPDATE = "music.update";
	// �����б�
	private static final String MUSIC_LIST = "music.list";
	// ����
	private static final int MUSIC_PLAY = 1;
	// ��ͣ
	private static final int MUSIC_PAUSE = 2;
	// ֹͣ
	private static final int MUSIC_STOP = 3;
	// ��һ��
	private static final int MUSIC_NEXT = 4;
	// ��һ��
	private static final int MUSIC_PREVIOUS = 5;
	// ���Ž��ȸı�
	private static final int MUSIC_PROGRESS_CHANGE = 6;
	// ������
	private String _title;
	// ������
	private String _artist;
	// ���ֲ��ŵ���λ��
	private int _position;
	// �����ļ���·��
	private String _path;

	private Handler mHandler = null;

	// MediaPlayer����
	private MediaPlayer mMediaPlayer = null;
	private Uri mUri = null;
	// ��ǰ����ʱ��
	private int currentTime;
	// ��ʱ��
	private int duration;
	// ֪ͨ����ʾ��ǰ��������
	public static Notification mNotification;
	public static NotificationManager mNotificationManager;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		// ʵ����MediaPlayer
		mMediaPlayer = new MediaPlayer();
		// ���ò������ʱ����
		mMediaPlayer.setOnCompletionListener(this);

		// ��ʾ֪ͨ��
		showNotification();

		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.ANSWER");
		// ע���������
		registerReceiver(PhoneListener, filter);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// ���֪ͨ����Ϣ
		mNotificationManager.cancelAll();
		if (mMediaPlayer != null) {
			// ֹͣ����
			mMediaPlayer.stop();
			mMediaPlayer = null;
		}
		if (mHandler != null) {
			// �Ƴ���Ϣ
			mHandler.removeMessages(1);
			mHandler = null;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Bundle bundle = intent.getExtras();
		_title = bundle.getString("_title");
		_artist = bundle.getString("_artist");
		_position = bundle.getInt("_position");
		_path = bundle.getString("_path");

		if ((_path != null) && (mMediaPlayer != null)) {
			try {
				// ����ý��·��
				mMediaPlayer.setDataSource(_path);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		setup();
		init();

		int op = bundle.getInt("_op", -1);
		if (op != -1) {
			switch (op) {
			case MUSIC_PLAY:
				// ����
				if (mMediaPlayer != null) {
					play();
				}
				break;
			case MUSIC_PAUSE:
				// ��ͣ
				if (mMediaPlayer.isPlaying()) {
					pause();
				}
				break;
			case MUSIC_STOP:
				// ֹͣ
				stop();
				break;
			case MUSIC_PROGRESS_CHANGE:
				// �������ı�
				currentTime = bundle.getInt("_progress");
				mMediaPlayer.seekTo(currentTime);
				break;
			}
		}
		showNotification();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCompletion(MediaPlayer mediaplayer) {
		// TODO Auto-generated method stub
		// nextOne();�˴�����Ҫ�޸�
		stop();
	}

	/**
	 * ��ʼ��,����duration����
	 */
	private void setup() {
		final Intent intent = new Intent();
		intent.setAction(MUSCI_DURATION);
		try {
			if (!mMediaPlayer.isPlaying()) {
				mMediaPlayer.prepare();
			}
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					// TODO Auto-generated method stub
					mHandler.sendEmptyMessage(1);
				}
			});
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		duration = mMediaPlayer.getDuration();
		intent.putExtra("_duration", duration);
		// ��MusicPlayActivity���͹㲥,��֪��ǰ��Ƶ�ļ���ʱ��
		sendBroadcast(intent);
	}

	/**
	 * ��ʼ��,����currentTime���ü����� ����mHandler��ʼ��
	 */
	private void init() {
		final Intent intent = new Intent();
		intent.setAction(MUSIC_CURRENT);
		if (mHandler == null) {
			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					if (msg.what == 1) {
						currentTime = mMediaPlayer.getCurrentPosition();
						intent.putExtra("_currentTime", currentTime);
						sendBroadcast(intent);
					}
					mHandler.sendEmptyMessageDelayed(1, 500);
				}
			};
		}
	}

	/**
	 * ����
	 */
	private void play() {
		if (mMediaPlayer != null) {
			mMediaPlayer.start();
		}
	}

	/**
	 * ��ͣ
	 */
	private void pause() {
		if (mMediaPlayer != null) {
			mMediaPlayer.pause();
		}
	}

	/**
	 * ֹͣ
	 */
	private void stop() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mHandler.removeMessages(1);
		}
	}

	/**
	 * �����ֲ���ʱ,֪ͨ����ʾ��ǰ������Ϣ
	 */
	private void showNotification() {
		// ��ȡ֪ͨ��ϵͳ�������
		mNotificationManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Builder builder = new Notification.Builder(MusicService.this);
		// ����֪ͨ��Ҫ��ʾ������
		builder.setSmallIcon(R.drawable.beats_logo_s);
		builder.setContentTitle(_title);
		builder.setContentText(_artist);
		builder.setAutoCancel(false);
		builder.setWhen(System.currentTimeMillis());

		_position = getCurrentTime();
		Intent intent = new Intent(MusicService.this, MusicPlayActivity.class);
		intent.putExtra("_title", _title);
		intent.putExtra("_artist", _artist);
		intent.putExtra("_path", _path);
		intent.putExtra("_position", _position);
		PendingIntent contentIntent = PendingIntent
				.getActivity(MusicService.this, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(contentIntent);
		mNotification = builder.getNotification();
		mNotificationManager.notify(0, mNotification);
	}

	/**
	 * @return ��ǰ���Ž���
	 */
	private int getCurrentTime() {
		int currenttime = 0;
		if (mMediaPlayer != null) {
			currenttime = mMediaPlayer.getCurrentPosition();
		}
		return currenttime;
	}

	/**
	 * �������
	 */
	protected BroadcastReceiver PhoneListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(Intent.ACTION_ANSWER)) {
				TelephonyManager telephonymanager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				switch (telephonymanager.getCallState()) {
				case TelephonyManager.CALL_STATE_RINGING:
					// ������ʱ��ͣ����
					pause();
				case TelephonyManager.CALL_STATE_OFFHOOK:
					// ����֮��ָ����ֲ���
					play();
				}
			}
		}
	};
}
