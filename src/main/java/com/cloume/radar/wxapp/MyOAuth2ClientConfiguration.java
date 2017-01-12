package com.cloume.radar.wxapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.config.annotation.web.configuration.OAuth2ClientConfiguration;

/**
 * 跳转到微信认证时需要appid参数用于携带client_id
 * @author Gang
 *
 */
@Configuration
public class MyOAuth2ClientConfiguration extends OAuth2ClientConfiguration {
	
	static public class MyOAuth2ClientContextFilter extends OAuth2ClientContextFilter {
		@Override
		protected void redirectUser(UserRedirectRequiredException e, HttpServletRequest request,
				HttpServletResponse response) throws IOException {
			String clientId = e.getRequestParams().get("client_id");
			e.getRequestParams().put("appid", clientId);
			
			super.redirectUser(e, request, response);
		}
	}

	@Override
	public OAuth2ClientContextFilter oauth2ClientContextFilter() {
		return new MyOAuth2ClientContextFilter();
	}
}
