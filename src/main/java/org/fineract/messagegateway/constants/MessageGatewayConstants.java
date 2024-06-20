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
package org.fineract.messagegateway.constants;

public interface MessageGatewayConstants {

	String TENANT_IDENTIFIER_HEADER = "Fineract-Platform-TenantId";
	
	String TENANT_APPKEY_HEADER =  "Fineract-Tenant-App-Key" ;
	
	
	String PROVIDER_ACCOUNT_ID = "Provider_Account_Id" ;
	
	String PROVIDER_AUTH_TOKEN = "Provider_Auth_Token" ;
	
	String PROVIDER_URL = "Provider_Url";
	String PROVIDER_AUTH_TYPE = "Provider_Auth_Type";
	String PROVIDER_AUTH_CUSTOM_PREFIX = "Provider_Auth_Custom_Prefix";

	String SENDER_NAME = "Sender_Name";

	String PROVIDER_API_KEY = "Provider_API_Key";
	String PROVIDER_PROJECT_ID = "Provider_Project_Id";
}
