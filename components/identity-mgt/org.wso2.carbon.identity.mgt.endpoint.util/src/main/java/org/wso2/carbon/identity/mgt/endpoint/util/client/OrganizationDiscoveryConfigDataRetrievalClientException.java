/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * Exception for organization discovery config data retrieval.
 */
public class OrganizationDiscoveryConfigDataRetrievalClientException extends IdentityException {

    /**
     * Client Exception with error message.
     *
     * @param message Error message.
     */
    public OrganizationDiscoveryConfigDataRetrievalClientException(String message) {

        super(message);
    }

    /**
     * Client exception with message and a throwable.
     *
     * @param message   Error message.
     * @param throwable Throwable.
     */
    public OrganizationDiscoveryConfigDataRetrievalClientException(String message, Throwable throwable) {

        super(message, throwable);
    }

    /**
     * Client exception with error code, message and a throwable.
     *
     * @param errorCode Error code.
     * @param message   Error message.
     * @param throwable Throwable.
     */
    public OrganizationDiscoveryConfigDataRetrievalClientException(String errorCode, String message,
                                                                   Throwable throwable) {

        super(errorCode, message, throwable);
    }

}
