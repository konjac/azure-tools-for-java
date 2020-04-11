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

package com.microsoft.tooling.msservices.serviceexplorer.azure.springcloud;

import com.microsoft.azure.management.appplatform.v2019_05_01_preview.DeploymentResourceStatus;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.AppResourceInner;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.DeploymentResourceInner;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.azuretools.core.mvp.model.springcloud.SpringCloudIdHelper;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.*;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureNodeActionPromptListener;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.microsoft.azuretools.telemetry.TelemetryConstants.*;

public class SpringCloudAppNode extends Node implements SpringCloudAppNodeView, IDataRefreshableComponent<AppResourceInner, DeploymentResourceInner> {
    private static final Logger LOGGER = Logger.getLogger(SpringCloudAppNodeView.class.getName());
    private static final String DELETE_APP_PROMPT_MESSAGE = "This operation will delete the Spring Cloud App: %s.\n"
            + "Are you sure you want to continue?";
    private static final String FAILED_TO_DELETE_APP = "Failed to delete Spring Cloud App: %s";
    private static final String ERROR_DELETING_APP = "Azure Services Explorer - Error Deleting Spring Cloud App";
    private static final String FAILED_TO_START_APP = "Failed to start Spring Cloud App: %s";
    private static final String FAILED_TO_RESTART_APP = "Failed to restart Spring Cloud App: %s";
    private static final String FAILED_TO_STOP_APP = "Failed to stop Spring Cloud App: %s";
    private static final String DELETE_APP_PROGRESS_MESSAGE = "Deleting Spring Cloud App";

    private static final String ACTION_START = "Start";
    private static final String ACTION_STOP = "Stop";
    private static final String ACTION_DELETE = "Delete";
    private static final String ACTION_RESTART = "Restart";
    private static final String ACTION_OPEN_IN_BROWSER = "Open In Browser";
    private static final String ACTION_SHOW_PROPERTY = "Show Properties";
    private static final String ACTION_OPEN_IN_PORTAL = "Open In Portal";

    private AppResourceInner app;
    private DeploymentResourceStatus status;
    private final String clusterName;
    private final String subscriptionId;

    private final SpringCloudAppNodePresenter springCloudAppNodePresenter;

    public SpringCloudAppNode(AppResourceInner app, DeploymentResourceInner deploy, SpringCloudNode parent) {
        super(app.id(), app.name(), parent, getIconForStatus(deploy == null ? DeploymentResourceStatus.UNKNOWN : deploy.properties().status()));
        this.app = app;
        this.clusterName = SpringCloudIdHelper.getClusterName(app.id());
        this.subscriptionId = SpringCloudIdHelper.getSubscriptionId(app.id());
        springCloudAppNodePresenter = new SpringCloudAppNodePresenter();
        springCloudAppNodePresenter.onAttachView(this);
        fillData(app, deploy);
        DefaultAzureResourceTracker.getInstance().registerNode(getAppId(), this);
    }

    @Override
    public List<NodeAction> getNodeActions() {
        syncActionState();
        return super.getNodeActions();
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getAppName() {
        return app.name();
    }

    public String getAppId() {
        return app.id();
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public void notifyDataRefresh(AppResourceInner newApp, DeploymentResourceInner deploy) {
        fillData(newApp, deploy);
    }

    @Override
    protected void loadActions() {
        addAction(ACTION_START, new WrappedTelemetryNodeActionListener(SPRING_CLOUD, START_SPRING_CLOUD_APP,
                createBackgroundActionListener("Starting", () -> startSpringCloudApp())));
        addAction(ACTION_STOP, new WrappedTelemetryNodeActionListener(SPRING_CLOUD, STOP_SPRING_CLOUD_APP,
                createBackgroundActionListener("Stopping", () -> stopSpringCloudApp())));

        addAction(ACTION_RESTART, new WrappedTelemetryNodeActionListener(SPRING_CLOUD, RESTART_SPRING_CLOUD_APP,
                createBackgroundActionListener("Restarting", () -> restartSpringCloudApp())));

        addAction(ACTION_DELETE, new DeleteSpringCloudAppAction());

        addAction(ACTION_OPEN_IN_PORTAL, new WrappedTelemetryNodeActionListener(SPRING_CLOUD, OPEN_IN_PORTAL_SPRING_CLOUD_APP,
                new NodeActionListener() {
                    @Override
                    protected void actionPerformed(NodeActionEvent e) throws AzureCmdException {
                        openResourcesInPortal(getSubscriptionId(), getAppId());
                    }
                }));
        addAction(ACTION_OPEN_IN_BROWSER, new WrappedTelemetryNodeActionListener(SPRING_CLOUD, OPEN_IN_BROWSER_SPRING_CLOUD_APP,
                new NodeActionListener() {
                    @Override
                    protected void actionPerformed(NodeActionEvent e) {
                        if (StringUtils.isNotEmpty(app.properties().url())) {
                            DefaultLoader.getUIHelper().openInBrowser(app.properties().url());
                        } else {
                            DefaultLoader.getUIHelper().showInfo(SpringCloudAppNode.this, "Public url is not available for app: " + app.name());
                        }
                    }
                }));
        addAction(ACTION_SHOW_PROPERTY, new WrappedTelemetryNodeActionListener(SPRING_CLOUD, SHOWPROP_SPRING_CLOUD_APP,
                new NodeActionListener() {
                    @Override
                    protected void actionPerformed(NodeActionEvent e) {
                        DefaultLoader.getUIHelper().openSpringCloudAppPropertyView(SpringCloudAppNode.this);
                    }
                }));
        super.loadActions();
    }

    private static NodeActionListener createBackgroundActionListener(final String actionName, final Runnable runnable) {
        return new NodeActionListener() {
            @Override
            protected void actionPerformed(NodeActionEvent e) {
                DefaultLoader.getIdeHelper().runInBackground(null, actionName, false,
                        true, String.format("%s...", actionName), runnable);
            }
        };
    }

    private static String getStatusDisplay(DeploymentResourceStatus status) {
        return status == null ? "Unknown" : status.toString();
    }

    private static String getIconForStatus(DeploymentResourceStatus statusEnum) {
        final String displayText = getStatusDisplay(statusEnum);
        String simpleStatus = displayText.endsWith("ing") && !StringUtils.equalsIgnoreCase(displayText, "running") ? "pending" : displayText.toLowerCase();
        return String.format("azure-springcloud-app-%s.png", simpleStatus);
    }

    private void startSpringCloudApp() {
        try {
            springCloudAppNodePresenter.onStartSpringCloudApp(this.app.id(), this.app.properties().activeDeploymentName(), status);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format(FAILED_TO_START_APP, this.app.name()), e);
        }
    }

    private void restartSpringCloudApp() {
        try {
            springCloudAppNodePresenter.onReStartSpringCloudApp(this.app.id(), this.app.properties().activeDeploymentName(), status);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format(FAILED_TO_RESTART_APP, this.app.name()), e);
        }
    }

    private void stopSpringCloudApp() {
        try {
            springCloudAppNodePresenter.onStopSpringCloudApp(this.app.id(), this.app.properties().activeDeploymentName(), status);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format(FAILED_TO_STOP_APP, this.app.name()), e);
        }
    }

    private void syncActionState() {
        if (status != null) {
            boolean stopped = DeploymentResourceStatus.STOPPED.equals(status);
            boolean running = DeploymentResourceStatus.RUNNING.equals(status);
            boolean unknown = DeploymentResourceStatus.UNKNOWN.equals(status);
            boolean allocating = DeploymentResourceStatus.ALLOCATING.equals(status);
            boolean hasURL = StringUtils.isNotEmpty(app.properties().url()) && app.properties().url().startsWith("http");
            getNodeActionByName(ACTION_OPEN_IN_BROWSER).setEnabled(hasURL && running);
            getNodeActionByName(ACTION_START).setEnabled(stopped);
            getNodeActionByName(ACTION_STOP).setEnabled(!stopped && !unknown && !allocating);
            getNodeActionByName(ACTION_RESTART).setEnabled(!stopped && !unknown && !allocating);
        } else {
            getNodeActionByName(ACTION_OPEN_IN_BROWSER).setEnabled(false);
            getNodeActionByName(ACTION_START).setEnabled(false);
            getNodeActionByName(ACTION_STOP).setEnabled(false);
            getNodeActionByName(ACTION_RESTART).setEnabled(false);
        }
    }

    private void fillData(AppResourceInner newApp, DeploymentResourceInner deploy) {
        if (newApp == null) {
            getParent().removeNode(SpringCloudIdHelper.getSubscriptionId(getAppId()), getAppId(), SpringCloudAppNode.this);
            DefaultAzureResourceTracker.getInstance().unregisterNode(getAppId(), this);
            return;
        }
        this.status = deploy == null ? DeploymentResourceStatus.UNKNOWN : deploy.properties().status();
        this.app = newApp;
        this.setIconPath(getIconForStatus(status));
        this.setName(String.format("%s - %s", app.name(), getStatusDisplay(status)));
        syncActionState();
    }


    private class DeleteSpringCloudAppAction extends AzureNodeActionPromptListener {
        DeleteSpringCloudAppAction() {
            super(SpringCloudAppNode.this, String.format(DELETE_APP_PROMPT_MESSAGE, SpringCloudIdHelper.getAppName(SpringCloudAppNode.this.id)),
                  DELETE_APP_PROGRESS_MESSAGE);
        }

        @Override
        protected void azureNodeAction(NodeActionEvent event) {
            try {
                springCloudAppNodePresenter.onDeleteApp(id);
            } catch (IOException e) {
                DefaultLoader.getUIHelper().showException(String.format(FAILED_TO_DELETE_APP, SpringCloudAppNode.this.getName()),
                        e, ERROR_DELETING_APP, false, true);
            }
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e) {
        }

        @Override
        protected String getServiceName(NodeActionEvent event) {
            return SPRING_CLOUD;
        }

        @Override
        protected String getOperationName(NodeActionEvent event) {
            return DELETE_SPRING_CLOUD_APP;
        }
    }
}
