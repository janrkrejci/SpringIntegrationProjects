package br.com.emailService.application.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import br.com.emailService.domain.DescriptionEmail;

public class EmailValidation implements Validator {
	static final Logger LOG = LoggerFactory.getLogger(EmailValidation.class);
	
	@Override
	public boolean supports(Class<?> clazz) {
		return DescriptionEmail.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {		
		LOG.info("Validating email structure");
		LOG.debug("Parameters :");
		LOG.debug("  target :" + target.toString());
		LOG.debug("  errors :" + errors.toString());
		ValidationUtils.rejectIfEmpty(errors, "title", "must be not null");
		ValidationUtils.rejectIfEmpty(errors, "description", "must be not null");
		LOG.debug("  errors after:" + errors.toString());
	}
	
}
