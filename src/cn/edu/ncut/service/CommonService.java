package cn.edu.ncut.service;

import java.util.Map;


public interface CommonService {
	int insert(Map map);

	int delete(Map map);

	int update(Map map);

	int select(Map map);
}
