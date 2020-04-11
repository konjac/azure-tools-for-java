/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azuretools.authmanage;

import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Tenant;
import com.microsoft.azuretools.Constants;
import com.microsoft.azuretools.adauth.*;
import com.microsoft.azuretools.authmanage.models.AdAuthDetails;
import com.microsoft.azuretools.authmanage.models.AuthMethodDetails;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.sdkmanage.AccessTokenAzureManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class AdAuthManager extends BaseADAuthManager {
    private IWebUi webUi;
    private static class LazyLoader {
        static final AdAuthManager INSTANCE = new AdAuthManager();;
    }

    /**
     * Get the AdAuthManager singleton instance.
     * @return AdAuthManager singleton instance.
     */
    public static AdAuthManager getInstance() {
        return LazyLoader.INSTANCE;
    }

    /**
     * Get access token.
     * @param tid String, tenant id.
     * @param resource String, resource url.
     * @param promptBehavior PromptBehavior, prompt enum.
     * @return String access token.
     * @throws IOException thrown when fail to get access token.
     */
    public String getAccessToken(String tid, String resource, PromptBehavior promptBehavior) throws IOException {
        AuthContext ac = createContext(tid, null);
        AuthResult result = null;
        try {
            result = ac.acquireToken(resource, false, adAuthDetails.getAccountEmail(), false,
                this.webUi, Constants.redirectUri);
        } catch (AuthException e) {
            if (AuthError.InvalidGrant.equalsIgnoreCase(e.getError())
                    || AuthError.InteractionRequired.equalsIgnoreCase(e.getError())) {
                throw new IOException(AUTHORIZATION_REQUIRED_MESSAGE, e);
            } else {
                throw e;
            }
        }
        return result.getAccessToken();
    }

    /**
     * Try to sign in with persisted authentication result.
     *
     * @param authMethodDetails The authentication method detail for helping
     * @return true for success
     */
    @Override
    public synchronized boolean tryRestoreSignIn(@NotNull final AuthMethodDetails authMethodDetails) {
        if (secureStore == null || authMethodDetails.getAzureEnv() == null ||
                // Restore only for the same saved Azure environment with current
                !CommonSettings.getEnvironment().getName().equals(authMethodDetails.getAzureEnv())) {
            return false;
        }

        AdAuthDetails originAdAuthDetails = adAuthDetails;

        adAuthDetails = new AdAuthDetails();
        adAuthDetails.setAccountEmail(authMethodDetails.getAccountEmail());

        try {
            // Try to restore
            AuthResult savedAuth = loadFromSecureStore();

            if (savedAuth != null) {
                signIn(savedAuth, null);

                return true;
            }
        } catch (Exception ignored) {
            LOGGER.info("The cached token is expired, can't restore it.");
            cleanCache();
        }

        adAuthDetails = originAdAuthDetails;
        return false;
    }

    /**
     * Sign in azure account.
     * @return AuthResult, auth result.
     * @throws IOException thrown when failed to get auth result.
     */
    @Override
    public AuthResult signIn(@Nullable AuthenticationCallback<AuthenticationResult> callback) throws IOException {
        return signIn(null, callback);
    }

    /**
     * Sign in azure account with saved authentication result.
     *
     * @param savedAuth saved authentication result, null for signing in from scratch
     * @return AuthResult, auth result.
     * @throws IOException thrown when failed to get auth result.
     */
    private AuthResult signIn(@Nullable AuthResult savedAuth,
                              @Nullable AuthenticationCallback<AuthenticationResult> callback)
            throws IOException {

        // build token cache for azure and graph api
        // using azure sdk directly

        AuthResult result;

        if (savedAuth == null) {
            cleanCache();
            AuthContext ac = createContext(getCommonTenantId(), null);
            // todo: to determine which acquireToken to call, device login or interactive login
            // todo: https://github.com/Microsoft/azure-tools-for-java/pull/1623
            result = ac.acquireToken(env.managementEndpoint(), true, null,
                false, this.webUi, Constants.redirectUri);
        } else {
            result = savedAuth;
        }

        String userId = result.getUserId();
        boolean isDisplayable = result.isUserIdDisplayble();

        Map<String, List<String>> tidToSidsMap = new HashMap<>();

        final AccessTokenAzureManager accessTokenAzureManager= new AccessTokenAzureManager(this);
        List<Tenant> tenants =  accessTokenAzureManager.getTenants(getCommonTenantId());
        for (Tenant t : tenants) {
            String tid = t.tenantId();
            AuthContext ac1 = createContext(tid, null);
            // put tokens into the cache
            try {
                ac1.acquireToken(env.managementEndpoint(), false, userId, isDisplayable,
                    this.webUi, Constants.redirectUri);
            } catch (AuthException e) {
                //TODO: should narrow to AuthError.InteractionRequired
                ac1.acquireToken(env.managementEndpoint(), true, userId, isDisplayable,
                    this.webUi, Constants.redirectUri);
            }

            // FIXME!!! Some environments and subscriptions can't get the resource manager token
            // Let the log in process passed, and throwing the errors when to access those resources
            try {
                ac1.acquireToken(env.resourceManagerEndpoint(), false, userId, isDisplayable,
                    this.webUi, Constants.redirectUri);
            } catch (AuthException e) {
                if (CommonSettings.getEnvironment() instanceof ProvidedEnvironment) {
                    // Swallow the exception since some provided environments are not full featured
                    LOGGER.warning("Can't get " + env.resourceManagerEndpoint() + " access token from environment " +
                            CommonSettings.getEnvironment().getName());
                }
            }

            try {
                ac1.acquireToken(env.graphEndpoint(), false, userId, isDisplayable,
                    this.webUi, Constants.redirectUri);
            } catch (AuthException e) {
                if (CommonSettings.getEnvironment() instanceof ProvidedEnvironment) {
                    // Swallow the exception since some provided environments are not full featured
                    LOGGER.warning("Can't get " + env.graphEndpoint() + " access token from environment " +
                            CommonSettings.getEnvironment().getName());
                }
            }

            // ADL account access token
            try {
                ac1.acquireToken(env.dataLakeEndpointResourceId(), false, userId, isDisplayable,
                    this.webUi, Constants.redirectUri);
            } catch (AuthException e) {
                LOGGER.warning("Can't get " + env.dataLakeEndpointResourceId() + " access token from environment " +
                        CommonSettings.getEnvironment().getName() + "for user " + userId);
            }

            // TODO: remove later
            // ac1.acquireToken(Constants.resourceVault, false, userId, isDisplayable);
            List<String> sids = new LinkedList<>();
            for (Subscription s : accessTokenAzureManager.getSubscriptions(tid)) {
                sids.add(s.subscriptionId());
            }
            tidToSidsMap.put(t.tenantId(), sids);
        }

        if (!isDisplayable) {
            throw new IllegalArgumentException("accountEmail is null");
        }

        adAuthDetails.setAccountEmail(userId);
        adAuthDetails.setTidToSidsMap(tidToSidsMap);

        saveToSecureStore(result);

        return result;
    }

    public Map<String, List<String>> getAccountTenantsAndSubscriptions() {
        return adAuthDetails.getTidToSidsMap();
    }

    private AdAuthManager() {
        super();
        webUi = CommonSettings.getUiFactory().getWebUi();
    }
}
