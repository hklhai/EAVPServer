package cn.edu.ncut.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.edu.ncut.annotation.Controller;
import cn.edu.ncut.annotation.Qualifier;
import cn.edu.ncut.annotation.RequestMapping;
import cn.edu.ncut.service.CommonService;

@Controller("commonController")
public class CommonController {

	@Qualifier("commonServiceImpl")
	private CommonService commonService;

	@RequestMapping("insert")
	public String insert(HttpServletRequest req, HttpServletResponse resp, String param) {
		System.out.println(req.getRequestURI()+"insert");
		commonService.insert(null);
		return null;
	}

	@RequestMapping("delete")
	public String delete(HttpServletRequest req, HttpServletResponse resp, String param) {
		commonService.delete(null);

		return null;
	}

	@RequestMapping("update")
	public String update(HttpServletRequest req, HttpServletResponse resp, String param) {
		commonService.update(null);

		return null;
	}
	
	@RequestMapping("select")
	public String select(HttpServletRequest req, HttpServletResponse resp, String param) {
		commonService.select(null);

		return null;
	}


}
