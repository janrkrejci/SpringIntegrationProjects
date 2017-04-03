package br.com.emailService.infra.error;

import br.com.emailService.domain.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * This class is the core controller of all exceptions. All exceptions flow within it.  
 * 
 * @author Jan R. Krejci
 * @version 1.0
 * @since 03/2017
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	
	static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	
	@ExceptionHandler(Throwable.class)
	public ResponseEntity<Object> exceptionHandler(Exception ex, WebRequest request){		
		LOG.debug("Exception class : " + ex.getClass().getName());
		Error errorBody = new Error();
		errorBody.setDescription(ex.getMessage());
		return new ResponseEntity<Object>(errorBody,HttpStatus.BAD_REQUEST);
	}
	
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        Error errorBody = new Error();
        errorBody.setDescription(getFieldErrorDescription(ex));
        return new ResponseEntity<Object>(errorBody, headers, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
	  MissingServletRequestParameterException ex, HttpHeaders headers, 
	  HttpStatus status, WebRequest request) {
        Error errorBody = new Error();	    
        errorBody.setDescription(ex.getParameterName() + " parameter is missing");
        return new ResponseEntity<Object>(errorBody, headers, status);
	}
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		Error errorBody = new Error();
		errorBody.setDescription("Http body not readable. Format invalid!");
		return new ResponseEntity<Object>(errorBody, headers, HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	@Override
	protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request){
		Error errorBody = new Error();
		errorBody.setDescription(ex.getMessage());
		return new ResponseEntity<Object>(errorBody, headers, status);
	}
	
	
    
    private String getFieldErrorDescription(MethodArgumentNotValidException ex){
    	FieldError fieldError = ex.getBindingResult().getFieldError();
    	return fieldError.getField()+ " " + fieldError.getCode();
    }
	
	
}
