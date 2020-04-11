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

package com.microsoft.azuretools.container.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class ConfigFileUtil {
    private static String configFileName = "azuretools-config.properties";

    /**
     * load config.
     */
    public static Properties loadConfig(IProject project) {
        Properties prop = new Properties();
        IFile file = project.getFile(configFileName);
        if (!file.exists()) {
            return prop;
        }
        try {
            prop.load(file.getContents());
        } catch (IOException | CoreException e) {
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * save config.
     */
    public static void saveConfig(IProject project, Properties prop) {
        IFile file = project.getFile(configFileName);

        try {
            ByteArrayOutputStream ous = new ByteArrayOutputStream();
            prop.store(ous, null);
            if (file.exists()) {
                file.setContents(new ByteArrayInputStream(ous.toByteArray()), true, true, null);
            } else {
                file.create(new ByteArrayInputStream(ous.toByteArray()), true, null);
            }

        } catch (Exception io) {
            io.printStackTrace();
        }
    }

}
