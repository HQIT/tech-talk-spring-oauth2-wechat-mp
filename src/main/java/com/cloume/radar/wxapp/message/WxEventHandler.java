package com.cloume.radar.wxapp.message;

import java.util.Map;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * 用户点击听课工具中的"我要提问"
 * @author Gang
 *
 */
public class WxEventHandler implements WxMpMessageHandler {
	
	private String event;
	
	protected String getEvent(){
		return event;
	}
	
	public WxEventHandler(String event){
		this.event = event;
	}

	@Override
	public WxMpXmlOutMessage handle(
			WxMpXmlMessage message, 
			Map<String, Object> data, 
			WxMpService service,
			WxSessionManager sm) throws WxErrorException {

		switch(getEvent()){
			case WxConsts.EVT_SUBSCRIBE: {
				///创建新用户
			} break;
			
			case WxConsts.EVT_UNSUBSCRIBE: {
			} break;
		}
		
		return null;
	}	
}
