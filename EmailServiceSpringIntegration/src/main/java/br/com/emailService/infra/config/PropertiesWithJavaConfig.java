package br.com.emailService.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySources({
		@PropertySource("classpath:Mail.properties")
})
public class PropertiesWithJavaConfig {
 
   @Bean
   public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
	   PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
	   p.setIgnoreUnresolvablePlaceholders(true);
	   return p;
   }     
   
}
