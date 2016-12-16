package com.cloume.radar.wxapp.wx.message;

import java.util.Map;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;

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
		
		///UserRepository ur = (UserRepository) data.get("UserRepository");

		switch(getEvent()){
			case WxConsts.EVT_SUBSCRIBE: {/*
				///创建新用户
				///WxMpUser wxMpUser = service.userInfo(message.getFromUserName(), null);
				User user = ur.findOne(message.getFromUserName());
				if(user == null){
					user = new User(message.getFromUserName());
					ur.save(user);
				}
				
				WxMpCustomMessage.WxArticle article = new WxMpCustomMessage.WxArticle();
				article.setUrl("http://hsep.tunnel.qydev.com/client-wx/menu/index");
				//article.setPicUrl("http://hsvk.cloume.com/hsvk/img/quiz.png");
				article.setDescription("选择注册用户角色可以获取更好的服务支持");
				article.setTitle("请您选择用户角色");
				
				WxMpCustomMessage msg = WxMpCustomMessage.NEWS()
						.toUser(message.getFromUserName())
						.addArticle(article)
						.build();
				
				service.customMessageSend(msg);*/
			} break;
			
			case WxConsts.EVT_UNSUBSCRIBE: {/*
				///FIXME: disable the user, 目前直接删除
				ur.delete(message.getFromUserName());*/
			} break;
		}
		
		return null;
	}	
}
