package br.com.emailService.infra.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManipulator {
	
	
	public Properties getProp(String resourceString) throws IOException {
		Properties props = new Properties();
				
		ClassLoader classLoader = getClass().getClassLoader();
		
		InputStream in = classLoader.getResourceAsStream(resourceString + ".properties") ;
		props.load(in);
		return props;
	}
	
	public Properties getTemplateProperties(String resourceString) throws IOException {
		Properties props = new Properties();
				
		ClassLoader classLoader = getClass().getClassLoader();
		
		InputStream in = classLoader.getResourceAsStream("templates/" + resourceString + ".properties") ;
		props.load(in);
		return props;
	}

}