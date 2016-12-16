package com.cloume.radar.wxapp.message;

import java.util.Map;

import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageInterceptor;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;

public class WxMpMessageInterceptorImpl implements WxMpMessageInterceptor {

	@Override
	public boolean intercept(
			WxMpXmlMessage message, 
			Map<String, Object> data, 
			WxMpService service, 
			WxSessionManager sessionManager) throws WxErrorException {
		
		///data.put("UserRepository", getUserRepository());
		
		return true;
	}

}
