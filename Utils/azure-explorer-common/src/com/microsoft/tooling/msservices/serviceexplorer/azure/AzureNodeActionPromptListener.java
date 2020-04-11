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

package com.microsoft.tooling.msservices.serviceexplorer.azure;

import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.Node;

import java.util.concurrent.Callable;

public abstract class AzureNodeActionPromptListener extends AzureNodeActionListener {
    private final String promptMessage;
    private boolean optionDialog;

    public AzureNodeActionPromptListener(@NotNull Node azureNode,
                                         @NotNull String promptMessage,
                                         @NotNull String progressMessage) {
        super(azureNode, progressMessage);
        this.promptMessage = promptMessage;
    }

    @NotNull
    @Override
    protected Callable<Boolean> beforeAsyncActionPerformed() {
        return () -> {
            DefaultLoader.getIdeHelper().invokeAndWait(() -> optionDialog = DefaultLoader.getUIHelper()
                    .showConfirmation(this.azureNode.getTree().getParent(),
                            promptMessage,
                            "Azure Explorer",
                            new String[]{"Yes", "No"},
                            null));

            return optionDialog;
        };
    }
}
