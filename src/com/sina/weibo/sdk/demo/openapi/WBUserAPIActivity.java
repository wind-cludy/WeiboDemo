
package com.sina.weibo.sdk.demo.openapi;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.demo.AccessTokenKeeper;
import com.sina.weibo.sdk.demo.Constants;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;

/**
 * ユーザー情報の取得
 * 
 */
public class WBUserAPIActivity extends Activity implements OnItemClickListener {
    private static final String TAG = WBUserAPIActivity.class.getName();
    
    /** UI ListView */
    private ListView mFuncListView;
   
    private String[] mFuncList;
    /**  Token  */
    private Oauth2AccessToken mAccessToken;
    /** ユーザーインターフェース */
    private UsersAPI mUsersAPI;
    
    /**
     * @see {@link Activity#onCreate}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_api_base_layout);
        
        // リストの取得
        mFuncList = getResources().getStringArray(R.array.user_func_list);
        // 初期化 ListView
        mFuncListView = (ListView)findViewById(R.id.api_func_list);
        mFuncListView.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, mFuncList));
        mFuncListView.setOnItemClickListener(this);
        
        //  Tokenを取得
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        // ユーザー情報の取得情報の取得
        mUsersAPI = new UsersAPI(this, Constants.APP_KEY, mAccessToken);
    }
    
    /**
     * @see {@link AdapterView.OnItemClickListener#onItemClick}
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view instanceof TextView) {
            if (mAccessToken != null && mAccessToken.isSessionValid()) {
                switch (position) {
                case 0:
                    //String uid = mAccessToken.getUid();
                    long uid = Long.parseLong(mAccessToken.getUid());
                    mUsersAPI.show(uid, mListener);
                    break;
                    
                case 1:
                    long[] uids = { Long.parseLong(mAccessToken.getUid()) };
                    mUsersAPI.counts(uids, mListener);
                    break;

                default:
                    break;
                }
            } else {
                Toast.makeText(WBUserAPIActivity.this, 
                        R.string.weibosdk_demo_access_token_is_empty, 
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    /**
     *  callback。
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                //User#parseでJSON stringをユーザーオブジェクトに変更
                User user = User.parse(response);
                if (user != null) {
                    Toast.makeText(WBUserAPIActivity.this, 
                            "User情報の取得が成功，ニックネーム：" + user.screen_name, 
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(WBUserAPIActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(WBUserAPIActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };
}
