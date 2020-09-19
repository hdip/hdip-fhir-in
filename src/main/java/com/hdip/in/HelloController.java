package com.hdip.in;

import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.ArrayList;
import java.util.List;

@RestController
public class HelloController implements ApplicationContextAware {

	static ApplicationContext context;
	private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

	@RequestMapping("/test-page")
	@ResponseBody
	public List<String> index(){
	List<String> beans = new ArrayList<String>();
		for(String bean: context.getBeanDefinitionNames() ){
			beans.add(bean);
		}
		return beans;
}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}
}
