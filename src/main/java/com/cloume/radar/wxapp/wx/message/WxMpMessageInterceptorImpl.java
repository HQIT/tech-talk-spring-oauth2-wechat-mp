package com.cloume.radar.wxapp.wx.message;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.cloume.radar.wxapp.repository.UserRepository;

import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageInterceptor;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;

public class WxMpMessageInterceptorImpl implements WxMpMessageInterceptor {

	@Autowired private UserRepository userRepository;
	protected UserRepository getUserRepository(){
		return userRepository;
	}
	
	@Override
	public boolean intercept(
			WxMpXmlMessage message, 
			Map<String, Object> data, 
			WxMpService service, 
			WxSessionManager sessionManager) throws WxErrorException {
		
		data.put("UserRepository", getUserRepository());
		
		/**
		 * 判断发送人是否为新用户? 是否有课程
		 * 判断发送人是否为老师?
		 * 是:
		 *   判断发送人是否为讲课模式下的老师?
		 *   是: 转发这个人的消息到当前所在课堂的所有学生
		 *   否: 一般消息处理
		 * 否:
		 *   判断是否有课正在上?
		 *   判断所在课堂是否为提问时间?
		 */
		
		return true;
	}

}
