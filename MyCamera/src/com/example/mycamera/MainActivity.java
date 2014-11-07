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
	 * 程序存放的图片路径是在/sdcard/fatalityUpload这个文件夹
	 */
	private SurfaceView mSurfaceView;
	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private boolean mPreviewRunning;
	private TextView nowLocality;
	private Button exitButton;
	private Button uploadButton;
	private Button paizhaoButton;
	// 实例化一个TelephonyManager 创建android的电话管理
	private TelephonyManager tm;
	private Date today;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
	private String fileName;
	private String lat = "", lng = "";// 经度和纬度
	private String latLongString;
	private GpsThread gpsThread;
	// private DBHelper dbHelper;
	private Cursor cursor;

	/**
	 * 本界面的主函数
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		checkMysoftStage();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_surface);
		// 摄像头界面将通过全屏显示，没有"标题(title)";
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);// 全屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
		this.initMyCamera();
	}

	/**
	 * 检查一下手机设备各项硬件的开启状态
	 */
	public void checkMysoftStage() {
		/*
		 * 先看手机是否已插入sd卡 然后判断sd卡里是不是已经创建了fatalityUpload文件夹用来存储本程序拍下来的照片
		 * 如果没有创建的话就重新在sdcard里创建fatalityUpload文件夹
		 */
		if (existSDcard()) { // 判断手机SD卡是否存在
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
					.setMessage("检查到没有存储卡,请插入手机存储卡再开启本应用")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									finish();
								}
							}).show();
		}
		/*
		 * 此处开始初始化数据库 try { dbHelper = new DBHelper(this); dbHelper.open(this);
		 * cursor = dbHelper.loadAll(); if(!(cursor!=null &&
		 * cursor.getCount()>0)){ dbHelper.initData(); } cursor.close();
		 * dbHelper.close(); } catch (Exception e) { Log.d("save data",
		 * "save data fail"); } finally { this.dbHelper.close(); }
		 */
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) { // 当前网络不可用
			new AlertDialog.Builder(MainActivity.this)
					.setMessage("检查到没有可用的网络连接,请打开网络连接")
					.setPositiveButton("确定",
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
	 * 初始化组件
	 */
	public void initMyCamera() {
		setListensers();
		// TelephonyManager是android的电源通讯的帮助类,通过实例化TelephonyManager来实现操作类似获取本机
		// IME码,手机号等信息
		tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		// 初始化拍摄模块
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		// mSurfaceView.setOnClickListener(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(MainActivity.this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		gpsThread = new GpsThread();
		gpsThread.start();
		// 设置监听器，自动更新的最小时间为间隔1秒，最小位移变化超过3米
		// mLocationManager.requestLocationUpdates(provider, 1000, 3,
		// locationListener);
	}

	/**
	 * 此函数负责两个工作 1.实例化屏幕上的按钮 2.为按钮添加Listener
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
	 * 当预览界面的格式和大小发生改变时，该方法被调用
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
	 * 重点函数 此处实例化了本界面的PictureCallback
	 * 当用户拍完一张照片的时候触发onPictureTaken,这时候对拍下的照片进行相应的处理操作
	 */
	PictureCallback mPictureCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			if (data != null) {
				try {
					today = new Date();
					// 定义fileName 用来设定拍照后的文件名
					// 结构: IMSI号 + 经度+ & + 纬度+ 格式化后的当前时间 (本来结构应该是 手机号 + 精度&纬度 +
					// 格式化后的当前时间)但是因为手机号有获取不到的情况,所以换为了IMSI号
					fileName = tm.getDeviceId() + "-" + lat + "-" + lng + "-"
							+ sdf.format(today); // tm.getLine1Number()获取手机号,这里直接是获取不到的,需要运营商的API解析
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
					// 然后传递图片信息到 图像预览界面
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
	 * 创建Camera对象函数 初次实例化，界面打开时该方法自动调用
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		// Thread openCameraThread = new Thread(
		// new Runnable() {
		// public void run() {
		// mCamera = Camera.open();
		// }
		// }
		// ).start();
		mCamera = Camera.open();// “打开”摄像头
	}

	/**
	 * 当用户进行 点击 操作的时候触发此事件,不过貌似没有起作用,有待测试
	 */
	public void onClick(View v) {
		mCamera.takePicture(mShutterCallback, null, mPictureCallback);
	}

	/**
	 * 在相机快门关闭时候的回调接口，通过这个接口来通知用户快门关闭的事件，
	 * 普通相机在快门关闭的时候都会发出响声，根据需要可以在该回调接口中定义各种动作， 例如：使设备震动
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
	 * 销毁函数 当预览界面被关闭时，该方法被调用
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
		mCamera = null;
	}

	/*
	 * 点击屏幕上的"退出"键时触发该Listener监听此按钮的动作
	 */
	private OnClickListener clickExitButton = new OnClickListener() {
		public void onClick(View v) {
			new AlertDialog.Builder(MainActivity.this)
					.setTitle("提示")
					.setMessage("确定退出?")
					.setPositiveButton("确定",
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
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// 取消按钮事件
								}
							}).show();
		}
	};
	/*
	 * 点击屏幕上的"上传"键时触发该Listener监听此按钮的动作
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
	 * 点击屏幕上的"拍照"键时触发该Listener监听此按钮的动作
	 */
	private OnClickListener clickShootButton = new OnClickListener() {
		public void onClick(View v) {
			showDialog(0);
			mCamera.takePicture(mShutterCallback, null, mPictureCallback);
		}
	};
	// 声明三个menu键带出来的按钮
	public static final int ITEM_1_ID = Menu.FIRST;
	public static final int ITEM_2_ID = Menu.FIRST + 1;
	public static final int ITEM_3_ID = Menu.FIRST + 2;

	// 初始化Menu菜单,用户按下menu键时自动触发
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ITEM_1_ID, 0, "设置");
		menu.add(0, ITEM_2_ID, 1, "关于");
		menu.add(0, ITEM_3_ID, 2, "退出");
		return true;
	}

	/**
	 * 处理用户按下menu键时的操作
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1: // 调出系统设置界面
			ComponentName cn = new ComponentName("com.android.settings",
					"com.android.settings.Settings");
			Intent intent = new Intent();
			intent.setComponent(cn);
			intent.setAction("android.intent.action.VIEW");
			startActivity(intent);
			return true;
		case 2: // 关于
			showDialog(1);
			return true;
		case 3: // 退出
			new AlertDialog.Builder(this)
					.setTitle("提示")
					.setMessage("确定退出?")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									setResult(RESULT_OK);// 确定按钮事件
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
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// 取消按钮事件
								}
							}).show();
			return true;
		}
		return false;
	}

	/**
	 * 当用户按下手机实体按键时触发 如果按下的是拍照键的话返回一个true,其他都是false
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			return super.onKeyDown(keyCode, event);
		} else
			return false;
	}

	/**
	 * 判断存储卡是否存在
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
	 * 相机自动对焦函数
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
					.setTitle("关于拍照上传软件")
					.setMessage(
							Html.fromHtml("<font color=#E43E07 >程序功能介绍：</font><p>本程序用于图片拍摄及上传图片等功能</p>"
									+ "<font color=#E43E07 >注意：</font><p>使用本程序前，请先开启用户手机的GPS及无线网络。(拍照界面中点击 menu键->设置->安全与位置/无限网络 开启GPS及无限网络)</p>"
									+ "<p align=center color=#767676 size=12px>PETER制作 版权所有</p>"))
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									removeDialog(1);
								}
							}).create();
		}
		case 0: {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("处理中，请稍后...");
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
						.setTitle("提示")
						.setMessage("检测到GPS/A-GPS没有开启 /n 点击 确定 进入系统设置，点击 取消 结束")
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/*
										 * 转到设置界面
										 */
										Intent fireAlarm = new Intent(
												"android.settings.LOCATION_SOURCE_SETTINGS");
										fireAlarm
												.addCategory(Intent.CATEGORY_DEFAULT);
										startActivity(fireAlarm);
									}
								})
						.setNegativeButton("取消",
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
						.setTitle("提示")
						.setMessage("无法获得当前位置,稍后将会重试")
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
	 * 手机的GPS线程
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
				latLongStr = "纬度:" + latitude + "/n经度:" + longitude + "/n速度:"
						+ v + " m/s , " + v * 3.6 + " km/h";
				location = slocation;
				preTime = System.currentTimeMillis();
			} else {
				latLongStr = "无法获取地理信息";
			}
		}

		private LocationListener locationListener = new LocationListener() {
			// 底层获得的位置会通过这个接口上报给应用
			public void onLocationChanged(Location location) {
				message.what = 0;
				updateWithNewLocation(location);
				// Log.d("Thread Test",
				// "*************************GPS IS OPEN!");
			}

			// Provider被disable时触发此函数，比如GPS被关闭
			public void onProviderDisabled(String provider) {
				message.what = 1;
				// Log.d("Thread Test",
				// "*************************GPS IS CLOSED!");
			}

			// Provider被enable时触发此函数，比如GPS被打开
			public void onProviderEnabled(String provider) {
			}

			/*
			 * 位置服务状态的变化通过这个接口上报 Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
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
			// 此处实例化了 手机的本地位置服务 的一个对象
			mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);// 设置为最大精度
			criteria.setAltitudeRequired(false);// 不要求海拔信息
			criteria.setBearingRequired(false);// 不要求方位信息
			criteria.setCostAllowed(true);// 是否允许付费
			criteria.setPowerRequirement(Criteria.POWER_LOW);// 对电量的要求
			String provider = mLocationManager.getBestProvider(criteria, true);
			while (!Thread.currentThread().isInterrupted()) {
				mLocationManager.requestLocationUpdates(provider, 1000, 1,
						locationListener);
				location = mLocationManager.getLastKnownLocation(provider);
				// updateWithNewLocation(location); // 更新位置
				while (location == null) {
					preTime = System.currentTimeMillis();
					// 刷新Provider信息
					mLocationManager.requestLocationUpdates(provider, 1000, 1,
							locationListener);
					// 获得最新的位置数据
					location = mLocationManager.getLastKnownLocation(provider);
					updateWithNewLocation(location); // 更新位置
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					message = new Message();
					updateWithNewLocation(location); // 更新位置
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