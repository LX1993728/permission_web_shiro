package liuxun.ssm.shiro;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import liuxun.ssm.po.ActiveUser;
import liuxun.ssm.po.SysPermission;
import liuxun.ssm.po.SysUser;
import liuxun.ssm.service.SysService;

public class CustomRealm extends AuthorizingRealm {
	@Autowired
	private SysService sysService;
	
	// 设置Realm名称
	@Override
	public void setName(String name) {
		super.setName("CustomRealm");
	}

	// 支持UsernamePasswordToken
	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof UsernamePasswordToken;
	}

	// 用于认证(使用静态数据模拟测试)
	/**@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		// 从token中获取用户身份信息
		String username = (String) token.getPrincipal();
		// 拿着username从数据库中进行查询
		// ....
		// 如果查询不到返回null
		if (!username.equals("zhangsan")) {
			return null;
		}

		// 获取从数据库查询出来的用户密码
		String password = "123"; // 这里使用静态数据进行测试

		// 根据用户id从数据库中取出菜单
		// ...先使用静态数据
		List<SysPermission> menus = new ArrayList<SysPermission>();
		SysPermission sysPermission_1 = new SysPermission();
		sysPermission_1.setName("商品管理");
		sysPermission_1.setUrl("/item/queryItem.action");
		SysPermission sysPermission_2 = new SysPermission();
		sysPermission_2.setName("用户管理");
		sysPermission_2.setUrl("/user/query.action");

		menus.add(sysPermission_1);
		menus.add(sysPermission_2);

		// 构建用户身份信息
		ActiveUser activeUser = new ActiveUser();
		activeUser.setUserid(username);
		activeUser.setUsername(username);
		activeUser.setUsercode(username);
		activeUser.setMenus(menus);

		// 返回认证信息由父类AuthenticationRealm进行认证
		SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(activeUser, password,
				this.getName());

		return simpleAuthenticationInfo;
	}

	// 用于授权(使用静态数据进行测试)
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		//获取身份信息
		ActiveUser activeUser = (ActiveUser) principals.getPrimaryPrincipal();
		//用户id
		String userid = activeUser.getUserid();
		// 根据用户id从数据库中查询权限数据
		// ...这里使用静态数据模拟
		List<String> permissions = new ArrayList<String>();
		permissions.add("item:query");
		permissions.add("item:update");
		
		//将权限信息封装为AuthorizationInfo
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
		//基于资源权限的访问控制
		for (String permission : permissions) {
			simpleAuthorizationInfo.addStringPermission(permission);
		}
		// 如果基于角色进行访问控制
		// for (String role : roles) {
		// simpleAuthorizationInfo.addRole(role);
		// }
		
		return simpleAuthorizationInfo;
	}
	**/
	// 用于认证(从数据库中查询用户信息)
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		// 从token中获取用户身份信息
		String userCode = (String) token.getPrincipal();
		
		SysUser sysUser = null;
		try {
			sysUser = sysService.findSysUserByUserCode(userCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//如果账号不存在则返回null
		if (sysUser == null) {
			return null;
		}
		
		//根据用户id取出菜单
		List<SysPermission> menus = null;
		try {
			menus = sysService.findMenuListByUserId(sysUser.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//用户密码
		String password = sysUser.getPassword();
		//盐
		String salt = sysUser.getSalt();
		
		//构建用户身份信息
		ActiveUser activeUser = new ActiveUser();
		activeUser.setUserid(sysUser.getId());
		activeUser.setUsername(sysUser.getUsername());
		activeUser.setUsercode(sysUser.getUsercode());
		activeUser.setMenus(menus);
		
		//将activeUser设置simpleAuthenticationInfo
		SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(activeUser, password,
				ByteSource.Util.bytes(salt), this.getName());
		return simpleAuthenticationInfo;
	}
	
	// 用于授权(从数据库中查询授权信息)
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		//获取身份信息
		ActiveUser activeUser = (ActiveUser) principals.getPrimaryPrincipal();
		//用户id
		String userid = activeUser.getUserid();
		//获取用户权限
		List<SysPermission> permissionsList = null;
		try {
			permissionsList = sysService.findPermissionListByUserId(userid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//构建shiro授权信息
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
		//单独定义一个集合
		List<String> permissions = new ArrayList<String>();
		for (SysPermission sysPermission : permissionsList) {
			//将数据库中的权限标签放入集合
			permissions.add(sysPermission.getPercode());
		}
		simpleAuthorizationInfo.addStringPermissions(permissions);
		
		return simpleAuthorizationInfo;
	}
	
	//清除用户的授权信息
	
	
	//清空缓
	public void clearCached(){
		//清空所有用户的身份缓存信息
		PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
		super.clearCache(principals);
	}

}
