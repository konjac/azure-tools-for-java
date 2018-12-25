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

package com.microsoft.azure.hdinsight.spark.run.configuration

import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.microsoft.azure.hdinsight.sdk.rest.azure.serverless.spark.models.CreateSparkBatchJobParameters
import com.microsoft.azure.hdinsight.spark.common.CosmosSparkSubmitModel
import com.microsoft.azure.hdinsight.spark.common.SparkSubmissionParameter
import com.microsoft.azuretools.utils.Pair
import java.util.stream.Stream

class CosmosServerlessSparkSubmitModel(project: Project) : CosmosSparkSubmitModel(project, CreateSparkBatchJobParameters()) {
    init {
        setSparkEventsDirectoryPath("spark-events")
    }

    @Attribute("sparkevents_directory")
    fun getSparkEventsDirectoryPath(): String {
        return (submissionParameter as CreateSparkBatchJobParameters).sparkEventsDirectoryPath()
    }

    @Attribute("sparkevents_directory")
    fun setSparkEventsDirectoryPath(path: String) {
        (submissionParameter as CreateSparkBatchJobParameters).withSparkEventsDirectoryPath(path)
    }

    override fun getDefaultParameters(): Stream<Pair<String, out Any>> {
        return listOf(
                Pair(CreateSparkBatchJobParameters.DriverMemory, CreateSparkBatchJobParameters.DriverMemoryDefaultValue),
                Pair(CreateSparkBatchJobParameters.DriverCores, CreateSparkBatchJobParameters.DriverCoresDefaultValue),
                Pair(CreateSparkBatchJobParameters.ExecutorMemory, CreateSparkBatchJobParameters.ExecutorMemoryDefaultValue),
                Pair(CreateSparkBatchJobParameters.ExecutorCores, CreateSparkBatchJobParameters.ExecutorCoresDefaultValue),
                Pair(CreateSparkBatchJobParameters.NumExecutors, CreateSparkBatchJobParameters.NumExecutorsDefaultValue)
        ).stream()
    }
}