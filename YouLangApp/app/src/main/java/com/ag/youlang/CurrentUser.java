package com.ag.youlang;

public class CurrentUser {
    private static CurrentUser ourInstance = new CurrentUser();

    public static CurrentUser getInstance() {
        return ourInstance;
    }

    private CurrentUser() {

    }

    public String Login;
}
