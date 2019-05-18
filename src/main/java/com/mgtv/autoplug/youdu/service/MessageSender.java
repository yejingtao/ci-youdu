package com.mgtv.autoplug.youdu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import im.youdu.entapp.AppClient;
import im.youdu.entapp.exception.AESCryptoException;
import im.youdu.entapp.exception.HttpRequestException;
import im.youdu.entapp.exception.ParamParserException;
import im.youdu.entapp.message.Message;
import im.youdu.entapp.message.TextBody;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageSender {
	
	@Autowired
	@Qualifier("appClient")
	private AppClient appClient;
	
	public void sendTxt(String toUsers, String toGroups, String message) {
    	TextBody body = new TextBody(message);
        Message msg = new Message(toUsers, toGroups, Message.MessageTypeText, body);
        try {
        	appClient.sendMsg(msg);
		} catch (AESCryptoException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} catch (ParamParserException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} catch (HttpRequestException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
    
	}
}
