package com.cloume.radar.wxapp.message;

import java.util.Map;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

public class WxMpMessageHandlerImpl implements WxMpMessageHandler {

	@Override
	public WxMpXmlOutMessage handle(
			WxMpXmlMessage message, 
			Map<String, Object> data, 
			WxMpService service,
			WxSessionManager sessionManager) throws WxErrorException {
		
		///以下为测试用户获取测试入口链接而增加
		if(message.getMsgType().equalsIgnoreCase(WxConsts.CUSTOM_MSG_TEXT)){
			/*if(message.getContent().equals("我要参加内测")){
				WxMpCustomMessage.WxArticle article = new WxMpCustomMessage.WxArticle();
				String url = service.shortUrl("http://jiajiao.tunnel.qydev.com/service-wx/oauth2/aHR0cDovL2ppYWppYW8udHVubmVsLnF5ZGV2LmNvbS9jbGllbnQtd3g=");
				article.setUrl(url);
				article.setDescription("感谢您参与我们的测试!");
				article.setTitle("华师家教1.0内测");
				
				WxMpCustomMessage msg = WxMpCustomMessage.NEWS()
						.toUser(message.getFromUserName())
						.addArticle(article)
						.build();
				
				service.customMessageSend(msg);
			}*/
		}
		
		return null;
	}

}
