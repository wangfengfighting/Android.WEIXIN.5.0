package com.zhy.baiduyun.im;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.google.gson.Gson;
import com.zhy.baiduyun.im.bean.Message;
import com.zhy.baiduyun.im.bean.User;
import com.zhy.baiduyun.im.client.PushMessageReceiver;
import com.zhy.baiduyun.im.client.PushMessageReceiver.onBindListener;
import com.zhy.baiduyun.im.dao.UserDB;
import com.zhy.baiduyun.im.utils.LoadingDialog;
import com.zhy.baiduyun.im.utils.NetUtil;
import com.zhy.baiduyun.im.utils.SendMsgAsyncTask;
import com.zhy.baiduyun.im.utils.SendMsgAsyncTask.OnSendScuessListener;
import com.zhy.baiduyun.im.utils.SharePreferenceUtil;
import com.zhy.baiduyun.im.utils.T;

public class LoginActivity extends FragmentActivity implements onBindListener
{
	private EditText mUsername;
	private EditText mPassword;

	private LoadingDialog mLoadingDialog;

	private SharePreferenceUtil mSpUtil;
	private UserDB mUserDB;
	private SendMsgAsyncTask task;
	private Gson mGson;

	private Handler mHandler = new Handler();
	private Runnable mConnTimeoutCallback = new Runnable()
	{
		@Override
		public void run()
		{
			if (mLoadingDialog != null)
			{
				mLoadingDialog.dismiss();
			}
			if (task != null)
			{
				task.stop();
			}
			T.showShort(LoginActivity.this, "��¼��ʱ��������");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mSpUtil = PushApplication.getInstance().getSpUtil();
		mUserDB = PushApplication.getInstance().getUserDB();
		mGson = PushApplication.getInstance().getGson();

		mLoadingDialog = new LoadingDialog();

		PushMessageReceiver.bindListeners.add(this);

		mUsername = (EditText) findViewById(R.id.username);
		mPassword = (EditText) findViewById(R.id.password);
	}

	public void login(View view)
	{
		if (!NetUtil.isNetConnected(this))
		{
			T.showLong(this, R.string.net_error_tip);
			return;
		}
		String nickname = mUsername.getText().toString();
		if (TextUtils.isEmpty(nickname))
		{
			T.showLong(this, "�ǳƲ���Ϊ��");
			return;
		}

		// ����һ��10���ʱ��Callback
		mHandler.postDelayed(mConnTimeoutCallback, 15000);
		mLoadingDialog.show(getSupportFragmentManager(), "LOADING_DIALOG");
		mLoadingDialog.setCancelable(false);
		mSpUtil.setNick(nickname);
		mSpUtil.setHeadIcon(R.drawable.h17);
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, PushApplication.API_KEY);// ��baidu�ʺŵ�¼,��apiKey�����ȡһ��id

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (task != null)
			task.stop();
		PushMessageReceiver.bindListeners.remove(this);// ע�����͵���Ϣ
	}

	@Override
	public void onBind(String userId, int errorCode)
	{
		Log.e("TAG", "Login Activity onBind ");
		if (errorCode == 0)
		{
			Log.e("TAG", "Login Activity onBind success ");
			// ������˺ųɹ������ڵ�һ�����У���ͬһtag��������һ��������Ϣ
			User u = new User(mSpUtil.getUserId(), mSpUtil.getChannelId(),
					mSpUtil.getNick(), mSpUtil.getHeadIcon(), 0);
			mUserDB.addUser(u);// ���Լ���ӵ����ݿ�
			Message firstSendMsg = new Message(System.currentTimeMillis(), "");
			firstSendMsg.setHello("hello");
			task = new SendMsgAsyncTask(mGson.toJson(firstSendMsg), "");
			task.setOnSendScuessListener(new OnSendScuessListener()
			{
				@Override
				public void sendScuess()
				{
					if (mLoadingDialog != null && mLoadingDialog.isVisible())
						mLoadingDialog.dismiss();
					mHandler.removeCallbacks(mConnTimeoutCallback);
					finish();
					Intent intent = new Intent(LoginActivity.this,
							MainActivity.class);
					startActivity(intent);
				}
			});
			task.send();
		}
	}

}
