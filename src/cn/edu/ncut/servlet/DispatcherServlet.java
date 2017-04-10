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

	Map<String, Object> handlerMap = new HashMap<String, Object>();// ����url��method�Ĺ�ϵ

	public DispatcherServlet() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		// ����������ʱҪ������ע�����ʵ����

		// ��ɨ��
		scanPackage("cn.edu.ncut");
		// ��������ʵ��new�����������е�����ע��
		try {
			filterAndNewInstance(); // ����û��ע�����
		} catch (Exception e) {
			e.printStackTrace();
		}

		// handlermap ������ע��֮ǰ

		handlerMap();

		// ����������ϵ��ʵ����������ϵע��
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
				// ��ȡController�е�
				String controllerValue = entry.getValue().getClass().getAnnotation(Controller.class).value();

				Method[] methods = entry.getValue().getClass().getMethods();
				for (Method method : methods) {
					if (method.isAnnotationPresent(RequestMapping.class)) {
						String methodValue = method.getAnnotation(RequestMapping.class).value();
						handlerMap.put("/" + controllerValue + "/" + methodValue, method);// key����uri��ƴ�ӣ�value����method����
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
			// �õ�ÿ���������
			Field[] fields = entry.getValue().getClass().getFields();
			for (Field field : fields) {
				field.setAccessible(true);
				// �ж��������Ƿ���ע��
				if (field.isAnnotationPresent(Qualifier.class)) {
					String value = field.getAnnotation(Qualifier.class).value();
					// Ϊ�˻�ȡʵ��
					Object object = map.get(value);

					field.setAccessible(true);
					field.set(entry.getValue(), object); // ע��
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

			// �ж��Ƿ���ע��
			if (clazz.isAnnotationPresent(Controller.class)) {
				Object newInstance = clazz.newInstance();
				// map�е�key����ע���ϵ�name,�ȵõ�controller�ϵ�ע��
				Controller controller = (Controller) clazz.getAnnotation(Controller.class);

				map.put(controller.value(), newInstance);
			} else if (clazz.isAnnotationPresent(Service.class)) {
				Object newInstance = clazz.newInstance();
				// map�е�key����ע���ϵ�name,�ȵõ�controller�ϵ�ע��
				Service service = (Service) clazz.getAnnotation(Service.class);

				map.put(service.value(), newInstance);
			} else {
				continue;// ����ѭ������������
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
			// �п������ļ����п������ļ���
			if (eachFile.isDirectory()) {
				// ����
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
		// ����Ŀ����ȥ���õ�mapping
		String mapping = uri.replaceAll(context, "");
		Method method = (Method) handlerMap.get(mapping);
		// ��ȡControllerʵ��
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
