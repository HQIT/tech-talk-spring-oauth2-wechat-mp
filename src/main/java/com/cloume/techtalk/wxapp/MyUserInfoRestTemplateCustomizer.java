package com.cloume.techtalk.wxapp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateCustomizer;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultRequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.ResponseExtractor;

import com.cloume.common.utils.MapBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 用于通过customize方法修改OAuth2RestTemplate中的AuthorizationCodeAccessTokenProvider,
 * 给AuthorizationCodeAccessTokenProvider设置新的TokenRequestEnhancer,
 * TokenRequestEnhancer中可以修改获取AccessToken时的uri参数
 * @author Gang
 *
 */
@Configuration
@Component
public class MyUserInfoRestTemplateCustomizer implements UserInfoRestTemplateCustomizer {
	
	/**
	 * 需要吧appid和secret也放到请求参数中
	 * @author Gang
	 *
	 */
	static class MyWxAccessTokenRequestEnhancer extends DefaultRequestEnhancer {
		@Override
		public void enhance(AccessTokenRequest request, OAuth2ProtectedResourceDetails resource,
				MultiValueMap<String, String> form, HttpHeaders headers) {
			super.enhance(request, resource, form, headers);
			form.set("appid", resource.getClientId());
			form.set("secret", resource.getClientSecret());
		}
	}
	
	static class MyAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {
		/**
		 * 微信用GET方式, spring oauth2框架只在GET时将form中参数拼接到url中
		 */
		@Override
		protected HttpMethod getHttpMethod() {
			return HttpMethod.GET;
		}
		
		/**
		 * 微信的response body是json格式的
		 */
		@Override
		protected ResponseExtractor<OAuth2AccessToken> getResponseExtractor() {
			getRestTemplate(); // force initialization
			return new HttpMessageConverterExtractor<OAuth2AccessToken>(OAuth2AccessToken.class, 
					Arrays.asList(new WxOAuth2AccessTokenMessageConverter()));
		}
	}
	
	static class WxOAuth2AccessTokenMessageConverter extends AbstractHttpMessageConverter<OAuth2AccessToken> {

		private final MappingJackson2HttpMessageConverter delegateMessageConverter;
		
		/**
		 * 微信的access_token是application/json
		 */
		WxOAuth2AccessTokenMessageConverter() {
			super(MediaType.APPLICATION_JSON, 
					MediaType.APPLICATION_JSON_UTF8, 
					MediaType.TEXT_PLAIN);
			delegateMessageConverter = new MappingJackson2HttpMessageConverter();
		}
		
		/**
		 * 要按照json来解析结果
		 */
		@Override
		protected OAuth2AccessToken readInternal(Class<? extends OAuth2AccessToken> clazz, HttpInputMessage inputMessage)
				throws IOException, HttpMessageNotReadableException {
			Object accessToken = delegateMessageConverter.read(DefaultOAuth2AccessToken.class, inputMessage);
			return (OAuth2AccessToken) accessToken;
		}

		@Override
		protected boolean supports(Class<?> clazz) {
			return OAuth2AccessToken.class.equals(clazz);
		}

		@Override
		protected void writeInternal(OAuth2AccessToken accessToken, HttpOutputMessage outputMessage)
				throws IOException, HttpMessageNotWritableException {
			throw new UnsupportedOperationException(
					"This converter is only used for converting from externally aqcuired form data");
		}
	}
	
	/**
	 * 需要通过TokenRequestEnhancer设置appid
	 */
	@Override
	public void customize(OAuth2RestTemplate template) {
		AuthorizationCodeAccessTokenProvider accessTokenProvider = new MyAuthorizationCodeAccessTokenProvider();
		accessTokenProvider.setTokenRequestEnhancer(new MyWxAccessTokenRequestEnhancer());
		template.setAccessTokenProvider(accessTokenProvider);
	}
	
	@Configuration
	static protected class MyUserInfoTokenServicesConfiguration {
		
		final ResourceServerProperties sso;
		final UserInfoRestTemplateFactory restTemplateFactory;
		final ObjectProvider<AuthoritiesExtractor> authoritiesExtractor;
		final ObjectProvider<PrincipalExtractor> principalExtractor;
		
		MyUserInfoTokenServicesConfiguration(
				ResourceServerProperties sso,
				UserInfoRestTemplateFactory restTemplateFactory,
				ObjectProvider<AuthoritiesExtractor> authoritiesExtractor,
				ObjectProvider<PrincipalExtractor> principalExtractor
				) {
			this.sso = sso;
			this.restTemplateFactory = restTemplateFactory;
			this.authoritiesExtractor = authoritiesExtractor;
			this.principalExtractor = principalExtractor;
		}
		
		class MyUserInfoTokenServices extends UserInfoTokenServices {
			
			OAuth2RestOperations restTemplate;
			
			public MyUserInfoTokenServices(String userInfoEndpointUrl, String clientId) {
				super(userInfoEndpointUrl, clientId);
			}
			
			@Override
			public void setRestTemplate(OAuth2RestOperations restTemplate) {
				super.setRestTemplate(this.restTemplate = restTemplate);
			}
			
			OAuth2RestOperations getRestTemplate() {
				if(restTemplate == null) {
					BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
					resource.setClientId(sso.getClientId());
					restTemplate = new OAuth2RestTemplate(resource);
				}
				return restTemplate;
			}
			
			AuthoritiesExtractor authoritiesExtractor = new FixedAuthoritiesExtractor();
			@Override
			public void setAuthoritiesExtractor(AuthoritiesExtractor authoritiesExtractor) {
				super.setAuthoritiesExtractor(authoritiesExtractor);
				this.authoritiesExtractor = authoritiesExtractor;
			}
			
			@Override
			public OAuth2Authentication loadAuthentication(String accessToken)
					throws AuthenticationException, InvalidTokenException {
				Map<String, Object> info = getRestTemplate().getAccessToken().getAdditionalInformation();
				System.out.printf("has %s", info.containsKey("openid"));
				
				try {
					OAuth2AccessToken existingToken = getRestTemplate().getOAuth2ClientContext().getAccessToken();
					if (existingToken == null || !accessToken.equals(existingToken.getValue())) {
						DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(accessToken);
						token.setTokenType(sso.getTokenType());
						restTemplate.getOAuth2ClientContext().setAccessToken(token);
					}
					
					String responseBody = restTemplate.getForEntity(
							String.format("%s?access_token={access_token}&openid={openid}", sso.getUserInfoUri()),
							String.class,
							MapBuilder.begin("access_token", accessToken)
								.and("openid", info.get("openid").toString())
								.and("lang", "zh_CN")
								.build()
							).getBody();
					
					Map<?, ?> map = new ObjectMapper().readValue(responseBody, Map.class);
					
					Object principal = getPrincipal((Map<String, Object>) map);
					if(principal.equals("unknown")) {
						principal = map.get("openid");
					}
						
					List<GrantedAuthority> authorities = authoritiesExtractor.extractAuthorities((Map<String, Object>) map);
					OAuth2Request request = new OAuth2Request(null, sso.getClientId(), null, true, null, null, null, null, null);
					UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
					token.setDetails(map);
					
					OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(request, token);
					oAuth2Authentication.setDetails(map);
					
					return oAuth2Authentication;
				}
				catch (Exception ex) {
					this.logger.info("Could not fetch user details: " + ex.getClass() + ", " + ex.getMessage());
				}
				
				return null;
			}
		}
		
		@Bean
		public UserInfoTokenServices userInfoTokenServices() {
			MyUserInfoTokenServices services = new MyUserInfoTokenServices(sso.getUserInfoUri(), sso.getClientId());
			services.setTokenType(sso.getTokenType());
			services.setRestTemplate(restTemplateFactory.getUserInfoRestTemplate());
			if(authoritiesExtractor.getIfAvailable() != null) {
				services.setAuthoritiesExtractor(authoritiesExtractor.getIfAvailable());
			}
			if(principalExtractor.getIfAvailable() != null) {
				services.setPrincipalExtractor(principalExtractor.getIfAvailable());
			}
			
			return services;
		}
	}
}