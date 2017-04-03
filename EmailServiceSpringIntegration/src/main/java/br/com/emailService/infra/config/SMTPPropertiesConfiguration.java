package br.com.emailService.infra.config;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
public class SMTPPropertiesConfiguration {
	
	
    @Configuration
    @Profile("default")
    @PropertySource("classpath:SMTPTest.properties")
    static class Defaults
    { }
	
	private static String EMAIL_SERVER_HOST;
	private static String SECURE_PROTOCOL = "smtps";
	private static String NON_SECURE_PROTOCOL = "smtp";
	private static boolean OPTIONAL_SECURE_PROTOCOL;
	private static String SSL_OPTIONAL;
	private static Integer SERVER_PORT;
	private static String USERNAME;
	private static String PASSWORD;
	private static String TIMEOUT;
	private static String CONNECTION_TIMEOUT;
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer smtpPropertiesDefault() {
		PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();	
		p.setIgnoreUnresolvablePlaceholders(true);
		return p;
	}
	
	@Value("${mail.send.server}")
	private void setEmailServerHost(String server){
		EMAIL_SERVER_HOST = server;
	}
	
	@Value("${mail.send.secureMode}")
	private void setOptionalSecureProtocol(String secureMode){
		OPTIONAL_SECURE_PROTOCOL = BooleanUtils.toBoolean(secureMode);		 
	}
	
	@Value("${mail.send.enableSSL}")
	private void setOptionalSSL(String sslOptional){
		SSL_OPTIONAL = sslOptional;
	}
	
	@Value("${mail.send.server.port}")
	private void setServerPort(Integer port){
		SERVER_PORT = port;
	}
	
	@Value("${mail.send.server.username}")
	private void setUsername(String username){
		USERNAME = username;
	}
	
	@Value("${mail.send.server.password}")
	private void setPassword(String password){
		PASSWORD = password;
	}
	
	@Value("${mail.send.timeout}")
	private void setSmtpTimeout(String timeout){
		TIMEOUT = timeout;
	}
	
	@Value("${mail.send.connection.timeout}")
	private void setConnectionTimeout(String connectionTimeout){
		CONNECTION_TIMEOUT = connectionTimeout;
	}
	
	
		
	public static String emailServerHost(){
		return EMAIL_SERVER_HOST;
	}
		
	public static String emailProtocol(){
		 		  
		if (OPTIONAL_SECURE_PROTOCOL)
			return SECURE_PROTOCOL;
		return NON_SECURE_PROTOCOL;
	}
	
	public static String enableSSL(){
		return SSL_OPTIONAL;
	}
	
	public static Integer serverPort(){
		return SERVER_PORT;
	}
	
	public static String username(){
		return USERNAME;
	}
	
	public static String password(){
		return PASSWORD;
	}
	
	public static String timeout(){
		return TIMEOUT;
	}
	
	public static String connectionTimeout(){
		return CONNECTION_TIMEOUT;
	}
	
	
	
}
