package com.zhy.baiduyun.im;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.RemoteViews;

import com.baidu.frontia.FrontiaApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zhy.baiduyun.im.client.PushMessageReceiver;
import com.zhy.baiduyun.im.dao.MessageDB;
import com.zhy.baiduyun.im.dao.UserDB;
import com.zhy.baiduyun.im.server.BaiduPush;
import com.zhy.baiduyun.im.utils.SharePreferenceUtil;

public class PushApplication extends FrontiaApplication
{
	/**
	 * API_KEY
	 */
	public final static String API_KEY = "UR32D3EODa3ZF59fkHqZDpDz";
	/**
	 * SECRET_KEY
	 */
	public final static String SECRIT_KEY = "NWkNW0kcCT3ZNUSyeBnKhB2UFu7C9UkZ";
	public static final String SP_FILE_NAME = "push_msg_sp";
	/**
	 * Ԥ�����ͷ��
	 */
	public static final int[] heads = { R.drawable.h0, R.drawable.h1,
			R.drawable.h2, R.drawable.h3, R.drawable.h4, R.drawable.h5,
			R.drawable.h6, R.drawable.h7, R.drawable.h8, R.drawable.h9,
			R.drawable.h10, R.drawable.h11, R.drawable.h12, R.drawable.h13,
			R.drawable.h14, R.drawable.h15, R.drawable.h16, R.drawable.h17,
			R.drawable.h18 };
	/**
	 * ����ҳ��
	 */

	private static PushApplication mApplication;

	private BaiduPush mBaiduPushServer;

	private SharePreferenceUtil mSpUtil;

	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private Gson mGson;

	private UserDB userDB;
	private MessageDB messageDB;

	public synchronized static PushApplication getInstance()
	{
		return mApplication;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		mApplication = this;
		initData();
	}

	private void initData()
	{
		mBaiduPushServer = new BaiduPush(BaiduPush.HTTP_METHOD_POST,
				SECRIT_KEY, API_KEY);
		// ��ת��û�� @Expose ע����ֶ�
		mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		mSpUtil = new SharePreferenceUtil(this, SP_FILE_NAME);
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		userDB = new UserDB(this);
		messageDB = new MessageDB(this);
	}

	public synchronized BaiduPush getBaiduPush()
	{
		if (mBaiduPushServer == null)
			mBaiduPushServer = new BaiduPush(BaiduPush.HTTP_METHOD_POST,
					SECRIT_KEY, API_KEY);
		return mBaiduPushServer;
	}

	public synchronized Gson getGson()
	{
		if (mGson == null)
			// ��ת��û�� @Expose ע����ֶ�
			mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
					.create();
		return mGson;
	}

	public NotificationManager getNotificationManager()
	{
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}

	public synchronized SharePreferenceUtil getSpUtil()
	{
		if (mSpUtil == null)
			mSpUtil = new SharePreferenceUtil(this, SP_FILE_NAME);
		return mSpUtil;
	}

	/**
	 * �����һ�ͼ��
	 */
	@SuppressWarnings("deprecation")
	public void showNotification()
	{
		if (!mSpUtil.getMsgNotify())// ����û����ò���ʾ�һ�ͼ�ֱ꣬�ӷ���
			return;

		int icon = R.drawable.notify_general;
		CharSequence tickerText = "�������ں�̨����";

		long when = System.currentTimeMillis();
		mNotification = new Notification(icon, tickerText, when);

		// ������"��������"��Ŀ��
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;

		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.notify_status_bar_latest_event_view);
		contentView.setImageViewResource(R.id.icon,
				heads[mSpUtil.getHeadIcon()]);
		contentView.setTextViewText(R.id.title, mSpUtil.getNick());
		contentView.setTextViewText(R.id.text, tickerText);
		contentView.setLong(R.id.time, "setTime", when);
		// ָ�����Ի���ͼ
		mNotification.contentView = contentView;

		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// ָ��������ͼ
		mNotification.contentIntent = contentIntent;

		mNotificationManager.notify(PushMessageReceiver.NOTIFY_ID,
				mNotification);
	}

	public MessageDB getMessageDB()
	{
		return messageDB;
	}

	public UserDB getUserDB()
	{
		return userDB;
	}

}
