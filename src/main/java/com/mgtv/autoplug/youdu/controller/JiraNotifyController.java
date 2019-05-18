package com.mgtv.autoplug.youdu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.mgtv.autoplug.youdu.service.MessageSender;

import net.sf.json.JSONObject;

@RestController
public class JiraNotifyController {
	
	public final Logger logger = LoggerFactory.getLogger(JiraNotifyController.class);
	
	@Autowired
	private MessageSender messageSender;
	
	@RequestMapping(value="/sendText", method=RequestMethod.POST)
	public String sendText(@RequestParam(required=true) String toUsers,
			@RequestParam(required=true) String message) {
		messageSender.sendTxt(toUsers, null, message);
		return "OK";
	}
	
	//curl -i -X POST -H 'Content-type':'application/json' -d '{"toUsers":"jingtao","message":"hello jingtao"}' http://127.0.0.1:9000/youdu/sendMessage
	@RequestMapping(value="/sendMessage", method=RequestMethod.POST, consumes="application/json;charset=UTF-8",produces = "application/json;charset=UTF-8")
	public String sendMessage(@RequestBody JSONObject jsonParam) {
		logger.info("Youdu sendMessage:"+jsonParam.toString());
		if(jsonParam.get("toUsers")==null || jsonParam.get("message")==null) {
			return "Fail";
		}else {
			
		}
		messageSender.sendTxt(jsonParam.getString("toUsers"), null, jsonParam.getString("message"));
		return "OK";
	}
}
