package br.com.emailService.application.template;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.emailService.domain.DescriptionEmail;
import br.com.emailService.infra.config.PropertiesManipulator;

public class EmailTemplate {
	private static final Logger LOG = LoggerFactory.getLogger(EmailTemplate.class.getName());
	
	/**
	 * 
	 * Contructs the header and initialize the parameters expected by service email
	 * @author Jan R. Krejci
	 * @version 1.0
	 * @since 12/2016
	 */
	public static Map<String,String> buildHeader(Properties propertiesTemplate, String title) throws IOException {
		LOG.info("Building the message header");
		Map<String,String> headers = new HashMap<String,String>();			
		
		headers.put("mail_subject", title );
		headers.put("mail_from", propertiesTemplate.getProperty("mail.from"));
		headers.put("mail_cc", propertiesTemplate.getProperty("mail.cc"));
		headers.put("mail_to", propertiesTemplate.getProperty("mail.to"));
		
		LOG.info("Header has builded");
		return headers;
		
	}
	
	/**
	 * Formats and constructs the message body 
	 * @author Jan R. Krejci
	 * @version 1.0
	 * @since 12/2016
	 */
	public static String buildBody(Properties propertiesTemplate, DescriptionEmail email, String client) throws IOException {
		LOG.info("Building the message body");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String body = propertiesTemplate.getProperty("mail.body");
		
		body = body.replaceAll("@Date",dateFormat.format(date));
		body = body.replaceAll("@Informer", client);
		body = body.replaceAll("@Description", email.getDescription());
		
		LOG.info("EmailBody : " + body);
		return body;
	}
	
	public static Properties instantiateProperty(String client) throws IOException {
		LOG.info("Loading client template :" + client.toUpperCase());
		PropertiesManipulator manipulator = new PropertiesManipulator();
		Properties properties = manipulator.getTemplateProperties("Mail"+client.toUpperCase()+"Template");
		
		return properties;
	}
}
