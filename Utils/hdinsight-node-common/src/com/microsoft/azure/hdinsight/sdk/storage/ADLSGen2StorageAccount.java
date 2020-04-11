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

package com.microsoft.azure.hdinsight.sdk.storage;

import com.microsoft.azure.hdinsight.common.logger.ILogger;
import com.microsoft.azure.hdinsight.sdk.cluster.IClusterDetail;
import com.microsoft.azure.hdinsight.sdk.common.AzureHttpObservable;
import com.microsoft.azure.hdinsight.sdk.rest.azure.storageaccounts.StorageAccountAccessKey;
import com.microsoft.azure.hdinsight.sdk.rest.azure.storageaccounts.api.PostListKeysResponse;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.authmanage.models.SubscriptionDetail;
import rx.Observable;

public class ADLSGen2StorageAccount extends HDStorageAccount implements ILogger {
    public final static String DefaultScheme = "abfs";

    public ADLSGen2StorageAccount(IClusterDetail clusterDetail, String fullStorageBlobName, String key, boolean isDefault, String defaultFileSystem, String scheme) {
        super(clusterDetail, fullStorageBlobName, key, isDefault, defaultFileSystem);
        this.scheme = scheme;
        key = getAccessKeyList(clusterDetail.getSubscription())
                .toBlocking()
                .firstOrDefault(new StorageAccountAccessKey())
                .getValue();

        this.setPrimaryKey(key);
    }

    public ADLSGen2StorageAccount(IClusterDetail clusterDetail, String fullStorageBlobName, boolean isDefault, String defaultFileSystem) {
        super(clusterDetail, fullStorageBlobName, null, isDefault, defaultFileSystem);
        this.scheme = DefaultScheme;
    }

    public String getStorageRootPath() {
        return String.format("%s://%s@%s", this.getscheme(), this.getDefaultContainer(), this.getFullStorageBlobName());
    }

    @Override
    public StorageAccountType getAccountType() {
        return StorageAccountType.ADLSGen2;
    }

    private Observable<StorageAccountAccessKey> getAccessKeyList(SubscriptionDetail subscription) {
        return Observable.fromCallable(() -> AuthMethodManager.getInstance().getAzureManager().getAzure(subscription.getSubscriptionId()))
                .flatMap(azure -> azure.storageAccounts().listAsync())
                .doOnNext(accountList -> log().debug(String.format("Listing storage accounts in subscription %s, accounts %s", subscription.getSubscriptionName(), accountList)))
                .filter(accountList -> accountList.name().equals(getName()))
                .map(ac -> ac.resourceGroupName())
                .first()
                .doOnNext(rgName -> log().info(String.format("Finish getting storage account %s resource group name %s", getName(), rgName)))
                .flatMap(rgName -> new AzureHttpObservable(subscription, "2018-07-01").post(String.format("https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Storage/storageAccounts/%s/listKeys",
                        subscription.getSubscriptionId(), rgName, getName()), null, null, null, PostListKeysResponse.class))
                .flatMap(keyList -> Observable.from(keyList.getKeys()));
    }
}
