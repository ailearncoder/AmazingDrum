package com.example.panpan.amazingdrum.util;

import java.util.ArrayList;

public class UartDataDeal
{
	private OnReceiveErrorListener onReceiveErrorListener;
	private OnReceivedOKListener onReceivedOKListener;

	public static final int[] head = new int[] { 0xF7, 0x7F };// 帧头
	private final int[] frameLength = new int[2];// 帧长度
	private final int[] orderCode = new int[2];// 命令码
	private final ArrayList<Integer> content = new ArrayList<Integer>();// 内容
	private boolean isCheckEnable = true;// 是否进行校验
	private int checkCode = 0;// 校验码
	// -------------------------------------------------------------------------------
	private int receiveNum = 0;// 接收到的数据数量
	private int stage = 0;// 阶段
	private int contentLength = 0;// 内容长度
	public final int ID = 1;

	public UartDataDeal()
	{

	}

	/**
	 * 接收处理函数
	 */
	public void receive(int receive_data)// 接收处理函数
	{

		receiveNum++;
		switch (stage)
		{
		case 0:
			headDeal(receive_data);// 帧头处理
			break;
		case 1:
			frameLengthDeal(receive_data);// 帧长度处理
			break;
		case 2:
			orderCodeDeal(receive_data);// 命令码处理
			break;
		case 3:
			contentDeal(receive_data);// 内容处理
			break;
		case 4:
			checkCodeDeal(receive_data);// 校验码处理
			break;
		}
	}

	/**
	 * 返回当前接收状态0-4
	 */
	public int getStage()
	{

		return stage;
	}

	/**
	 * 返回接收数据计数
	 */
	public int getReceiveNum()
	{

		return receiveNum;
	}

	/**
	 * 获得数据内容长度
	 */
	public int getContentLength()
	{

		return contentLength;
	}

	/**
	 * 复位
	 */
	public void resetData()
	{

		receiveNum = 0;// 接收到的数据数量
		stage = 0;// 阶段
		contentLength = 0;
	}

	// ↓↓↓↓↓↓↓↓↓↓↓↓以下代码移植自C语言↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	/**
	 * 帧头处理
	 */
	void headDeal(int receive_data)// 帧头处理
	{

		if (receiveNum == 1)
		{
			if (receive_data != head[0])// 校验帧头
			{
				resetData();
				// sendErrorInfo(0x01,receive_data);//（帧头错误）
				// if (onReceiveErrorListener != null)
				// onReceiveErrorListener.OnReceiveError(ErrorType.TYPE_HEAD,
				// "第1位帧头错误:" + receive_data + "<" + head[0] + ">");
				return;
			}
		}
		if (receiveNum == 2)
		{
			if (receive_data != head[1])
			{
				resetData();
				// sendErrorInfo(0x01,receive_data);//（帧头错误）
				// if (onReceiveErrorListener != null)
				// onReceiveErrorListener.OnReceiveError(ErrorType.TYPE_HEAD,
				// "第2位帧头错误:" + receive_data + "<" + head[1] + ">");
				return;
			} else
			// 帧头校验通过
			{
				stage = 1;// 开始帧长度接收
				receiveNum = 0;
			}
		}
	}

	/**
	 * 帧长度处理
	 */
	void frameLengthDeal(int receive_data)// 帧长度处理
	{

		frameLength[receiveNum - 1] = receive_data;
		if (receiveNum == 2)
		{
			stage = 2;// 命令码开始接收
			receiveNum = 0;
		}
	}

	/**
	 * 内容数量判断
	 */
	void contentNumJudge()// 内容数量判断
	{

		int mlength = frameLength[0] * 256 + frameLength[1];
		if (mlength == 7)// 无内容命令
		{
			stage = 4;// 开始校验码接收
			receiveNum = 0;
			return;
		}
		if (mlength > 527 || mlength < 7)
		{
			resetData();
			// sendErrorInfo(0x03,mlength);//（帧内容参数错误）
			if (onReceiveErrorListener != null)
				onReceiveErrorListener.OnReceiveError(ErrorType.TYPE_LENGTH,
						"帧参数长度错误：" + mlength);
			return;
		}
		// while (content.size() != 0)
		// {
		// content.remove(0);
		// }
		contentLength = mlength - 7;
		if (content.size() < contentLength)
		{
			content.ensureCapacity(contentLength);
			content.trimToSize();
		}
	}

	/**
	 * 命令码处理
	 */
	void orderCodeDeal(int receive_data)// 命令码处理
	{

		orderCode[receiveNum - 1] = receive_data;
		if (orderCode[0] != ID)
		{
			resetData();
			// sendErrorInfo(0x02,orderCode[0]);//（ID错误）
			// if (onReceiveErrorListener != null)
			// onReceiveErrorListener.OnReceiveError(ErrorType.TYPE_ID,
			// "ID错误：" + orderCode[0] + "<" + ID + ">");
		}
		if (receiveNum == 2)
		{
			stage = 3;// 内容开始接收
			receiveNum = 0;
			contentNumJudge();// 内容数量判断
		}
	}

	/**
	 * 内容处理
	 */
	void contentDeal(int receive_data)// 内容处理
	{

		if (content == null)
		{
			resetData();
			// sendErrorInfo(0x04,0x00);//（帧内容空指针错误）
			if (onReceiveErrorListener != null)
				onReceiveErrorListener.OnReceiveError(ErrorType.TYPE_CONTENT,
						"空指针错误");
			return;
		}
		if (content.size() < receiveNum)
			content.add(receive_data);
		else
			content.set(receiveNum - 1, receive_data);
		if (receiveNum == contentLength)// 帧内容接收完毕
		{
			stage = 4;// 开始校验码接收
			receiveNum = 0;
		}
	}

	/**
	 * 校验码处理
	 */
	void checkCodeDeal(int receive_data)// 校验码处理
	{

		int index;
		checkCode = head[0] + head[1] + frameLength[0] + frameLength[1]
				+ orderCode[0] + orderCode[1] + 1;
		for (index = 0; index < contentLength; index++)
		{
			checkCode += content.get(index);
		}
		if ((checkCode & 0xFF) == receive_data || !isCheckEnable)
		{
			stage = 0;
			receiveNum = 0;
			// sendErrorInfo(0x00,0x00);//（成功）
			executeOrder();
			contentLength = 0;
		} else
		{
			resetData();
			if (onReceiveErrorListener != null)
				onReceiveErrorListener.OnReceiveError(
						ErrorType.TYPE_CHERK_CODE, "校验码错误：" + receive_data
								+ "<" + (checkCode & 0xFF) + ">");
			// sendErrorInfo(0x05,tempCheck);//（校验码错误）
		}
	}

	/**
	 * 执行命令
	 */
	void executeOrder()// 执行命令
	{

		if (onReceivedOKListener != null)
			onReceivedOKListener.OnReceivedOK(orderCode[1], content,
					contentLength);
	}

	// ↑↑↑↑↑↑↑↑↑↑↑↑↑以上代码移植自C语言↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

	public void setOnReceiveErrorListener(
			OnReceiveErrorListener onReceiveErrorListener)
	{

		this.onReceiveErrorListener = onReceiveErrorListener;
	}

	public void setOnReceivedOKListener(
			OnReceivedOKListener onReceivedOKListener)
	{

		this.onReceivedOKListener = onReceivedOKListener;
	}

	public enum ErrorType
	{
		TYPE_HEAD, TYPE_ID, TYPE_LENGTH, TYPE_ORDER_CODE, TYPE_CONTENT, TYPE_CHERK_CODE
	}

	// 接收数据出现错误监听
	public interface OnReceiveErrorListener
	{
		void OnReceiveError(ErrorType errorType, String msg);
	}

	// 接收数据成功监听
	public interface OnReceivedOKListener
	{
		void OnReceivedOK(int orderCode, ArrayList<Integer> content,
                          int contentLength);
	}

	/**
	 * 设置数据校验开关
	 */
	public void setCheckEnable(boolean isCheckEnable)
	{

		this.isCheckEnable = isCheckEnable;
	}

	/*
	 * 发送命令
	 */
	public static byte[] sendCommand(int id, int command, int... data)
	{

		byte data2[] = new byte[7 + data.length];
		data2[0] = (byte) 0xF7;
		data2[1] = (byte) 0x7F;// 帧头
		data2[2] = (byte) (data2.length >> 8);
		data2[3] = (byte) (data2.length);// 帧长度
		data2[4] = (byte) id;// ID
		data2[5] = (byte) command;// 命令码
		int temp = 0xF7 + 0x7F + data2[2] + data2[3] + id + command + 1;
		int i;
		for (i = 6; i < data2.length - 1; i++)
		{
			data2[i] = (byte) data[i - 6];
			temp += data[i - 6];
		}
		data2[i] = (byte) (temp & 0xFF);// 校验码
		return data2;
	}

	public static byte data2[];

	/**
	 * 发送命令
	 */
	public static byte[] sendCommand(int id, int command)
	{

		if (data2 == null)
			data2 = new byte[7];
		data2[0] = (byte) 0xF7;
		data2[1] = (byte) 0x7F;// 帧头
		data2[2] = (byte) (data2.length >> 8);
		data2[3] = (byte) (data2.length);// 帧长度
		data2[4] = (byte) id;// ID
		data2[5] = (byte) command;// 命令码
		int temp = 0xF7 + 0x7F + data2[2] + data2[3] + id + command + 1;
		data2[6] = (byte) (temp & 0xFF);// 校验码
		return data2;
	}
}
