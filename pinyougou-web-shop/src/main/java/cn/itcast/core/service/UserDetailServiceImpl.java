package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;

/**
 * 主要功能:验证登录用户密码 是不是在数据库中存在,并且是不是正确的,可不可以登录,授予什么权利
 *
 *
 * SpringSecutity 使用这个bean 去Dubbo中调用Service实例 连接数据库,
 * 使用登录用户名 查询用户密码 并进行加密验证 返回是否成功
 * ::thisBean 的实例化
 * ::从Dubbo中引用Servive实例
 * ::将Service实例 注入到 thisBean实例中
 * ==>都在SpringSecurity.xml中进行的
 * 问题:为什么?
 * 答:因为SpringSecutity被加载到了Spring容器中了
 * 而这又是在Springmvc的主场:大部分都在Springmvc容器中,
 * Spring父容器不能使用子容器 Springmvc中的实例Bean
 * 所以 SpringSecurity 才在自己的xml中单独完成了上述工作.
 */
//需要实现UserDetailsService接口
public class UserDetailServiceImpl implements UserDetailsService{
    /**
     * 问题:为什么能使用到SellerService接口?
     * 答:因为pom中继承了
     * 问题:那为什么不能直接从dubbo中引用到?
     * 答:因为这个类没有被springmvc.xml扫描实例化,
     * 如果被springmvc.xml实例化到springmvc容器中,
     * 那么由于这个类最后是被spring容器中的springSecurity使用,
     * 而spring父容器使用不到springmvc子容器中的内容,
     * 所以这个类需要被springSecurity.xml中使用<beans:bean 实例化,
     * 而springSecurity引用这个类,需要这个类去连接数据库,
     * 所以这个类中需要定义:SellerService变量
     * 而使用SellerService实例调用SellerDao,
     * >由于这个类是在springSecurity中使用<beans:bean 实例化的
     * 所以,SellerService不能使用@Reference注入值
     * 就必须 在SpringSecurity.xml中 给这个类手动注入<beans:property
     * :而:SerllerService只是继承过来了,但是并没有实例化到Spring容器中,
     * springMVC中有一个实例,但是Spring容器中并没有.
     * 所以需要在springSecurity.xml中重新从Dubbo引用实例化到Spring容器中:
     * dubbo:reference id="sellerService"  interface="cn.itcast.core.service.SellerService"
     * 引入一个实例:以上.
     */
    private SellerService sellerService;//接口
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     *
     * @param username
     * @return UserDetails:是interface接口:其实现类是User spring框架的
     * User中有Set<GrantedAuthority>权限集合 set不可重复无序
     * GrantedAuthority 是一个interface接口 授权:
     * SimpleGrantedAuthority 实现了GrantedAuthority:
     * SimpleGrantedAuthority 中有方法:能接收String role 字符角色
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        1.调用Service连接数据库:查询用户对象
        Seller seller = sellerService.findOne(username);
//        2.之后的功能是什么?
/**
* 原来SpringSecurity.xml中的<user name="admin" password="123" authority="ROLE_ADMIN" 被代替了
 * 所以之后就是完成上述功能的 其他实现方式:
 * A.获取到      用户密码   给UserDetails实现类
 * B.更改/赋予   权限       给UserDetails实现类
 *
 * 加上完整的业务逻辑:
 * 1.根据用户名查询用户是否存在
 *   2.判断审核状态码
 *      3.如果都成功:则将A.B.赋值给UserDetails实现类,return
 * 否则return Null SpringSecurity框架会 自动识别为没有注册记录/没有输入正确 用户名或密码
 *
 * 最后的密码比对:在springSecurity.xml中配置了密码加密器
 * 所以:比对操作:在框架底层进行
 * 如果没有成功:SpringSecurity框架会自动进行拦截 ,让其重新登录等操作
*/
//    如果查询到用户名存在
        if (null!=seller){
//          则判断审核状态码 是否被审核通过
            if ("1".equals(seller.getStatus())){
//                如果用户存在,且被审核通过,则将密码/权限 给UserDetails实现类 return 该实现类
//                User user = new User();//User没有空参构造方法,不能先创建一个空属相对象,留着之后再进行赋值
//                所以必须使用带参构造方法 创建该对象
//                1.先处理 权限 因为权限在Set集合中,权限对象是 GrantedAuthority实现类SimpleGrantedAuthority对象
//              HashSet是无序的:这里的应用
//              记得将角色对象放入到set集合中
                SimpleGrantedAuthority role_seller = new SimpleGrantedAuthority("ROLE_SELLER");
                HashSet<GrantedAuthority> grantedAuthority = new HashSet<>();
                grantedAuthority.add(role_seller);
//                注意:这里返回了 username且使用的是入参,密码,手动赋权
                User user = new User(username, seller.getPassword(), grantedAuthority);
                return user;
            }
        }
        return null;
        /**
         * 最后:返回USER对象:用户名:用户密码:授予权限
         * 交由SpringSecurity来进行判断:
         * 1.如果user为空:
         *            为空->直接跳转登录失败页面重新登录
         *
         * 2.如果user不为空:
         *           验证加密密码 是否 相同:
         *                                  相同:验证成功->跳转成功登录首页
         *                                不相同:验证失败->跳转登录失败页面
         */
    }

    /**
     * 遇到的DIYBUG:什么都查完了,最后没有手动赋值权限 ROLE_SELLER
     * 错误效果:登录成功了,页面跳转了,但是显示 deny ,就是因为你没有权限访问 跳转的页面
     * ->最后发现没有权限->追踪到问题->没有将权限ROLE_SELLER解决
     */
}

