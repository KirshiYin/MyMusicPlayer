package com.lm.mymusicplayer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	// ���ſ��ư�ť
	private Button btnPre, btnStop, btnPlay, btnPause, btnNext;
	// �����б�listview
	private ListView musiclistview;
	// mediaplayer����
	private MediaPlayer mMediaPlayer;
	// �����б�
	private List<String> mMusicList = new ArrayList<String>();
	// ��ǰ���Ÿ���������
	private int currentPlayItem = 0;
	// ��������·��
	private static final String MUSIC_PATH = "/sdcard/";

	// �������ֲ��Ž�����ʾ
	private SeekBar mSeekBar;
	// ����seekbar�߳�
	private Thread mThread;
	// ������ʾʱ��
	private TextView time1, time2;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			time1.setText(getTime(msg.arg1));
			time2.setText(getTime(msg.arg2));
		}
	};

	OnClickListener buttonclicklistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.pre:
				FrontMusic();
				break;
			case R.id.stop:
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
					mMediaPlayer.reset();
				}
				break;
			case R.id.play:
				playMusic(MUSIC_PATH + mMusicList.get(currentPlayItem));
				break;
			case R.id.pause:
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
				} else {
					mMediaPlayer.start();
				}
				break;
			case R.id.next:
				nextMusic();
				break;
			}
		}
	};

	// ��ʾ�����б�
	void showMusicList() {
		// ȡ��ָ��λ�õ��ļ�������ʾ�������б�
		File home = new File(MUSIC_PATH);
		if ((home.listFiles(new MusicFilter())).length > 0) {
			for (File file : home.listFiles(new MusicFilter())) {
				mMusicList.add(file.getName());
			}
			ArrayAdapter<String> musicListAdapter = new ArrayAdapter<String>(
					MainActivity.this, android.R.layout.simple_list_item_1,
					mMusicList);
			musiclistview.setAdapter(musicListAdapter);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// ����MediaPlayer����
		mMediaPlayer = new MediaPlayer();

		btnPre = (Button) findViewById(R.id.pre);
		btnStop = (Button) findViewById(R.id.stop);
		btnPlay = (Button) findViewById(R.id.play);
		btnPause = (Button) findViewById(R.id.pause);
		btnNext = (Button) findViewById(R.id.next);
		btnPre.setOnClickListener(buttonclicklistener);
		btnStop.setOnClickListener(buttonclicklistener);
		btnPlay.setOnClickListener(buttonclicklistener);
		btnPause.setOnClickListener(buttonclicklistener);
		btnNext.setOnClickListener(buttonclicklistener);

		time1 = (TextView) findViewById(R.id.time1);
		time2 = (TextView) findViewById(R.id.time2);

		mSeekBar = (SeekBar) findViewById(R.id.seekbar);
		mThread = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while (!Thread.currentThread().isInterrupted()) {
					try {
						if (mMediaPlayer != null) {
							setSeekBar(mMediaPlayer);
						}
						Thread.sleep(500);
					} catch (Exception e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		};
		mThread.start();
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int seektotime = 0;

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				if (mMediaPlayer != null) {
					mMediaPlayer.seekTo(seektotime * 1000);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					seektotime = progress;
				}
			}
		});

		musiclistview = (ListView) findViewById(R.id.musiclist);
		musiclistview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				currentPlayItem = position;
				playMusic(MUSIC_PATH + mMusicList.get(position));
			}
		});

		showMusicList();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mMediaPlayer.stop();
		mMediaPlayer.release();
	}

	void playMusic(String path) {
		try {
			// ����MediaPlayer
			mMediaPlayer.reset();
			// ����Ҫ���ŵ��ļ���·��
			mMediaPlayer.setDataSource(path);
			// ׼������
			mMediaPlayer.prepare();
			// ��ʼ����
			mMediaPlayer.start();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					// �������һ��֮�������һ��
					nextMusic();
				}
			});
		} catch (IOException e) {

		}
	}

	void setSeekBar(MediaPlayer mediaplayer) {
		int length = 0;
		int current = 0;
		if (mediaplayer.isPlaying()) {
			length = (mediaplayer.getDuration() / 1000);
			current = (mediaplayer.getCurrentPosition() / 1000);
		}
		mSeekBar.setMax(length);
		mSeekBar.setProgress(current);
		Message msg = new Message();
		msg.arg1 = current;
		msg.arg2 = length;
		mHandler.sendMessage(msg);
	}

	String getTime(int time) {
		int minute = time / 60;
		int second = time % 60;
		return String.format("%02d:%02d", minute, second);
	}

	// ��һ��
	void nextMusic() {
		if (++currentPlayItem >= mMusicList.size()) {
			currentPlayItem = 0;
		} else {
			playMusic(MUSIC_PATH + mMusicList.get(currentPlayItem));
		}
	}

	// ��һ��
	void FrontMusic() {
		if (--currentPlayItem >= 0) {
			currentPlayItem = mMusicList.size();
		} else {
			playMusic(MUSIC_PATH + mMusicList.get(currentPlayItem));
		}
	}

	// �����ļ�����
	class MusicFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".mp3"));
		}
	}
}
