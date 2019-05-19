package cn.itcast.core.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;

/*
1.手动实现 UserDetailsService接口
2.CAS验证成功之后,返回的User登录名就在重写方法的参数中

 */
public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        如果CAS认证成功,则会跳转到这个方法,否则不会?
        SimpleGrantedAuthority role_user = new SimpleGrantedAuthority("ROLE_USER");
        HashSet<GrantedAuthority> grantedAuthorityHashSet = new HashSet<>();
        grantedAuthorityHashSet.add(role_user);
//                           用户名      密码       权限
        User user = new User(username,"",grantedAuthorityHashSet);
        return user;
    }
}
