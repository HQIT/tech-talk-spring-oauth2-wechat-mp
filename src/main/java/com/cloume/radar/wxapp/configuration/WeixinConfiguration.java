package com.cloume.radar.wxapp.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpServiceImpl;

@Configuration
public class WeixinConfiguration {
	
	@Value("${wx.appID}") private String wxAppID;
	@Value("${wx.appSecret}") private String wxAppSecret;
	@Value("${wx.token}") private String wxToken;
	@Value("${wx.aesKey}") private String wxAesKey;
	
	@Bean @Scope("singleton")
	public WxMpConfigStorage wxMpConfigStorage(){
		WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();
		
		config.setAppId(wxAppID);	// 设置微信公众号的appid
		config.setSecret(wxAppSecret);	// 设置微信公众号的app corpSecret
		config.setToken(wxToken);	// 设置微信公众号的token
		config.setAesKey(wxAesKey);	// 设置微信公众号的EncodingAESKey

		return config;
	}
	
	@Bean
	public WxMpService wxMpService() {
		WxMpService wxService = new WxMpServiceImpl();
		wxService.setWxMpConfigStorage(wxMpConfigStorage());
		
		return wxService;
	}
}
