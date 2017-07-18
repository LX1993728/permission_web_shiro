package liuxun.ssm.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import liuxun.ssm.shiro.CustomRealm;
/**
 * 测试：手动调用controller清除shiro中的缓存
 * @author liuxun
 *
 */
@Controller
public class ClearShiroCache {

	//注入realm
	@Autowired
	private CustomRealm customRealm;
	
	@RequestMapping("/clearShiroCache")
	public String clearShiroCache(){
		//清除缓存，如果按照标准写法是在Service中调用customRealm.clearCached();
		customRealm.clearCached();
		return "success";
	}
}
