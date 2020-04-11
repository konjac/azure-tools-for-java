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

package com.microsoft.azure.hdinsight.spark.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBPanel;
import com.microsoft.azure.hdinsight.common.mvc.SettableControl;
import com.microsoft.azure.hdinsight.spark.common.SparkBatchJobConfigurableModel;
import com.microsoft.azure.hdinsight.spark.common.SparkSubmitModel;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.telemetry.TelemetryConstants;

import javax.swing.*;
import java.awt.*;

public class SparkBatchJobConfigurable implements SettableControl<SparkBatchJobConfigurableModel>, Disposable {
    private JTabbedPane executionTypeTabPane;
    private JPanel myWholePanel;
    private SparkLocalRunParamsPanel localRunParamsPanel;
    private JPanel commonPanel;
    private JPanel remoteRunParamsPanel;
    public SparkSubmissionContentPanel submissionContentPanel;
    private SparkCommonRunParametersPanel commonRunParametersPanel;

    @NotNull
    private final Project myProject;

    @NotNull
    public String getServiceName() {
        return TelemetryConstants.HDINSIGHT;
    }

    public SparkBatchJobConfigurable(@NotNull final Project project) {
        this.myProject = project;
    }

    @NotNull
    public JComponent getComponent() {
        return myWholePanel;
    }

    protected void createUIComponents() {
        localRunParamsPanel = new SparkLocalRunParamsPanel(getProject()).withInitialize();
        remoteRunParamsPanel = new JPanel(new VerticalFlowLayout(0, 5));
        setClusterSubmissionPanel(createSubmissionPanel());

        this.commonPanel = new JBPanel();
        this.commonRunParametersPanel = new SparkCommonRunParametersPanel(this.myProject, this);
        this.commonPanel.setLayout(new BorderLayout());
        this.commonPanel.add(commonRunParametersPanel.getComponent());
    }

    protected SparkSubmissionContentPanel createSubmissionPanel() {
        return new SparkSubmissionDebuggablePanel(getProject());
    }

    @Override
    public void setData(@NotNull SparkBatchJobConfigurableModel data) {
        // Data -> Component
        localRunParamsPanel.setData(data.getLocalRunConfigurableModel());

        SparkSubmitModel submitModel = data.getSubmitModel();
        submitModel.setClusterSelectable(data.isClusterSelectionEnabled());
        submissionContentPanel.setData(data.getSubmitModel());

        executionTypeTabPane.setSelectedIndex(data.getFocusedTabIndex());

        commonRunParametersPanel.setMainClassName(data.getSubmitModel().getMainClassName());

        // Presentation only
        setLocalRunConfigEnabled(data.isLocalRunConfigEnabled());
    }

    @Override
    public void getData(@NotNull SparkBatchJobConfigurableModel data) {
        // Component -> Data
        localRunParamsPanel.getData(data.getLocalRunConfigurableModel());
        submissionContentPanel.getData(data.getSubmitModel());
        data.setFocusedTabIndex(executionTypeTabPane.getSelectedIndex());

        data.getLocalRunConfigurableModel().setRunClass(commonRunParametersPanel.getMainClassName());
        data.getSubmitModel().setMainClassName(commonRunParametersPanel.getMainClassName());
    }

    @NotNull
    public Project getProject() {
        return myProject;
    }

    protected synchronized void setClusterSubmissionPanel(SparkSubmissionContentPanel clusterSubmissionPanel) {
        this.submissionContentPanel = clusterSubmissionPanel;
        Disposer.register(this, this.submissionContentPanel);
        remoteRunParamsPanel.add(clusterSubmissionPanel.getComponent());
    }

    private void setLocalRunConfigEnabled(boolean enabled) {
        executionTypeTabPane.setEnabledAt(0, enabled);
    }

    public void validateInputs() throws ConfigurationException {
        submissionContentPanel.validateInputs();
        commonRunParametersPanel.validateInputs();
    }

    @Override
    public void dispose() {
    }
}
