package com.cloume.radar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

	@RequestMapping(value="/test", method = RequestMethod.GET)
	@ResponseBody
	public String hello(@RequestParam(defaultValue= "world") String param) {
		System.err.printf("hello: %s \n", param);
		return "hello, spring web";
	}
}