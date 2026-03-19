package com.chat.cdc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "realtime-server.outbox")
public record CdcProperties(int partitions) { }
