

package com.sina.weibo.sdk.demo;

import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.LogoutAPI;
import com.sina.weibo.sdk.widget.LoginButton;
import com.sina.weibo.sdk.widget.LoginoutButton;

/**
 * SDKが提供したボタンを経由してログイン
 */
public class WBLoginLogoutActivity extends Activity {
    
    /** UI */
    private TextView mTokenView;
    private LoginButton mLoginBtnDefault;
  
    /** ログインlistener */
    private AuthListener mLoginListener = new AuthListener();
    /**ログアウトlistener */
    private LogOutRequestListener mLogoutListener = new LogOutRequestListener();

    private Button mCurrentClickedButton;
    
    private AuthInfo mAuthInfo;
    
    /**
     * @see {@link Activity#onCreate}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_logout);
        mTokenView = (TextView) findViewById(R.id.result);

        // 認証の設定
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);

        // ログイン
        mLoginBtnDefault = (LoginButton) findViewById(R.id.login_button_default);
        mLoginBtnDefault.setWeiboAuthInfo(mAuthInfo, mLoginListener);
        //mLoginBtnStyle2.setStyle(LoginButton.LOGIN_INCON_STYLE_1);

      
        
        /**
         * ログアウト
         */
        final Button logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new LogoutAPI(WBLoginLogoutActivity.this, Constants.APP_KEY, 
                        AccessTokenKeeper.readAccessToken(WBLoginLogoutActivity.this)).logout(mLogoutListener);
            }
        });
        
        mLoginBtnDefault.setExternalOnClickListener(mButtonClickListener);
    
    }
    /**
     * 
     * @see {@link Activity#onActivityResult}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (mCurrentClickedButton != null) {
            if (mCurrentClickedButton instanceof LoginButton) {
                ((LoginButton)mCurrentClickedButton).onActivityResult(requestCode, resultCode, data);
            } else if (mCurrentClickedButton instanceof LoginoutButton) {
                ((LoginoutButton)mCurrentClickedButton).onActivityResult(requestCode, resultCode, data);
            }
        }
        
      
    }
    

    private OnClickListener mButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof Button) {
                mCurrentClickedButton = (Button)v;
            }
        }
    };

   
    private class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {
                String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                        new java.util.Date(accessToken.getExpiresTime()));
                String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
                mTokenView.setText(String.format(format, accessToken.getToken(), date));

                AccessTokenKeeper.writeAccessToken(getApplicationContext(), accessToken);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(WBLoginLogoutActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(WBLoginLogoutActivity.this, 
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_SHORT).show();
        }
    }


    private class LogOutRequestListener implements RequestListener {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String value = obj.getString("result");

                    if ("true".equalsIgnoreCase(value)) {
                        AccessTokenKeeper.clear(WBLoginLogoutActivity.this);
                        mTokenView.setText(R.string.weibosdk_demo_logout_success);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }     

        @Override
        public void onWeiboException(WeiboException e) {
            mTokenView.setText(R.string.weibosdk_demo_logout_failed);
        }
    }
}
