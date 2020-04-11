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

package com.microsoft.azure.hdinsight.sdk.rest.azure.datalake.analytics.accounts.models;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for OperationOrigin.
 */
public final class OperationOrigin extends ExpandableStringEnum<OperationOrigin> {
    /** Static value user for OperationOrigin. */
    public static final OperationOrigin USER = fromString("user");

    /** Static value system for OperationOrigin. */
    public static final OperationOrigin SYSTEM = fromString("system");

    /** Static value user,system for OperationOrigin. */
    public static final OperationOrigin USERSYSTEM = fromString("user,system");

    /**
     * Creates or finds a OperationOrigin from its string representation.
     * @param name a name to look for
     * @return the corresponding OperationOrigin
     */
    @JsonCreator
    public static OperationOrigin fromString(String name) {
        return fromString(name, OperationOrigin.class);
    }

    /**
     * @return known OperationOrigin values
     */
    public static Collection<OperationOrigin> values() {
        return values(OperationOrigin.class);
    }
}
