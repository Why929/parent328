package cn.itcast.core.service;

import cn.itcast.core.pojo.user.User;

public interface UserService {
    public void sentCode(String string);

    void add(User user, String phoneNum);
}
