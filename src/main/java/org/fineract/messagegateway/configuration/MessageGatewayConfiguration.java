/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fineract.messagegateway.configuration;

import org.fineract.messagegateway.sms.domain.AbstractPersistableCustom;
import org.fineract.messagegateway.sms.domain.SMSBridge;
import org.fineract.messagegateway.sms.domain.SMSBridgeConfig;
import org.fineract.messagegateway.sms.domain.SMSMessage;
import org.fineract.messagegateway.tenants.domain.Tenant;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration(exclude = { org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class })
@EnableJpaRepositories(basePackages = {
        "org.fineract.messagegateway.sms.repository",
        "org.fineract.messagegateway.tenants.repository"

})
@EntityScan(basePackageClasses = {
        AbstractPersistableCustom.class,
        SMSBridge.class,
        SMSBridgeConfig.class,
        SMSMessage.class,
        Tenant.class
})
@ComponentScan(basePackages = {
        "org.fineract.messagegateway.*"
})
public class MessageGatewayConfiguration {

    public MessageGatewayConfiguration() {
        super();
    }

    @Bean
    public SimpleApplicationEventMulticaster applicationEventMulticaster() {
        final SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
        multicaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return multicaster;
    }

}
