package com.cloume.radar.wxapp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeixinController {

	@RequestMapping(value = "/say")
	public String oauth(
			@RequestParam(defaultValue = "hello world") String what
			) {
		return what;
	}
}
