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

package com.microsoft.azure.cosmosspark.sdk.common;

import com.microsoft.azure.hdinsight.sdk.common.AzureHttpObservable;
import com.microsoft.azure.hdinsight.sdk.common.azure.serverless.AzureSparkServerlessAccount;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CosmosSparkHttpObservable extends AzureHttpObservable {
    public static final String KOBO_ACCOUNT_HEADER_NAME = "x-ms-kobo-account-name";

    private AzureSparkServerlessAccount adlAccount;

    public CosmosSparkHttpObservable(@NotNull String tenantId, @NotNull AzureSparkServerlessAccount adlAccount) {
        super(tenantId, "");
        this.adlAccount = adlAccount;
    }

    @Override
    public Header[] getDefaultHeaders() throws IOException {
        Header[] defaultHeaders = super.getDefaultHeaders();
        List<Header> headers = Arrays.stream(defaultHeaders)
                      .filter(header -> !header.getName().equals(KOBO_ACCOUNT_HEADER_NAME))
                      .collect(Collectors.toList());

        headers.add(new BasicHeader(KOBO_ACCOUNT_HEADER_NAME, adlAccount.getName()));

        return headers.toArray(new Header[0]);
    }
}
