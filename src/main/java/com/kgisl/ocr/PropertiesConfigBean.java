package com.kgisl.ocr;

import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = "classpath:ocr.properties")
public class PropertiesConfigBean implements InitializingBean {

	@Autowired
	private Environment env;

	@Value("${key.folder}")
	private String folder;

	@Value("${key.folder.ocr}")
	private String folderOcr;

	public PropertiesConfigBean() {
		super();
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

}
