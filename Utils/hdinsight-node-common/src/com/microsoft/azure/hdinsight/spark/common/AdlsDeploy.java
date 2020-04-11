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

package com.microsoft.azure.hdinsight.spark.common;

import com.microsoft.azure.hdinsight.common.MessageInfoType;
import com.microsoft.azure.hdinsight.spark.jobs.JobUtils;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import rx.Observable;
import rx.Observer;

import java.io.File;
import java.util.AbstractMap;

// for cluster with adls gen1 account to deploy using ADLS storage account type
public class AdlsDeploy implements Deployable {
    @NotNull
    private String adlsRootPath;
    @NotNull
    private String accessToken;

    public AdlsDeploy(@NotNull String adlsRootPath,
                      @NotNull String accessToken) {
        this.adlsRootPath = adlsRootPath;
        this.accessToken = accessToken;
    }

    @NotNull
    @Override
    public Observable<String> deploy(File src,
                                     Observer<AbstractMap.SimpleImmutableEntry<MessageInfoType, String>> logSubject) {
        return JobUtils.deployArtifactToADLS(src.getAbsolutePath(), adlsRootPath, accessToken);
    }
}
