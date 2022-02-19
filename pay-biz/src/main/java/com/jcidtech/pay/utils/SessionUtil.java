package com.jcidtech.pay.utils;

import com.jcidtech.pay.model.UserInfo;

public class SessionUtil {
    private static final ThreadLocal<UserInfo> USER_INFO = new ThreadLocal<>();
    public static void addUserInfo(UserInfo userInfo){
        USER_INFO.set(userInfo);
    }
    public static UserInfo getUserInfo(){
        return USER_INFO.get();
    }
    public static void removeUserInfo(){
        USER_INFO.remove();
    }
}
