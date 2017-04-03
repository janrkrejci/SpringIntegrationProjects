package br.com.emailService.application.validation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.emailService.application.exception.HeaderException;

public class HeaderValidation {
	static final Logger LOG = LoggerFactory.getLogger(HeaderValidation.class);
	
	public static void validate(String client) throws HeaderException{		
		LOG.debug("Header Valitation");
	    if (StringUtils.isBlank(client)) {
	    	throw new HeaderException("Http header 'client' must be not null");
	    }
	}
}
