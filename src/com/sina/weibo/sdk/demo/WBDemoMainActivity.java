package com.sina.weibo.sdk.demo;

import java.text.SimpleDateFormat;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.demo.openapi.WBUserAPIActivity;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.LogUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * DEMOの入り口
 * 
 * @author qiangxzh
 * @since 2015-11-20
 */
public class WBDemoMainActivity extends Activity {

    private TextView mTokenText;
    private AuthInfo mAuthInfo;
    private Oauth2AccessToken mAccessToken;

    private SsoHandler mSsoHandler;
    /**
     * @see {@link Activity#onCreate}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtil.sIsLogEnable = true;
        
        // Token表示用の View，并让提示 View 的内容可滚动（小屏幕可能显示不全）
        mTokenText = (TextView) findViewById(R.id.token_text_view);
        
        // SDKのSSO認証を呼ぶ
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(WBDemoMainActivity.this, mAuthInfo);
        
    
        //SSO認証ボタン、WEIBOアプリがインストールの場合、WEIBOアプリが起動、
    	//インストールされない場合、WEBで認証
        findViewById(R.id.obtain_token_via_signature).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler.authorize(new AuthListener());
            }
        });
        
        // login and logout
        this.findViewById(R.id.feature_login_logout).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(WBDemoMainActivity.this, WBLoginLogoutActivity.class));
            }
        });
        // login and logout
        this.findViewById(R.id.feature_open_api).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(WBDemoMainActivity.this, WBUserAPIActivity.class));
            }
        });
        
        // ログアウト
//        findViewById(R.id.logout).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AccessTokenKeeper.clear(getApplicationContext());
//                mAccessToken = new Oauth2AccessToken();
//                updateTokenView(false);
//            }
//        });
        
        // URLでTokenを取得
//        findViewById(R.id.obtain_token_via_code).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(WBDemoMainActivity.this, WBAuthCodeActivity.class));
//            }
//        });

        // SharedPreferencesからAccessTokenを取得
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        if (mAccessToken.isSessionValid()) {
            updateTokenView(true);
        }
        
    }
    /**
     * 当 SSO 授权 Activity 退出时，该函数被调用。
     * 
     * @see {@link Activity#onActivityResult}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // SSO登録のコールバック処理を呼び出す。
    	//実際の処理はAuthListenerで実装されます。
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
        
    }

    /**
     * Weibo認証のコールバック処理
     * 1. SSO認証の場合、該当処理は{@link SsoHandler#authorizeCallBack} から呼ばれます。
     * 2. SSO認証ではない場合、認証が完了次第該当処理が呼ばれます
     */
    class AuthListener implements WeiboAuthListener {
        
        @Override
        public void onComplete(Bundle values) {
            // Tokenの取得
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            //ユーザーの携帯番号の取得 
            String  phoneNum =  mAccessToken.getPhoneNum();
            if (mAccessToken.isSessionValid()) {
                // Token出力
                updateTokenView(false);
                
                // Token情報をSharedPreferencesに格納
                AccessTokenKeeper.writeAccessToken(WBDemoMainActivity.this, mAccessToken);
                Toast.makeText(WBDemoMainActivity.this, 
                        R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
            } else {
                // エラーがあった場合、エラーコードとエラーメッセージを出力
                String code = values.getString("code");
                String message = getString(R.string.weibosdk_demo_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(WBDemoMainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(WBDemoMainActivity.this, 
                   R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(WBDemoMainActivity.this, 
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Token情報の出力
     * 
     * @param hasExisted 
     */
    private void updateTokenView(boolean hasExisted) {
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                new java.util.Date(mAccessToken.getExpiresTime()));
        String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
        mTokenText.setText(String.format(format, mAccessToken.getToken(), date));
        
        String message = String.format(format, mAccessToken.getToken(), date);
        if (hasExisted) {
            message = getString(R.string.weibosdk_demo_token_has_existed) + "\n" + message;
        }
        mTokenText.setText(message);
    }
}
