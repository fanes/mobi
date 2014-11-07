package com.yichang.qianlichuanyin.main;

import java.util.Arrays;

import com.google.gson.Gson;
import com.yichang.qianlichuanyin.net.Client;
import com.yichang.qianlichuanyin.net.ControlMessage;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class RecordThread implements Runnable {
	private boolean stopRecord = true;
	private AudioRecord audioRecord = null;
	private Client client;
	private Gson gson = new Gson();
	private int toUserId;

	public RecordThread(int toUserId) {
		// this.audioRecord=audioRecord;
		this.toUserId = toUserId;
		this.client = FirstActivity.control.getClient();
	}

	@Override
	public void run() {
		ControlMessage mesg;
		String jsonStr;
		byte[] buffer;
		byte[] realBuffer;
		int bufferSize = Const.RecordBufferSize;
		int realSize;
		stopRecord = true;

		// 为空才初始化
		if (audioRecord == null) {
			initAudioRecord();
		}

		audioRecord.startRecording();
		while (stopRecord) {
			try {
				Thread.sleep(Const.RecordTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			buffer = new byte[bufferSize]; // short类型对应16bit音频数据格式，byte类型对应于8bit
			try {
				realSize = audioRecord.read(buffer, 0, bufferSize); // 返回值是个int类型的数据长度值

				// 发送消息给服务器,只取有效数组即可
				realBuffer = Arrays.copyOfRange(buffer, 0, realSize);

				// Log.v("test", "声音包字节数"+realSize);

				// Log.v("test", "语音包发出前十个包的内容为" + buffer[0] + buffer[150]
				// + buffer[800] + buffer[900]);

				jsonStr = gson.toJson(realBuffer);
				mesg = new ControlMessage(Const.soundMessage, jsonStr, 0,
						toUserId);
				client.sendMessage(mesg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void initAudioRecord() {
		// 获取缓冲区最小的字节数，以免设定的缓冲区太小
		int minSize = AudioRecord.getMinBufferSize(Const.RateInHz,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		Log.v("test", "录音minSize" + minSize);

		/*
		 * 初始化录音对象 audioSource： 录音源
		 * ,sampleRateInHz：默认的采样频率,channelConfig：描述音频通道设置 audioFormat：音频数据支持格式
		 * 单/双通道, bufferSizeInBytes： 缓冲区的总数（字节）
		 */
		try {
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
					Const.RateInHz, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, Const.RecordBufferSize);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 停下整个录音线程
	 */
	public void stopRecord() {
		stopRecord = false;
		stopAudioRecord();
	}

	/**
	 * 暂停录音对象的录音，并释放录音资源
	 */
	public void stopAudioRecord() {
		if (audioRecord != null) {
			audioRecord.stop();
			audioRecord.release();
			audioRecord = null;
		}
	}

}
