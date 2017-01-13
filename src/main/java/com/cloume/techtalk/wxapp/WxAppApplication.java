package com.cloume.techtalk.wxapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoCustomConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoDefaultConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerTokenServicesConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 这是UI服务器
 * @author Gang
 *
 */
@SpringBootApplication

//@Import(MyOAuth2ClientConfiguration.class)
//@EnableOAuth2Client
@EnableConfigurationProperties(OAuth2SsoProperties.class)
@Import({ 
	MyOAuth2ClientConfiguration.class, 
	OAuth2SsoDefaultConfiguration.class, 
	OAuth2SsoCustomConfiguration.class,
	ResourceServerTokenServicesConfiguration.class })
//@EnableOAuth2Sso
public class WxAppApplication {

	public static void main(String[] args){
		SpringApplication.run(WxAppApplication.class, args);
	}
	
	UserInfoTokenServices a;
}
