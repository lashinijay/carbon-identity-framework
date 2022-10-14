/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.mgt.listeners;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.utils.CarbonUtils.isLegacyAuditLogsDisabled;

/**
 * This audit logger logs the User Management success activities.
 */
public class UserManagementAuditLogger extends AbstractIdentityUserOperationEventListener {

    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private static final String SUCCESS = "Success";
    private static final String IN_PROGRESS = "In-Progress";
    public static final String USER_AGENT_QUERY_KEY = "User-Agent";
    public static final String USER_AGENT_KEY = "User Agent";
    public static final String REMOTE_ADDRESS_QUERY_KEY = "remoteAddress";
    public static final String REMOTE_ADDRESS_KEY = "RemoteAddress";
    public static final String SERVICE_PROVIDER_KEY = "ServiceProviderName";
    public static final String SERVICE_PROVIDER_QUERY_KEY = "serviceProvider";
    private final String USER_NAME_KEY = "UserName";
    private final String USER_NAME_QUERY_KEY = "userName";

    @Override
    public boolean isEnable() {

        if (super.isEnable()) {
            return !isLegacyAuditLogsDisabled();
        }
        return false;
    }

    @Override
    public boolean doPostAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                 String profile, UserStoreManager userStoreManager) {

        if (isEnable()) {

            JSONObject data = new JSONObject();
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);

            if (LoggerUtils.isLogMaskingEnable) {
                Map<String, String> sanitizedClaims = LoggerUtils.maskClaimValues(claims);
                data.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(sanitizedClaims));
                target = LoggerUtils.maskContent(target);
            } else {
                data.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(claims));
            }
            if (ArrayUtils.isNotEmpty(roleList)) {
                data.put(ListenerUtils.ROLES_FIELD, new JSONArray(roleList));
            }
            data.put(ListenerUtils.PROFILE_FIELD, profile);
            audit.warn(createAuditMessage(ListenerUtils.ADD_USER_ACTION, target, data, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                target = LoggerUtils.maskContent(target);
            }
            audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_ACTION, target, null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                Map<String, String> claims = new HashMap<>();
                claims.put(claimURI, claimValue);
                Map<String, String> sanitizedClaims = LoggerUtils.maskClaimValues(claims);
                dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, sanitizedClaims.get(claimURI));
                target = LoggerUtils.maskContent(target);
            } else {
                dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, claimValue);
            }
            dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claimURI);
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            audit.info(createAuditMessage(ListenerUtils.SET_USER_CLAIM_VALUE_ACTION, target, dataObject, IN_PROGRESS));
        }
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                target = LoggerUtils.maskContent(target);
            }
            audit.warn(createAuditMessage(ListenerUtils.SET_USER_CLAIM_VALUE_ACTION, target, null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                Map<String, String> sanitizedClaims = LoggerUtils.maskClaimValues(claims);
                target = LoggerUtils.maskContent(target);
                dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(sanitizedClaims));
            } else {
                dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(claims));
            }
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            audit.warn(createAuditMessage(ListenerUtils.SET_USER_CLAIM_VALUES_ACTION, target, dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                target = LoggerUtils.maskContent(target);
            }
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(claims));
            audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_CLAIM_VALUES_ACTION, target, dataObject,
                    IN_PROGRESS));
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValues(String userName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                target = LoggerUtils.maskContent(target);
            }
            audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_CLAIM_VALUES_ACTION,target , null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                target = LoggerUtils.maskContent(target);
                profileName = LoggerUtils.maskContent(profileName);
            }
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claimURI);
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_CLAIM_VALUE_ACTION, target, dataObject,
                    IN_PROGRESS));
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValue(String userName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                target = LoggerUtils.maskContent(target);
            }
            audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_CLAIM_VALUE_ACTION, target, null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdateCredential(String userName, Object credential, UserStoreManager userStoreManager) {

        if (isEnable()) {
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                target = LoggerUtils.maskContent(target);
            }
            audit.warn(createAuditMessage(ListenerUtils.CHANGE_PASSWORD_BY_USER_ACTION, target, null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdateCredentialByAdmin(String userName, Object newCredential,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                target = LoggerUtils.maskContent(target);
            }
            audit.info(createAuditMessage(ListenerUtils.CHANGE_PASSWORD_BY_ADMIN_ACTION, target, null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            audit.warn(createAuditMessage(ListenerUtils.DELETE_ROLE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostAddRole(String roleName, String[] userList, Permission[] permissions,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(userList)) {
                if (LoggerUtils.isLogMaskingEnable) {
                    String[] sanitizedUserList = new String[userList.length];
                    for (int count = 0; count < userList.length; count++) {
                        sanitizedUserList[count] = LoggerUtils.maskContent(userList[count]);
                    }
                    dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(sanitizedUserList));
                } else {
                    dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(userList));
                }
            }
            if (ArrayUtils.isNotEmpty(permissions)) {
                JSONArray permissionsArray = new JSONArray(permissions);
                dataObject.put(ListenerUtils.PERMISSIONS_FIELD, permissionsArray);
            }
            if (IdentityUtil.isGroupsVsRolesSeparationImprovementsEnabled()) {
                audit.warn(createAuditMessage(ListenerUtils.ADD_GROUP_ACTION,
                        ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
            } else {
                audit.warn(createAuditMessage(ListenerUtils.ADD_ROLE_ACTION,
                        ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
            }
        }
        return true;
    }

    @Override
    public boolean doPostAddInternalRoleWithID(String roleName, String[] userIDs, Permission[] permissions,
                                               UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(userIDs)) {
                dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(userIDs));
            }
            if (ArrayUtils.isNotEmpty(permissions)) {
                JSONArray permissionsArray = new JSONArray(permissions);
                dataObject.put(ListenerUtils.PERMISSIONS_FIELD, permissionsArray);
            }
            audit.warn(createAuditMessage(ListenerUtils.ADD_ROLE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdateRoleName(String roleName, String newRoleName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.NEW_ROLE_NAME, newRoleName);
            audit.warn(createAuditMessage(ListenerUtils.UPDATE_ROLE_NAME_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdatePermissionsOfRole(String roleName, Permission[] permissions, UserStoreManager
            userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(permissions)) {
                JSONArray permissionsArray = new JSONArray(permissions);
                dataObject.put(ListenerUtils.PERMISSIONS_FIELD, permissionsArray);
            }

            audit.warn(createAuditMessage(ListenerUtils.UPDATE_PERMISSIONS_OF_ROLE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(deletedUsers)) {
                if (LoggerUtils.isLogMaskingEnable) {
                    String[] sanitizedDeletedUsers = new String[deletedUsers.length];
                    for (int count = 0; count < deletedUsers.length; count++) {
                        sanitizedDeletedUsers[count] = LoggerUtils.maskContent(deletedUsers[count]);
                    }
                    dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(sanitizedDeletedUsers));
                } else {
                    dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(deletedUsers));
                }
            }
            if (ArrayUtils.isNotEmpty(newUsers)) {
                if (LoggerUtils.isLogMaskingEnable) {
                    String[] sanitizedNewUsers = new String[newUsers.length];
                    for (int count = 0; count < newUsers.length; count++) {
                        sanitizedNewUsers[count] = LoggerUtils.maskContent(newUsers[count]);
                    }
                    dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(sanitizedNewUsers));
                } else {
                    dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(newUsers));
                }
            }
            audit.info(createAuditMessage(ListenerUtils.UPDATE_USERS_OF_ROLE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                target = LoggerUtils.maskContent(target);
            }
            if (ArrayUtils.isNotEmpty(deletedRoles)) {
                dataObject.put(ListenerUtils.DELETED_ROLES, new JSONArray(deletedRoles));
            }
            if (ArrayUtils.isNotEmpty(newRoles)) {
                dataObject.put(ListenerUtils.NEW_ROLES, new JSONArray(newRoles));
            }
            audit.info(createAuditMessage(ListenerUtils.UPDATE_ROLES_OF_USER_ACTION, target, dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostGetUserClaimValue(String userName, String claim, List<String> claimValue, String profileName,
            UserStoreManager storeManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claim);
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, storeManager);
            Map<String, String> claims = new HashMap<>();
            if (LoggerUtils.isLogMaskingEnable && !claimValue.isEmpty()) {
                for (String claimVal : claimValue) {
                    claims.put(claim, claimVal);
                }
                Map<String, String> sanitizedClaims = LoggerUtils.maskClaimValues(claims);
                dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, new JSONArray(sanitizedClaims.values()));
                target = LoggerUtils.maskContent(target);
            }
            else if (CollectionUtils.isNotEmpty(claimValue)) {
                dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, new JSONArray(claimValue));
            }
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            audit.info(createAuditMessage(ListenerUtils.GET_USER_CLAIM_VALUE_ACTION, target, dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostGetUserClaimValues(String userName, String[] claims, String profileName,
            Map<String, String> claimMap, UserStoreManager storeManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, storeManager);
            if (LoggerUtils.isLogMaskingEnable) {
                Map<String, String> sanitizedClaims = LoggerUtils.maskClaimValues(claimMap);
                dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(sanitizedClaims));
                target = LoggerUtils.maskContent(target);
            } else {
                dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(claimMap));
            }
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            audit.info(createAuditMessage(ListenerUtils.GET_USER_CLAIM_VALUES_ACTION, target, dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostGetUserList(String claimUri, String claimValue, final List<String> returnValues,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (LoggerUtils.isLogMaskingEnable) {
                for (int index = 0; index < returnValues.size(); index++) {
                    returnValues.set(index, LoggerUtils.maskContent(returnValues.get(index)));
                }
                Map<String, String> claims = new HashMap<>();
                claims.put(claimUri, claimValue);
                Map<String, String> sanitizedClaims = LoggerUtils.maskClaimValues(claims);
                dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, sanitizedClaims.get(claimUri));
            } else {
                dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, claimValue);
            }
            dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claimUri);
            if (CollectionUtils.isNotEmpty(returnValues)) {
                dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(returnValues));
            }
            audit.info(createAuditMessage(ListenerUtils.GET_USER_LIST_ACTION, null, dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostGetRoleListOfUser(String userName, String filter, String[] roleList,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
            if (LoggerUtils.isLogMaskingEnable) {
                target = LoggerUtils.maskContent(target);
            }
            dataObject.put(ListenerUtils.FILTER_FIELD, filter);
            if (ArrayUtils.isNotEmpty(roleList)) {
                dataObject.put(ListenerUtils.ROLES_FIELD, new JSONArray(roleList));
            }
            audit.info(createAuditMessage(ListenerUtils.GET_ROLES_OF_USER_ACTION, target, dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostGetUserListOfRole(String roleName, String[] userList, UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(userList)) {
                if (LoggerUtils.isLogMaskingEnable) {
                    String[] sanitizedUserList = new String[userList.length];
                    for (int index = 0; index < userList.length; index++) {
                        sanitizedUserList[index] = LoggerUtils.maskContent(userList[index]);
                    }
                    dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(sanitizedUserList));
                } else {
                    dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(userList));
                }
            }
            audit.info(createAuditMessage(ListenerUtils.GET_USERS_OF_ROLE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public int getExecutionOrderId() {
        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 1;
    }

    /**
     * To create an audit message based on provided parameters.
     *
     * @param action      Activity
     * @param target      Target affected by this activity.
     * @param data        Information passed along with the request.
     * @param resultField Result value.
     * @return Relevant audit log in Json format.
     */
    private String createAuditMessage(String action, String target, JSONObject data, String resultField) {

        if (data == null) {
            data = new JSONObject();
        }
        String initiator = null;

        if (LoggerUtils.isLogMaskingEnable) {
            String username = MultitenantUtils.getTenantAwareUsername(ListenerUtils.getUser());
            String tenantDomain = MultitenantUtils.getTenantDomain(ListenerUtils.getUser());
            if (StringUtils.isNotBlank(tenantDomain)) {
                initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
            }
            if (StringUtils.isBlank(initiator)) {
                initiator = LoggerUtils.maskContent(ListenerUtils.getUser());
            }
        } else {
            initiator = ListenerUtils.getUser();
        }
        addContextualAuditParams(data);
        String auditMessage =
                ListenerUtils.INITIATOR + "=%s " + ListenerUtils.ACTION + "=%s " + ListenerUtils.TARGET + "=%s "
                        + ListenerUtils.DATA + "=%s " + ListenerUtils.OUTCOME + "=%s";
        return String.format(auditMessage, initiator, action, target, data, resultField);
    }

    private void addContextualAuditParams(JSONObject jsonObject) {

        jsonObject.put(REMOTE_ADDRESS_KEY, MDC.get(REMOTE_ADDRESS_QUERY_KEY));
        jsonObject.put(USER_AGENT_KEY, MDC.get(USER_AGENT_QUERY_KEY));
        jsonObject.put(USER_NAME_KEY, MDC.get(USER_NAME_QUERY_KEY));
        jsonObject.put(SERVICE_PROVIDER_KEY, MDC.get(SERVICE_PROVIDER_QUERY_KEY));
    }
}
