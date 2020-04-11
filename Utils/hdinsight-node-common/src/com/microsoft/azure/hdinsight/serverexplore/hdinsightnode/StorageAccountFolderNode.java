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

package com.microsoft.azure.hdinsight.serverexplore.hdinsightnode;

import com.microsoft.azure.hdinsight.common.ClusterManagerEx;
import com.microsoft.azure.hdinsight.common.CommonConst;
import com.microsoft.azure.hdinsight.common.HDInsightLoader;
import com.microsoft.azure.hdinsight.common.logger.ILogger;
import com.microsoft.azure.hdinsight.sdk.cluster.HDInsightAdditionalClusterDetail;
import com.microsoft.azure.hdinsight.sdk.cluster.IClusterDetail;
import com.microsoft.azure.hdinsight.sdk.storage.HDStorageAccount;
import com.microsoft.azure.hdinsight.sdk.storage.IHDIStorageAccount;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.RefreshableNode;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
import java.util.Optional;

public class StorageAccountFolderNode extends RefreshableNode implements ILogger {
    private static final String STORAGE_ACCOUNT_FOLDER_MODULE_ID = StorageAccountFolderNode.class.getName();
    private static final String STORAGE_ACCOUNT_NAME = "Storage Accounts";
    private static final String ICON_PATH = CommonConst.StorageAccountFoldIConPath;

    @NotNull
    private IClusterDetail clusterDetail;
    public StorageAccountFolderNode(Node parent, @NotNull IClusterDetail clusterDetail) {
        super(STORAGE_ACCOUNT_FOLDER_MODULE_ID, STORAGE_ACCOUNT_NAME, parent, ICON_PATH);
        this.clusterDetail = clusterDetail;
    }

    @Override
    protected void onNodeClick(NodeActionEvent e) {
        if (ClusterManagerEx.getInstance().isHdiReaderCluster(clusterDetail)) {
            HDInsightLoader.getHDInsightHelper().createRefreshHdiReaderStorageAccountsWarningForm(
                    StorageAccountFolderNode.this, ClusterNode.ASE_DEEP_LINK);
        } else if (clusterDetail instanceof HDInsightAdditionalClusterDetail
                && !isStorageAccountsAvailable(clusterDetail)) {
            HDInsightLoader.getHDInsightHelper().createRefreshHdiLinkedClusterStorageAccountsWarningForm(
                    StorageAccountFolderNode.this, ClusterNode.ASE_DEEP_LINK);
        }

        super.onNodeClick(e);
    }

    @Override
    protected void refreshItems() {
        if (!clusterDetail.isEmulator()) {
            try {
                if (isStorageAccountsAvailable(clusterDetail)) {
                    clusterDetail.getConfigurationInfo();

                    Optional.ofNullable(clusterDetail.getStorageAccount())
                            .map(defaultStorageAccount -> new StorageAccountNode(this, defaultStorageAccount, clusterDetail,true))
                            .ifPresent(this::addChildNode);

                    List<HDStorageAccount> additionalStorageAccount = clusterDetail.getAdditionalStorageAccounts();
                    if (additionalStorageAccount != null) {
                        for (HDStorageAccount account : additionalStorageAccount) {
                            addChildNode(new StorageAccountNode(this, account, clusterDetail, false));
                        }
                    }
                }
            } catch (Exception ex) {
                String exceptionMsg = ex.getCause() == null ? "" : ex.getCause().getMessage();
                String errorHint = String.format("Failed to get HDInsight cluster %s configuration. ", clusterDetail.getName());
                log().warn(errorHint + ExceptionUtils.getStackTrace(ex));

                DefaultLoader.getUIHelper().showError(errorHint + exceptionMsg, "HDInsight Explorer");
            }
        }
    }

    private boolean isStorageAccountsAvailable(@NotNull IClusterDetail clusterDetail) {
        IHDIStorageAccount defaultStorageAccount = clusterDetail.getStorageAccount();
        List<HDStorageAccount> additionalStorageAccounts = clusterDetail.getAdditionalStorageAccounts();
        return defaultStorageAccount != null ||
                (additionalStorageAccounts != null && additionalStorageAccounts.size() > 0);
    }

    @Override
    public String getServiceName() {
        return TelemetryConstants.HDINSIGHT;
    }
}
