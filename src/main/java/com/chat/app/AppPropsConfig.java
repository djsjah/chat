package com.chat.app;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.chat.cdc.CdcProperties;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({ CdcProperties.class })
public class AppPropsConfig { }
