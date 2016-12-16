package com.cloume.radar.wxapp.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;

@Controller
public class WeixinController {
	
	@Autowired private WxMpService wxMpService;
	@Autowired private WxMpConfigStorage wxMpConfigStorage;
	@Autowired private WxMpMessageRouter wxMpMessageRouter;
	
	@Value(value = "${service.baseUrl}") private String serviceBaseUrl;
	@Value(value = "${client.version}") private String clientVersion;
	
	/**
	 * @param returnUrl base64 encoded return url
	 * @param session 用户在restapi中的http session id
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/oauth2/{return}")
	public void oauth(
			@RequestParam(value = "session", defaultValue = "") String session,
			@PathVariable("return") String returnUrl, HttpServletResponse response) throws IOException{		
		String url = wxMpService.oauth2buildAuthorizationUrl(
				serviceBaseUrl + "/wxoauthcallback?session=" + session + "&return=" + returnUrl, 
				WxConsts.OAUTH2_SCOPE_USER_INFO,
				"UNAUTHED");
		response.sendRedirect(url);
	}
	
	@RequestMapping(value = "/wxoauthcallback")
	public void wxOAuthCallback(HttpServletRequest request, HttpServletResponse response)
			throws IOException, WxErrorException {
		String code = request.getParameter("code");
		if (code == null || code.isEmpty()) {
			response.sendRedirect("/");
			return;
		}

		WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
		//getMongoTemplate().save(new OAuth2AccessToken(wxMpOAuth2AccessToken), "oauth2");

		WxMpUser wxMpUser = wxMpService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);
		///getMongoTemplate().save(new WxUser(wxMpUser), "wxuser");
		if(wxMpUser == null){
			response.sendRedirect("/");
			return;
		}
		
		//TODO:处理业务逻辑，根据OpenID查询用户注册状态并进行相应的跳转
		
		String returnUrl = request.getParameter("return");
		returnUrl = returnUrl.replace('_', '/');
		returnUrl = new String(Base64.decodeBase64(returnUrl));
		returnUrl += String.format("%s%s=%s&%s=%s&%s=%s", 
				returnUrl.contains("?") ? "&" : "?",
				"openid", wxMpUser.getOpenId(),
				"v", clientVersion,
				"t", System.currentTimeMillis()
				);
		response.sendRedirect(returnUrl);
	}
	
	/**
	 * appId, timestamp, nonceStr, signature
	 * @return JSSDK配置
	 * @throws WxErrorException
	 */
	@ResponseBody @RequestMapping(value = "/jsapisign", method = RequestMethod.POST)
	public WxJsapiSignature wxJSSdkConfiguration(@RequestBody Map<String, Object> body, HttpServletResponse response) throws WxErrorException{
		/*wx.config({
		    debug: true, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
		    appId: '', // 必填，公众号的唯一标识
		    timestamp: , // 必填，生成签名的时间戳
		    nonceStr: '', // 必填，生成签名的随机串
		    signature: '',// 必填，签名，见附录1
		    jsApiList: [] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
		});*/
		WxJsapiSignature signature = wxMpService.createJsapiSignature(String.valueOf(body.get("url")));
		
		return signature;
	}
	
	@RequestMapping(value = "/service")
	public void service(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("signature") String signature, @RequestParam("nonce") String nonce,
			@RequestParam("timestamp") String timestamp) throws IOException {

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
			// 消息签名不正确，说明不是公众平台发过来的消息
			response.getWriter().println("非法请求");
			return;
		}

		String echostr = request.getParameter("echostr");
		if (!StringUtils.isEmpty(echostr)) {
			// 说明是一个仅仅用来验证的请求，回显echostr
			response.getWriter().println(echostr);
			return;
		}

		String encryptType = StringUtils.isEmpty(request.getParameter("encrypt_type")) ? "raw" : request.getParameter("encrypt_type");

		WxMpXmlMessage inMessage = null;
		if ("raw".equals(encryptType)) {
			// 明文传输的消息
			inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
		} else if ("aes".equals(encryptType)) {
			// 是aes加密的消息
			String msgSignature = request.getParameter("msg_signature");
			inMessage = WxMpXmlMessage.fromEncryptedXml(request.getInputStream(), wxMpConfigStorage, timestamp, nonce,
					msgSignature);
		} else {
			response.getWriter().println("不可识别的加密类型");
			return;
		}

		/// wxMpMessageRouter.setSessionManager(wxSessionManager);
		WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
		if (outMessage != null) {
			if ("raw".equals(encryptType)) {
				response.getWriter().write(outMessage.toXml());
			} else if ("aes".equals(encryptType)) {
				response.getWriter().write(outMessage.toEncryptedXml(wxMpConfigStorage));
			}
			return;
		}
	}
}
