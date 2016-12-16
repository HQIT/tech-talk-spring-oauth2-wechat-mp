package com.cloume.radar.wxapp.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.cloume.radar.wxapp.wx.message.WxClickEventHandler;
import com.cloume.radar.wxapp.wx.message.WxEventHandler;
import com.cloume.radar.wxapp.wx.message.WxMpMessageHandlerImpl;
import com.cloume.radar.wxapp.wx.message.WxMpMessageInterceptorImpl;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpMessageInterceptor;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
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
	
	@Bean
	public WxMpMessageInterceptor wxMpMessageInterceptor(){
		return new WxMpMessageInterceptorImpl();
	}
	
	@Bean
	public WxMpMessageHandler wxMpMessageHandler(){
		return new WxMpMessageHandlerImpl();
	}
	
	public WxMpMessageHandler wxClickEventHandler(String key){
		return new WxClickEventHandler(key);
	}
	
	public WxMpMessageHandler wxEventHandler(String event){
		return new WxEventHandler(event);
	}
	
	@Bean @Scope("singleton")
	public WxMpMessageRouter wxMpMessageRouter(){
		WxMpMessageRouter router = new WxMpMessageRouter(wxMpService());
		
		router
			.rule()
			.msgType(WxConsts.XML_MSG_EVENT)
			.event(WxConsts.EVT_CLICK)
			.eventKey("V1003")		///听课提问
			.handler(wxClickEventHandler("V1003"))
			.next()
			
			.rule()
			.msgType(WxConsts.XML_MSG_EVENT)
			.event(WxConsts.EVT_SUBSCRIBE)
			.interceptor(wxMpMessageInterceptor())
			.handler(wxEventHandler(WxConsts.EVT_SUBSCRIBE))
			.next()
			
			.rule()
			.msgType(WxConsts.XML_MSG_EVENT)
			.event(WxConsts.EVT_UNSUBSCRIBE)
			.interceptor(wxMpMessageInterceptor())
			.handler(wxEventHandler(WxConsts.EVT_UNSUBSCRIBE))
			.next()
			
			.rule()
		    .interceptor(wxMpMessageInterceptor())
		    .handler(wxMpMessageHandler())
		    .end();
		
		return router;
	}
}
