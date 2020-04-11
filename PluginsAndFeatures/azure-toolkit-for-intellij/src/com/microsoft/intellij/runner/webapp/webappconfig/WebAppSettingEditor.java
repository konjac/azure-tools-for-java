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

package com.microsoft.intellij.runner.webapp.webappconfig;

import com.intellij.openapi.project.Project;
import com.microsoft.intellij.runner.AzureSettingPanel;
import com.microsoft.intellij.runner.AzureSettingsEditor;
import com.microsoft.intellij.runner.webapp.webappconfig.slimui.WebAppSlimSettingPanel;
import com.microsoft.intellij.runner.webapp.webappconfig.ui.WebAppSettingPanel;
import org.jetbrains.annotations.NotNull;

public class WebAppSettingEditor extends AzureSettingsEditor<WebAppConfiguration> {

    private final AzureSettingPanel mainPanel;
    private final WebAppConfiguration webAppConfiguration;

    public WebAppSettingEditor(Project project, @NotNull WebAppConfiguration webAppConfiguration) {
        super(project);
        if (webAppConfiguration.getUiVersion() == IntelliJWebAppSettingModel.UIVersion.NEW) {
            mainPanel = new WebAppSlimSettingPanel(project, webAppConfiguration);
        } else {
            mainPanel = new WebAppSettingPanel(project, webAppConfiguration);
        }
        this.webAppConfiguration = webAppConfiguration;
    }

    @Override
    @NotNull
    protected AzureSettingPanel getPanel() {
        return this.mainPanel;
    }
}
