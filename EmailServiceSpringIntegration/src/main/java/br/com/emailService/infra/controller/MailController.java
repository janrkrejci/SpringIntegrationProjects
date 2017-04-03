package br.com.emailService.infra.controller;

import java.util.Map;
import java.util.Properties;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.emailService.application.template.EmailTemplate;
import br.com.emailService.application.validation.EmailValidation;
import br.com.emailService.application.validation.HeaderValidation;
import br.com.emailService.domain.DescriptionEmail;
import br.com.emailService.infra.service.MailSenderService;

/**
 * Class controller supports all requests and call the Mail gateway service.  
 * 
 * @author Jan R. Krejci
 * @version 1.0
 * @since 03/2017
 */

@RestController
public class MailController {
	
	private static final Logger LOG = LoggerFactory.getLogger(MailController.class);
		
	@Autowired
	private ConfigurableApplicationContext ctx;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(new EmailValidation());
	}
	
	
	/**
	 * Endpoint that send emails 
	 * 
	 * @param descEmail
	 * @param client
	 *
	 * @return String
	 */
	@RequestMapping(value = "${mail.send.uri}", 
			produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	public ResponseEntity<?> mail(
			@RequestBody @Valid DescriptionEmail descEmail,
			@RequestHeader(value="client",required=true) String clientRaw) throws Exception {
		
		HeaderValidation.validate(clientRaw);
		final String client = StringUtils.capitalize(clientRaw);		
		
		LOG.info("Receiving a new email");
		LOG.debug("Description : " + descEmail.toString());
		LOG.info("Sending the email to cliente : " + client);
		
		LOG.debug("Instantiating the client template" );		
		Properties propertiesTemplate = EmailTemplate.instantiateProperty(client);
		
		LOG.debug("Calling the method EmailHeaderTemplate.buildHeader" );		
		Map<String,String> header = EmailTemplate.buildHeader(propertiesTemplate, descEmail.getTitle());

		LOG.debug("Calling the method EmailBodyTemplate.buildBody" );
		String body = EmailTemplate.buildBody(propertiesTemplate, descEmail,client);
		
		LOG.debug("Injeting the Bean MailSenderService.MailService.class e calling the interface sendMail" );
		ctx.getBean(MailSenderService.MailService.class)
			.sendMail(body,header);

		return new ResponseEntity<String>(HttpStatus.OK);
		
	}
}
		
