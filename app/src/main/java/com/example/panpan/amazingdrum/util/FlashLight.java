package com.example.panpan.amazingdrum.util;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;

import java.util.List;

/**
 * 闪光灯控制类
 * */
public class FlashLight
{
	private static Camera m_Camera;
	private static Camera.Parameters parameters;
	public static boolean isAutoFlash = false;// 线程是否运行
	public static boolean isBegin = false;// 是否开始自动闪
	private static boolean isFlash = false;// 闪光灯是否开启

	/**
	 * 初始化闪光灯
	 * */
	public static boolean init(Context context)
	{

		PackageManager pm = context.getPackageManager();
		FeatureInfo[] features = pm.getSystemAvailableFeatures();
		for (FeatureInfo f : features)
		{
			if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) // 判断设备是否支持闪光灯
			{
				if (m_Camera == null)
					m_Camera = Camera.open();
				if (m_Camera == null)
					return false;
				parameters = m_Camera.getParameters();
				List<String> flashModes = parameters.getSupportedFlashModes();
				// Check if camera flash exists
				if (flashModes == null)
				{
					// Use the screen as a flashlight (next best thing)
					return false;
				}
				String flashMode = parameters.getFlashMode();
				if (flashMode == null)
					return false;
				break;
			}
		}
		return open();
	}

	public static boolean open()
	{

		isFlash = true;
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		try
		{
			m_Camera.setParameters(parameters);
		} catch (Exception e)
		{
			// TODO: handle exception
			return false;
		}
		return true;
		// m_Camera.startPreview();
	}

	public static boolean close()
	{

		isFlash = false;
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		try
		{
			m_Camera.setParameters(parameters);
		} catch (Exception e)
		{
			// TODO: handle exception
			return false;
		}
		return true;
		// m_Camera.stopPreview();
	}

	/**
	 * 释放内存
	 * */
	public static void release()
	{

		// m_Camera.stopPreview();
		if (m_Camera != null)
			m_Camera.release();
		m_Camera = null;
	}

	public static void startAutoFlash()
	{

		isAutoFlash = true;
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{

				// TODO Auto-generated method stub
				while (isAutoFlash)
				{
					try
					{
						if (isBegin)
						{
							isBegin = false;
							if (!isFlash)
								open();
							Thread.sleep(50);
						}
						if (isFlash)
						{
							close();
						}
						Thread.sleep(10);
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				release();
			}
		});
		thread.setName("闪光灯线程");
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
}
