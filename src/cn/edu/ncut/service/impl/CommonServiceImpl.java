package cn.edu.ncut.service.impl;

import java.util.Map;

import cn.edu.ncut.annotation.Service;
import cn.edu.ncut.service.CommonService;

@Service("commonServiceImpl")
public class CommonServiceImpl implements CommonService {

	public int insert(Map map) {
		System.out.println("commonServiceImpl" + "insert");
		return 0;
	}

	public int delete(Map map) {
		return 0;
	}

	public int update(Map map) {
		return 0;
	}

	public int select(Map map) {
		return 0;
	}

}
