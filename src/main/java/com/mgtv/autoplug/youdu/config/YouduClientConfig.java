package com.mgtv.autoplug.youdu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import im.youdu.entapp.AppClient;

@Configuration
public class YouduClientConfig {
	
	@Value("${youdu.address}")
	private String address; // 请填写有度服务器地址
	
	@Value("${youdu_buin}")
    private int buin; // 请填写企业总机号码
	
	@Value("${youdu_appId}")
	private String appId; // 请填写企业应用AppId
	
	@Value("${youdu_encodingaesKey}")
    private String encodingaesKey; // 请填写企业应用的EncodingaesKey
	

	private AppClient singleAppClient;
	
	@Bean
	public AppClient appClient() {
		if(singleAppClient==null) {
			singleAppClient = new AppClient(address, buin, appId, encodingaesKey);
		}
		return singleAppClient;
	}
}
