package br.com.emailService.infra.service;

import static br.com.emailService.infra.config.SMTPPropertiesConfiguration.connectionTimeout;
import static br.com.emailService.infra.config.SMTPPropertiesConfiguration.emailProtocol;
import static br.com.emailService.infra.config.SMTPPropertiesConfiguration.emailServerHost;
import static br.com.emailService.infra.config.SMTPPropertiesConfiguration.enableSSL;
import static br.com.emailService.infra.config.SMTPPropertiesConfiguration.password;
import static br.com.emailService.infra.config.SMTPPropertiesConfiguration.serverPort;
import static br.com.emailService.infra.config.SMTPPropertiesConfiguration.timeout;
import static br.com.emailService.infra.config.SMTPPropertiesConfiguration.username;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.dsl.mail.Mail;
import org.springframework.integration.dsl.mail.MailSendingMessageHandlerSpec;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.integration.handler.advice.ErrorMessageSendingRecoverer;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.MailSendException;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;


/**
 * Email service Spring integration definition. It uses JavaMail.<br>
 * Gateway({@link #MailSenderService()}) -> Channel({@link #mailInput()})-> Pollable Channel({@link #poller()}}) -> Flow-Service activator({@link #smtp()}}) -> OutboundAdapter({@link #mailOutboundAdapter()}})
 * 										
 * @author Jan R. Krejci
 * @version 1.0
 * @since 03/2017
 */
@Configuration
@EnableAutoConfiguration
@IntegrationComponentScan
public class MailSenderService {
	
	/**
	 * Outbound email adpater conection properties 
	 */
		
	@Value("${mail.send.queue.rate}")
	public String QUEUE_RATE;
	@Value("${mail.send.queue.capacity}")
	public String QUEUE_CAPACITY;
	@Value("${mail.send.retry.maxAttemps}")
	public String RETRY_MAX_ATTEMPS;
	@Value("${mail.send.retry.backOffPeriod}")
	public String RETRY_BACKOFF_PERIOD;
	
	@Value("${mail.send.error.directory}")
	public String ERROR_DIRECTORY;
	

	/**
	 * ################# 
	 * MESSAGE ENDPOINTS
	 * #################
	 */
	
	
	/**
	 * Default queue configuration 
	 * @return PollerMetadata
	 */	
	@Bean(name = PollerMetadata.DEFAULT_POLLER)
	public PollerMetadata poller() {
	    return Pollers	    		
	    		.fixedRate(NumberUtils.createLong(QUEUE_RATE))
	    		.maxMessagesPerPoll(10)
	    		.get();
	}
	
	@Bean(name = "mail.input")
	public MessageChannel mailInput(){
		return new QueueChannel(NumberUtils.createInteger(QUEUE_CAPACITY));
		
	}
	
	/**
	 * RecoveryChannel configuration in case of error to send email.
	 * @return MessageChannel
	 */	
	@Bean
	public MessageChannel recoveryChannel() {
	    return MessageChannels.direct().get();
	}
	

	/**
	 * Interface Gateway para a interface de envio de emails	 * 
	 */
	@MessagingGateway 
	public static interface MailService {
		@Gateway(requestChannel = "mail.input")
		void sendMail(@Payload String body, @Headers Map<String,String> headers);		
	}
	
	/**
	 * Inject the bean RetryPolicy to define the policy in case of send email error.
	 * Define o número de tentativas e a quantidade de vezes que irá tentar. O padrão é 3.
	 * @return RetryPolicy
	 */
	@Bean
	public RetryPolicy retryPolicy() {
		final Map<Class<? extends Throwable>, Boolean> map = 
				new HashMap<Class<? extends Throwable>, Boolean>() {
					{
						put(MailSendException.class,true);
						put(RuntimeException.class, true);
					}
					private static final long serialVersionUID = -1L;
				};
		final RetryPolicy ret = new SimpleRetryPolicy(NumberUtils.createInteger(RETRY_MAX_ATTEMPS), map, true);
		return ret;
	}
	
	/**
	 * Inject the bean RetryTemplate 
	 * @return RetryTemplate
	 */
	@Bean
	public RetryTemplate retryTemplate() {
		final RetryTemplate ret = new RetryTemplate();
		ret.setRetryPolicy(retryPolicy());
		FixedBackOffPolicy backoffPolicy = new FixedBackOffPolicy();
		backoffPolicy.setBackOffPeriod(NumberUtils.createLong(RETRY_BACKOFF_PERIOD));
		ret.setBackOffPolicy(backoffPolicy);
		ret.setThrowLastExceptionOnExhausted(false);
		return ret;
	}
	
	/**
	 * Inject the Advice bean to configure the retryTemplate and the recoveryChannel 
	 * @return Advice
	 */
	@Bean
	public Advice retryAdvice() {
		final RequestHandlerRetryAdvice advice = new RequestHandlerRetryAdvice();		
		advice.setRetryTemplate(retryTemplate());
		RecoveryCallback<Object> recoveryCallBack = new ErrorMessageSendingRecoverer(recoveryChannel());
		
		advice.setRecoveryCallback(recoveryCallBack);
		return advice;
	}
	
	
	/**
	 * Configure the email outbound adapter. This adapater effectively send email to the remittee 
	 * @return MailSendingMessageHandlerSpec
	 */
	private MailSendingMessageHandlerSpec mailOutboundAdapter(){
		MailSendingMessageHandlerSpec msmhs = 
				Mail.outboundAdapter(emailServerHost())
				.port(serverPort())
				.credentials(username(), password())
				.protocol(emailProtocol())				
				.javaMailProperties(p -> p
						.put("mail.debug", "false")
						.put("mail.smtp.ssl.enable",enableSSL())
						.put("mail.smtp.connectiontimeout", connectionTimeout())
						.put("mail.smtp.timeout", timeout()));
		return msmhs;
	}
	
	/**
	 * Configure the file outbound to write a file with the header and payload in case of error to send
	 * the email after max attemps setted in retryPolicy
	 * @return FileWritingMessageHandler
	 */
	@Bean
	public FileWritingMessageHandler fileOutboundAdapter(){
		FileWritingMessageHandler fwmhs = Files
				.outboundAdapter(new File(ERROR_DIRECTORY))
				.autoCreateDirectory(true)
				.get();		
		
		return fwmhs;
	}
	
	/**
	 * ################ 
	 * FLOWS
	 * ################
	 */
	
	/**
	 * Fluxo de integracao para envio de e-mails
	 * Integration flow injection. Defines all payload flow and start the service activator.
	 * @return IntegrationFlow
	 */
	@Bean
	public IntegrationFlow smtp(){	
		return IntegrationFlows.from("mail.input")
				.handle(this.mailOutboundAdapter(), 
						e -> e.id("smtpOut")								
							.poller(Pollers.fixedRate(5000).maxMessagesPerPoll(10))
							.advice(retryAdvice())
						)				
				.get();
	}
	
	/**
	 * Error Integration flow injection. Defines the flow in case of error and redirect to RecoveryChannel
	 * @return IntegrationFlow
	 */
	@Bean 
	public IntegrationFlow errorFlow(){
		
		return IntegrationFlows.from(recoveryChannel())																
								.log(Level.ERROR,"emailService.infra.service.errorFlow", "'Send email error: '.concat(payload.message)")
								.transform("payload.failedMessage")								
								.handle((p,h) -> 
											MessageBuilder.withPayload(new GenericMessage<>(p,h)))
								.transform(Transformers.toJson())
								.enrichHeaders(c -> c.headerExpression(FileHeaders.FILENAME, "'emailErrors-'.concat(headers.getTimestamp()).concat('.json')"))
								.log(LoggingHandler.Level.INFO,
										"emailService.infra.service.errorFlow", 
										"'It couldn't send the email. The file '.concat(headers.file_name).concat(' has been written')")
								.handle(fileOutboundAdapter())								
								.get();
	}
}

