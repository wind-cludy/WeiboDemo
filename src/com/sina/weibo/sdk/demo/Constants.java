
package com.sina.weibo.sdk.demo;

/**
 * weiboが必要なパラメータの定義
 */
public interface Constants {

    /** APP_KEY */
    public static final String APP_KEY      = "2405159747";
    
    /** 
     * callback URL
     */
    public static final String REDIRECT_URL = "http://www.wind123.tk/tmp/weibo/callback.php";

    /**
     * Scope 
     * weiboの使いたいScopeを指定する
     */
    public static final String SCOPE = 
            "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
}
