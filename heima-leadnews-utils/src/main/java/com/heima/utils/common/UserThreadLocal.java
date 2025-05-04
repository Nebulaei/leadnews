package com.heima.utils.common;

import org.springframework.boot.autoconfigure.security.SecurityProperties;

public class UserThreadLocal {

    private final static ThreadLocal<Integer> USER_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUserId(Integer userId) {
        USER_THREAD_LOCAL.set(userId);
    }

    public static Integer getUserId() {
        return USER_THREAD_LOCAL.get();
    }

    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }
}
