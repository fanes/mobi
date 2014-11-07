package com.example.mycamera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements SurfaceHolder.Callback,
		OnClickListener {
	/**
	 * �����ŵ�ͼƬ·������/sdcard/fatalityUpload����ļ���
	 */
	private SurfaceView mSurfaceView;
	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private boolean mPreviewRunning;
	private TextView nowLocality;
	private Button exitButton;
	private Button uploadButton;
	private Button paizhaoButton;
	// ʵ����һ��TelephonyManager ����android�ĵ绰����
	private TelephonyManager tm;
	private Date today;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
	private String fileName;
	private String lat = "", lng = "";// ���Ⱥ�γ��
	private String latLongString;
	private GpsThread gpsThread;
	// private DBHelper dbHelper;
	private Cursor cursor;

	/**
	 * �������������
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		checkMysoftStage();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_surface);
		// ����ͷ���潫ͨ��ȫ����ʾ��û��"����(title)";
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);// ȫ��
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// ����
		this.initMyCamera();
	}

	/**
	 * ���һ���ֻ��豸����Ӳ���Ŀ���״̬
	 */
	public void checkMysoftStage() {
		/*
		 * �ȿ��ֻ��Ƿ��Ѳ���sd�� Ȼ���ж�sd�����ǲ����Ѿ�������fatalityUpload�ļ��������洢����������������Ƭ
		 * ���û�д����Ļ���������sdcard�ﴴ��fatalityUpload�ļ���
		 */
		if (existSDcard()) { // �ж��ֻ�SD���Ƿ����
			if (new File("/sdcard").canRead()) {
				File file = new File("sdcard/fatalityUpload");
				if (!file.exists()) {
					file.mkdir();
					file = new File("sdcard/fatalityUpload/Thumbnail_fatality");
					file.mkdir();
					file = new File("sdcard/fatalityUpload/fatality");
					file.mkdir();
				}
			}
		} else {
			new AlertDialog.Builder(this)
					.setMessage("��鵽û�д洢��,������ֻ��洢���ٿ�����Ӧ��")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									finish();
								}
							}).show();
		}
		/*
		 * �˴���ʼ��ʼ�����ݿ� try { dbHelper = new DBHelper(this); dbHelper.open(this);
		 * cursor = dbHelper.loadAll(); if(!(cursor!=null &&
		 * cursor.getCount()>0)){ dbHelper.initData(); } cursor.close();
		 * dbHelper.close(); } catch (Exception e) { Log.d("save data",
		 * "save data fail"); } finally { this.dbHelper.close(); }
		 */
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) { // ��ǰ���粻����
			new AlertDialog.Builder(MainActivity.this)
					.setMessage("��鵽û�п��õ���������,�����������")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									ComponentName cn = new ComponentName(
											"com.android.settings",
											"com.android.settings.Settings");
									Intent intent = new Intent();
									intent.setComponent(cn);
									intent.setAction("android.intent.action.VIEW");
									startActivity(intent);
									// finish();
								}
							}).show();
		}
	}

	/**
	 * ��ʼ�����
	 */
	public void initMyCamera() {
		setListensers();
		// TelephonyManager��android�ĵ�ԴͨѶ�İ�����,ͨ��ʵ����TelephonyManager��ʵ�ֲ������ƻ�ȡ����
		// IME��,�ֻ��ŵ���Ϣ
		tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		// ��ʼ������ģ��
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		// mSurfaceView.setOnClickListener(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(MainActivity.this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		gpsThread = new GpsThread();
		gpsThread.start();
		// ���ü��������Զ����µ���Сʱ��Ϊ���1�룬��Сλ�Ʊ仯����3��
		// mLocationManager.requestLocationUpdates(provider, 1000, 3,
		// locationListener);
	}

	/**
	 * �˺��������������� 1.ʵ������Ļ�ϵİ�ť 2.Ϊ��ť���Listener
	 */
	private void setListensers() {
		nowLocality = (TextView) findViewById(R.id.nowLocality);
		exitButton = (Button) findViewById(R.id.exitPro);
		uploadButton = (Button) findViewById(R.id.uploadPhoto);
		paizhaoButton = (Button) findViewById(R.id.paizhao);
		exitButton.setOnClickListener(clickExitButton);
		uploadButton.setOnClickListener(clickUploadButton);
		paizhaoButton.setOnClickListener(clickShootButton);
	}

	/**
	 * ��Ԥ������ĸ�ʽ�ʹ�С�����ı�ʱ���÷���������
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}
		Camera.Parameters p = mCamera.getParameters();
		p.setPreviewSize(w, h);
		mCamera.setParameters(p);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	/**
	 * �ص㺯�� �˴�ʵ�����˱������PictureCallback
	 * ���û�����һ����Ƭ��ʱ�򴥷�onPictureTaken,��ʱ������µ���Ƭ������Ӧ�Ĵ������
	 */
	PictureCallback mPictureCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			if (data != null) {
				try {
					today = new Date();
					// ����fileName �����趨���պ���ļ���
					// �ṹ: IMSI�� + ����+ & + γ��+ ��ʽ����ĵ�ǰʱ�� (�����ṹӦ���� �ֻ��� + ����&γ�� +
					// ��ʽ����ĵ�ǰʱ��)������Ϊ�ֻ����л�ȡ���������,���Ի�Ϊ��IMSI��
					fileName = tm.getDeviceId() + "-" + lat + "-" + lng + "-"
							+ sdf.format(today); // tm.getLine1Number()��ȡ�ֻ���,����ֱ���ǻ�ȡ������,��Ҫ��Ӫ�̵�API����
					Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
							data.length);
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(String.format(
									"sdcard/fatalityUpload/fatality/"
											+ fileName + ".jpg",
									System.currentTimeMillis())));
					bm.compress(Bitmap.CompressFormat.JPEG, 60, bos);
					bos.flush();
					bos.close();
					BufferedOutputStream bos1 = new BufferedOutputStream(
							new FileOutputStream(String.format(
									"sdcard/fatalityUpload/Thumbnail_fatality/"
											+ fileName + ".jpg",
									System.currentTimeMillis())));
					Bitmap bm1 = Bitmap.createScaledBitmap(bm, 100, 100, false);
					bm1.compress(Bitmap.CompressFormat.JPEG, 100, bos1);
					bos1.flush();
					bos1.close();
					removeDialog(0);
					// ByteArrayOutputStream baos = new ByteArrayOutputStream();
					// bm.compress(Bitmap.CompressFormat.PNG, 30, baos);
					// byte[] newData = baos.toByteArray();
					// Ȼ�󴫵�ͼƬ��Ϣ�� ͼ��Ԥ������
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					// bundle.putByteArray("picPre", newData);
					bundle.putString("picPath",
							"/sdcard/fatalityUpload/fatality/" + fileName
									+ ".jpg");
					// intent.putExtra("picPre", data);
					intent.putExtras(bundle);
					intent.setClass(MainActivity.this, PhotoPreview.class);
					// if (mLocationManager != null) {
					// mLocationManager.removeUpdates(locationListener);
					// }
					if (!gpsThread.isInterrupted()) {
						gpsThread.stopGspListener();
						gpsThread.interrupt();
					}
					finish();
					startActivity(intent);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * ����Camera������ ����ʵ�����������ʱ�÷����Զ�����
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		// Thread openCameraThread = new Thread(
		// new Runnable() {
		// public void run() {
		// mCamera = Camera.open();
		// }
		// }
		// ).start();
		mCamera = Camera.open();// ���򿪡�����ͷ
	}

	/**
	 * ���û����� ��� ������ʱ�򴥷����¼�,����ò��û��������,�д�����
	 */
	public void onClick(View v) {
		mCamera.takePicture(mShutterCallback, null, mPictureCallback);
	}

	/**
	 * ��������Źر�ʱ��Ļص��ӿڣ�ͨ������ӿ���֪ͨ�û����Źرյ��¼���
	 * ��ͨ����ڿ��Źرյ�ʱ�򶼻ᷢ��������������Ҫ�����ڸûص��ӿ��ж�����ֶ����� ���磺ʹ�豸��
	 */
	ShutterCallback mShutterCallback = new ShutterCallback() {
		public void onShutter() {
			// just log ,do nothing
			Log.d("ShutterCallback", "...onShutter...");
		}
	};

	// PictureCallback rowCallback = new PictureCallback(){
	// public void onPictureTaken(byte[] data, Camera camera) {
	//
	// }
	// };
	/**
	 * ���ٺ��� ��Ԥ�����汻�ر�ʱ���÷���������
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
		mCamera = null;
	}

	/*
	 * �����Ļ�ϵ�"�˳�"��ʱ������Listener�����˰�ť�Ķ���
	 */
	private OnClickListener clickExitButton = new OnClickListener() {
		public void onClick(View v) {
			new AlertDialog.Builder(MainActivity.this)
					.setTitle("��ʾ")
					.setMessage("ȷ���˳�?")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// if (mLocationManager != null) {
									// mLocationManager
									// .removeUpdates(locationListener);
									// }
									if (!gpsThread.isInterrupted()) {
										gpsThread.stopGspListener();
										gpsThread.interrupt();
									}
									finish();
								}
							})
					.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// ȡ����ť�¼�
								}
							}).show();
		}
	};
	/*
	 * �����Ļ�ϵ�"�ϴ�"��ʱ������Listener�����˰�ť�Ķ���
	 */
	private OnClickListener clickUploadButton = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, PictrueView.class);// PhotoView
			// if (mLocationManager != null) {
			// mLocationManager.removeUpdates(locationListener);
			// }
			if (!gpsThread.isInterrupted()) {
				gpsThread.stopGspListener();
				gpsThread.interrupt();
			}
			finish();
			startActivity(intent);
		}
	};
	/*
	 * �����Ļ�ϵ�"����"��ʱ������Listener�����˰�ť�Ķ���
	 */
	private OnClickListener clickShootButton = new OnClickListener() {
		public void onClick(View v) {
			showDialog(0);
			mCamera.takePicture(mShutterCallback, null, mPictureCallback);
		}
	};
	// ��������menu���������İ�ť
	public static final int ITEM_1_ID = Menu.FIRST;
	public static final int ITEM_2_ID = Menu.FIRST + 1;
	public static final int ITEM_3_ID = Menu.FIRST + 2;

	// ��ʼ��Menu�˵�,�û�����menu��ʱ�Զ�����
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ITEM_1_ID, 0, "����");
		menu.add(0, ITEM_2_ID, 1, "����");
		menu.add(0, ITEM_3_ID, 2, "�˳�");
		return true;
	}

	/**
	 * �����û�����menu��ʱ�Ĳ���
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1: // ����ϵͳ���ý���
			ComponentName cn = new ComponentName("com.android.settings",
					"com.android.settings.Settings");
			Intent intent = new Intent();
			intent.setComponent(cn);
			intent.setAction("android.intent.action.VIEW");
			startActivity(intent);
			return true;
		case 2: // ����
			showDialog(1);
			return true;
		case 3: // �˳�
			new AlertDialog.Builder(this)
					.setTitle("��ʾ")
					.setMessage("ȷ���˳�?")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									setResult(RESULT_OK);// ȷ����ť�¼�
									// if (mLocationManager != null) {
									// mLocationManager
									// .removeUpdates(locationListener);
									// }
									if (!gpsThread.isInterrupted()) {
										gpsThread.stopGspListener();
										gpsThread.interrupt();
									}
									finish();
								}
							})
					.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// ȡ����ť�¼�
								}
							}).show();
			return true;
		}
		return false;
	}

	/**
	 * ���û������ֻ�ʵ�尴��ʱ���� ������µ������ռ��Ļ�����һ��true,��������false
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			return super.onKeyDown(keyCode, event);
		} else
			return false;
	}

	/**
	 * �жϴ洢���Ƿ����
	 * 
	 * @return
	 */
	public boolean existSDcard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

	/**
	 * ����Զ��Խ�����
	 */
	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1: {
			return new AlertDialog.Builder(MainActivity.this)
					.setIcon(R.drawable.ic_menu_info_details)
					.setTitle("���������ϴ����")
					.setMessage(
							Html.fromHtml("<font color=#E43E07 >�����ܽ��ܣ�</font><p>����������ͼƬ���㼰�ϴ�ͼƬ�ȹ���</p>"
									+ "<font color=#E43E07 >ע�⣺</font><p>ʹ�ñ�����ǰ�����ȿ����û��ֻ���GPS���������硣(���ս����е�� menu��->����->��ȫ��λ��/�������� ����GPS����������)</p>"
									+ "<p align=center color=#767676 size=12px>PETER���� ��Ȩ����</p>"))
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									removeDialog(1);
								}
							}).create();
		}
		case 0: {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("�����У����Ժ�...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			return dialog;
		}
		}
		return null;
	}

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Bundle bundle = msg.getData();
				// Log.d("Thread Test",
				// "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<handler get message"+msg.what);
				lat = bundle.getString("lat");
				lng = bundle.getString("lng");
				latLongString = bundle.getString("latLongString");
				// Log.d("handler Text",
				// "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+latLongString+" lat:"+lat+" lng:"+lng);
				nowLocality.setText(System.currentTimeMillis() + ":/n"
						+ latLongString);
				break;
			case 1:
				AlertDialog dialog1 = new AlertDialog.Builder(MainActivity.this)
						.setTitle("��ʾ")
						.setMessage("��⵽GPS/A-GPSû�п��� /n ��� ȷ�� ����ϵͳ���ã���� ȡ�� ����")
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/*
										 * ת�����ý���
										 */
										Intent fireAlarm = new Intent(
												"android.settings.LOCATION_SOURCE_SETTINGS");
										fireAlarm
												.addCategory(Intent.CATEGORY_DEFAULT);
										startActivity(fireAlarm);
									}
								})
						.setNegativeButton("ȡ��",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										if (!gpsThread.isInterrupted()) {
											gpsThread.stopGspListener();
											gpsThread.interrupt();
										}
										finish();
									}
								}).create();
				dialog1.show();
				break;
			case 2:
				AlertDialog dialog2 = new AlertDialog.Builder(MainActivity.this)
						.setTitle("��ʾ")
						.setMessage("�޷���õ�ǰλ��,�Ժ󽫻�����")
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										// finish();
									}
								}).create();
				dialog2.show();
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * �ֻ���GPS�߳�
	 * 
	 * @author peter
	 */
	class GpsThread extends Thread implements Runnable {
		private Looper mLooper;
		private LocationManager mLocationManager;
		private Location location;
		private Message message;
		private long preTime;
		String latLongStr = "", latitude = "", longitude = "";

		public Looper getLooper() {
			return mLooper;
		}

		public void quit() {
			mLooper.quit();
		}

		private void updateWithNewLocation(Location slocation) {
			if (slocation != null) {
				latitude = Double.toString(slocation.getLatitude());
				longitude = Double.toString(slocation.getLongitude());
				long subTime = (System.currentTimeMillis() - preTime) / 1000;
				float v = (subTime == 0 || location == null) ? 0 : (location
						.distanceTo(slocation) / subTime);
				latLongStr = "γ��:" + latitude + "/n����:" + longitude + "/n�ٶ�:"
						+ v + " m/s , " + v * 3.6 + " km/h";
				location = slocation;
				preTime = System.currentTimeMillis();
			} else {
				latLongStr = "�޷���ȡ������Ϣ";
			}
		}

		private LocationListener locationListener = new LocationListener() {
			// �ײ��õ�λ�û�ͨ������ӿ��ϱ���Ӧ��
			public void onLocationChanged(Location location) {
				message.what = 0;
				updateWithNewLocation(location);
				// Log.d("Thread Test",
				// "*************************GPS IS OPEN!");
			}

			// Provider��disableʱ�����˺���������GPS���ر�
			public void onProviderDisabled(String provider) {
				message.what = 1;
				// Log.d("Thread Test",
				// "*************************GPS IS CLOSED!");
			}

			// Provider��enableʱ�����˺���������GPS����
			public void onProviderEnabled(String provider) {
			}

			/*
			 * λ�÷���״̬�ı仯ͨ������ӿ��ϱ� Provider��ת̬�ڿ��á���ʱ�����ú��޷�������״ֱ̬���л�ʱ�����˺���
			 */
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				message.what = 2;
			}
		};

		@SuppressWarnings("static-access")
		public void run() {
			Looper.prepare();
			mLooper = Looper.myLooper();
			// �˴�ʵ������ �ֻ��ı���λ�÷��� ��һ������
			mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);// ����Ϊ��󾫶�
			criteria.setAltitudeRequired(false);// ��Ҫ�󺣰���Ϣ
			criteria.setBearingRequired(false);// ��Ҫ��λ��Ϣ
			criteria.setCostAllowed(true);// �Ƿ�������
			criteria.setPowerRequirement(Criteria.POWER_LOW);// �Ե�����Ҫ��
			String provider = mLocationManager.getBestProvider(criteria, true);
			while (!Thread.currentThread().isInterrupted()) {
				mLocationManager.requestLocationUpdates(provider, 1000, 1,
						locationListener);
				location = mLocationManager.getLastKnownLocation(provider);
				// updateWithNewLocation(location); // ����λ��
				while (location == null) {
					preTime = System.currentTimeMillis();
					// ˢ��Provider��Ϣ
					mLocationManager.requestLocationUpdates(provider, 1000, 1,
							locationListener);
					// ������µ�λ������
					location = mLocationManager.getLastKnownLocation(provider);
					updateWithNewLocation(location); // ����λ��
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					message = new Message();
					updateWithNewLocation(location); // ����λ��
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// Log.d("Thread Test",
				// "*************************Message.what's Value is:"+message.what);
				Bundle data = new Bundle();
				data.putString("latLongString", latLongStr);
				data.putString("lat", latitude);
				data.putString("lng", longitude);
				message.setData(data);
				myHandler.sendMessage(message);
			}
			Looper.loop();
		}

		public void stopGspListener() {
			if (mLocationManager != null) {
				mLocationManager.removeUpdates(locationListener);
			}
		}
	};
}