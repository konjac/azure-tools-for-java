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

package com.microsoft.azure.cosmosspark.serverexplore.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.microsoft.azure.cosmosspark.common.JXHyperLinkWithUri;
import com.microsoft.azure.cosmosspark.serverexplore.CosmosSparkClusterStatesCtrlProvider;
import com.microsoft.azure.cosmosspark.serverexplore.CosmosSparkClusterStatesModel;
import com.microsoft.azure.cosmosspark.serverexplore.cosmossparknode.CosmosSparkClusterNode;
import com.microsoft.azure.hdinsight.common.mvc.SettableControl;
import com.microsoft.azure.hdinsight.sdk.common.azure.serverless.AzureSparkCosmosCluster;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.intellij.rxjava.IdeaSchedulers;
import rx.Subscription;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;

public class CosmosSparkClusterMonitorDialog extends DialogWrapper
        implements SettableControl<CosmosSparkClusterStatesModel> {
    private JLabel masterStateLabel;
    private JLabel workerStateLabel;
    private JLabel masterTargetLabel;
    private JLabel masterRunningLabel;
    private JLabel masterFailedLabel;
    private JLabel masterOutstandingLabel;
    private JLabel workerTargetLabel;
    private JLabel workerRunningLabel;
    private JLabel workerFailedLabel;
    private JLabel workerOutStandingLabel;
    private JLabel clusterStateLabel;
    private JXHyperLinkWithUri sparkHistoryHyperLink;
    private JXHyperLinkWithUri sparkMasterHyperLink;
    private JPanel monitorDialogPanel;
    private JTextField clusterIDField;

    @Nullable
    private Subscription refreshSub;
    @NotNull
    private final CosmosSparkClusterStatesCtrlProvider ctrlProvider;

    private static final int REFRESH_INTERVAL = 2;

    public CosmosSparkClusterMonitorDialog(@NotNull final CosmosSparkClusterNode clusterNode,
                                           @NotNull final AzureSparkCosmosCluster cluster) {
        super((Project) clusterNode.getProject(), true);
        this.ctrlProvider = new CosmosSparkClusterStatesCtrlProvider(
                this, new IdeaSchedulers((Project) clusterNode.getProject()), cluster);

        init();
        this.setTitle(String.format("Cluster Status(%s)", cluster.getName()));
        this.setModal(true);
        this.getWindow().addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(final WindowEvent e) {
                refreshSub = ctrlProvider.updateAll()
                                         .retryWhen(ob -> ob.delay(REFRESH_INTERVAL, TimeUnit.SECONDS))
                                         .repeatWhen(ob -> ob.delay(REFRESH_INTERVAL, TimeUnit.SECONDS))
                                         .subscribe();
                super.windowOpened(e);
            }

            @Override
            public void windowClosing(final WindowEvent e) {
                if (refreshSub != null) {
                    refreshSub.unsubscribe();
                }
                super.windowClosing(e);
            }
        });
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return clusterIDField;
    }

    // Data -> Components
    @Override
    public void setData(@NotNull final CosmosSparkClusterStatesModel data) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            masterStateLabel.setText(data.getMasterState());
            workerStateLabel.setText(data.getWorkerState());

            masterTargetLabel.setText(String.valueOf(data.getMasterTarget()));
            workerTargetLabel.setText(String.valueOf(data.getWorkerTarget()));

            masterRunningLabel.setText(String.valueOf(data.getMasterRunning()));
            workerRunningLabel.setText(String.valueOf(data.getWorkerRunning()));

            masterFailedLabel.setText(String.valueOf(data.getMasterFailed()));
            workerFailedLabel.setText(String.valueOf(data.getWorkerFailed()));

            masterOutstandingLabel.setText(String.valueOf(data.getMasterOutstanding()));
            workerOutStandingLabel.setText(String.valueOf(data.getWorkerOutstanding()));

            sparkHistoryHyperLink.setURI(data.getSparkHistoryUri());
            sparkMasterHyperLink.setURI(data.getSparkMasterUri());

            clusterStateLabel.setText(data.getClusterState());
            clusterIDField.setText(data.getClusterID());
            clusterIDField.setBorder(null);
        }, ModalityState.any());
    }

    // Components -> Data
    @Override
    public void getData(@NotNull final CosmosSparkClusterStatesModel data) {
    }

    @NotNull
    @Override
    protected Action[] createLeftSideActions() {
        return new Action[0];
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return monitorDialogPanel;
    }

}
