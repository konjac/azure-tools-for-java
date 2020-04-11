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

package com.microsoft.azuretools.container.handlers;

import java.nio.file.Paths;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;

import com.microsoft.azuretools.container.Constant;
import com.microsoft.azuretools.container.ui.PushImageDialog;
import com.microsoft.azuretools.container.utils.WarUtil;
import com.microsoft.azuretools.core.actions.MavenExecuteAction;
import com.microsoft.azuretools.core.utils.AzureAbstractHandler;
import com.microsoft.azuretools.core.utils.MavenUtils;
import com.microsoft.azuretools.core.utils.PluginUtil;
import com.microsoft.tooling.msservices.components.DefaultLoader;

public class PushImageHandler extends AzureAbstractHandler {

    private static final String MAVEN_GOALS = "package";
    private IProject project;
    private String destinationPath;
    private String basePath;

    @Override
    public Object onExecute(ExecutionEvent event) throws ExecutionException {
        project = PluginUtil.getSelectedProject();
        if (project == null) {
            return null;
        }
        basePath = project.getLocation().toString();

        try {
            if (MavenUtils.isMavenProject(project)) {
                destinationPath = MavenUtils.getTargetPath(project);
                MavenExecuteAction action = new MavenExecuteAction(MAVEN_GOALS);
                IContainer container = MavenUtils.getPomFile(project).getParent();
                action.launch(container, () -> {
                    buildAndRun();
                    return null;
                });
            } else {
                destinationPath = Paths.get(basePath, Constant.DOCKERFILE_FOLDER, project.getName() + ".war")
                        .normalize().toString();
                WarUtil.export(project, destinationPath);
                buildAndRun();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendTelemetryOnException(event, e);
        }
        return null;
    }

    private void buildAndRun() {
        DefaultLoader.getIdeHelper().invokeAndWait(() -> {
            PushImageDialog pushImageDialog = new PushImageDialog(PluginUtil.getParentShell(), basePath,
                    destinationPath);
            pushImageDialog.open();
        });
    }
}
