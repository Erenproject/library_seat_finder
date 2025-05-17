package com.library.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpResponse;

@SpringBootApplication
@EnableScheduling // 啟用定時任務
public class LibraryApplication {

	@Autowired
	private ObjectMapper objectMapper;

	public static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);
	}
	
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		
		// 添加StringHttpMessageConverter，支持所有媒體類型
		List<MediaType> supportedMediaTypes = new ArrayList<>();
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);
		supportedMediaTypes.add(MediaType.TEXT_PLAIN);
		supportedMediaTypes.add(MediaType.TEXT_HTML);
		supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
		supportedMediaTypes.add(MediaType.ALL);
		
		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
		stringConverter.setSupportedMediaTypes(supportedMediaTypes);
		
		// 添加更靈活的JSON轉換器
		MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
		jacksonConverter.setSupportedMediaTypes(supportedMediaTypes);
		
		// 使用注入的ObjectMapper，確保一致性
		jacksonConverter.setObjectMapper(objectMapper);
		
		// 更新消息轉換器
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(stringConverter);
		messageConverters.add(jacksonConverter);
		restTemplate.setMessageConverters(messageConverters);
		
		// 添加日誌攔截器
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add((request, body, execution) -> {
			System.out.println("發送請求: " + request.getMethod() + " " + request.getURI());
			System.out.println("請求頭: " + request.getHeaders());
			
			// 執行請求
			ClientHttpResponse response = execution.execute(request, body);
			
			// 記錄響應信息
			System.out.println("響應狀態碼: " + response.getStatusCode());
			System.out.println("響應頭: " + response.getHeaders());
			
			return response;
		});
		restTemplate.setInterceptors(interceptors);
		
		return restTemplate;
	}
}
