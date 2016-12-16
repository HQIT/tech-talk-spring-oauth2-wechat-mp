package com.cloume.radar.wxapp.message;

import java.util.Map;

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
public class WxClickEventHandler implements WxMpMessageHandler {
	
	private String eventKey;
	
	protected String getEventKey(){
		return eventKey;
	}
	
	public WxClickEventHandler(String key){
		///Logger.getLogger("WxAskQuestionHandler").info("WxAskQuestionHandler constructed!");
		this.eventKey = key;
	}

	@Override
	public WxMpXmlOutMessage handle(
			WxMpXmlMessage message, 
			Map<String, Object> data, 
			WxMpService service,
			WxSessionManager sm) throws WxErrorException {

		switch(getEventKey()){
			/*
				WxMpCustomMessage.WxArticle article = new WxMpCustomMessage.WxArticle();
				article.setUrl("http://www.62226222.com.cn");
				//article.setPicUrl("http://hsvk.cloume.com/hsvk/img/quiz.png");
				article.setDescription("我们正在全力搭建更好的平台为您服务");
				article.setTitle("关于华师家教中心");
				
				WxMpCustomMessage msg = WxMpCustomMessage.NEWS()
						.toUser(message.getFromUserName())
						.addArticle(article)
						.build();
				
				service.customMessageSend(msg);
			*/
		}
		
		return null;
	}
}
