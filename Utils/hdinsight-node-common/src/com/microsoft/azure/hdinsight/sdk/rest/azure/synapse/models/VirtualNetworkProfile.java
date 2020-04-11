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

package com.microsoft.azure.hdinsight.sdk.rest.azure.synapse.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Virtual Network Profile.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualNetworkProfile {
    /**
     * Subnet ID used for computes in workspace.
     */
    @JsonProperty(value = "computeSubnetId")
    private String computeSubnetId;

    /**
     * Get subnet ID used for computes in workspace.
     *
     * @return the computeSubnetId value
     */
    public String computeSubnetId() {
        return this.computeSubnetId;
    }

    /**
     * Set subnet ID used for computes in workspace.
     *
     * @param computeSubnetId the computeSubnetId value to set
     * @return the VirtualNetworkProfile object itself.
     */
    public VirtualNetworkProfile withComputeSubnetId(String computeSubnetId) {
        this.computeSubnetId = computeSubnetId;
        return this;
    }

}
