package cn.edu.ncut.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.edu.ncut.annotation.Controller;
import cn.edu.ncut.annotation.Qualifier;
import cn.edu.ncut.annotation.RequestMapping;
import cn.edu.ncut.annotation.Service;

public class DispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 7036516926254334347L;

	List<String> packageNames = new ArrayList<String>();

	Map<String, Object> map = new HashMap<>();

	Map<String, Object> handlerMap = new HashMap<String, Object>();// 建立url与method的关系

	public DispatcherServlet() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		// 服务器启动时要将带有注解的类实例化

		// 包扫描
		scanPackage("cn.edu.ncut");
		// 把所有类实例new出来，把类中的依赖注入
		try {
			filterAndNewInstance(); // 过滤没有注解的类
		} catch (Exception e) {
			e.printStackTrace();
		}

		// handlermap 发生在注入之前

		handlerMap();

		// 将有依赖关系的实例按依赖关系注入
		try {
			ioc();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("================class========================");
		for (String str : packageNames) {
			System.out.println(str);
		}
		System.out.println("================class========================");

		System.out.println("================map========================");
		for (Map.Entry<String, Object> m : map.entrySet()) {
			System.out.println(m.getKey() + " :" + m.getValue());
		}
		System.out.println("================map========================");

		System.out.println("================handlermap========================");
		for (Map.Entry<String, Object> m : handlerMap.entrySet()) {
			System.out.println(m.getKey() + " :" + m.getValue());
		}
		System.out.println("================handlermap========================");

	}

	private void handlerMap() {
		if (map.size() <= 0)
			return;
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue().getClass().isAnnotationPresent(Controller.class)) {
				// 获取Controller中的
				String controllerValue = entry.getValue().getClass().getAnnotation(Controller.class).value();

				Method[] methods = entry.getValue().getClass().getMethods();
				for (Method method : methods) {
					if (method.isAnnotationPresent(RequestMapping.class)) {
						String methodValue = method.getAnnotation(RequestMapping.class).value();
						handlerMap.put("/" + controllerValue + "/" + methodValue, method);// key就是uri的拼接，value就是method对象
					} else {
						continue;
					}
				}// end for
			} else {
				continue;
			}
		}// end for
	}

	private void ioc() throws Exception {
		if (map.size() <= 0)
			return;
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			// 得到每个类的属性
			Field[] fields = entry.getValue().getClass().getFields();
			for (Field field : fields) {
				field.setAccessible(true);
				// 判断属性上是否有注解
				if (field.isAnnotationPresent(Qualifier.class)) {
					String value = field.getAnnotation(Qualifier.class).value();
					// 为了获取实例
					Object object = map.get(value);

					field.setAccessible(true);
					field.set(entry.getValue(), object); // 注入
				}
			}
		}
	}

	private void filterAndNewInstance() throws Exception {
		if (packageNames.size() <= 0)
			return;
		for (String s : packageNames) {
			String className = s.replaceAll(".class", "");
			Class clazz = (Class) Class.forName(className);

			// 判断是否含有注解
			if (clazz.isAnnotationPresent(Controller.class)) {
				Object newInstance = clazz.newInstance();
				// map中的key就是注解上的name,先得到controller上的注解
				Controller controller = (Controller) clazz.getAnnotation(Controller.class);

				map.put(controller.value(), newInstance);
			} else if (clazz.isAnnotationPresent(Service.class)) {
				Object newInstance = clazz.newInstance();
				// map中的key就是注解上的name,先得到controller上的注解
				Service service = (Service) clazz.getAnnotation(Service.class);

				map.put(service.value(), newInstance);
			} else {
				continue;// 继续循环过滤其他类
			}

		}

	}

	private void scanPackage(String path) {
		// URL url = this.getClass().getClassLoader().getResource("/"
		// +replace(path));
		URL url = this.getClass().getClassLoader().getResource(replace(path));

		String pathFile = url.getFile();
		File file = new File(pathFile);
		String[] list = file.list();

		for (String string : list) {
			File eachFile = new File(pathFile + "/" + string);
			// 有可能是文件，有可能是文件夹
			if (eachFile.isDirectory()) {
				// 迭代
				scanPackage(path + "." + eachFile.getName());
			} else {
				packageNames.add(path + "." + eachFile.getName());
				// TODO
				System.out.println(path + "." + eachFile.getName());
			}
		}

	}

	private String replace(String path) {
		return path.replaceAll("\\.", "/");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uri = req.getRequestURI();
		String context = req.getContextPath();
		// 将项目名称去掉得到mapping
		String mapping = uri.replaceAll(context, "");
		Method method = (Method) handlerMap.get(mapping);
		// 获取Controller实例
		Object object = map.get(mapping.split("/")[0]);

		try {
			method.invoke(object, new Object[] { req, resp, null });
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}
}
