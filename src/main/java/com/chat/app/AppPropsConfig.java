package com.chat.app;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.chat.cdc.CdcProperties;

@Configuration
@EnableConfigurationProperties({ CdcProperties.class })
public class AppPropsConfig { }
