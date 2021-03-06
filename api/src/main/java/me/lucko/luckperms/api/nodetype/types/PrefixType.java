/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.api.nodetype.types;

import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.nodetype.NodeType;
import me.lucko.luckperms.api.nodetype.NodeTypeKey;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * A sub-type of {@link Node} used to store prefix assignments.
 *
 * @since 4.2
 */
public interface PrefixType extends NodeType {

    /**
     * The key for this type.
     */
    NodeTypeKey<PrefixType> KEY = new NodeTypeKey<PrefixType>(){};

    /**
     * Gets the priority of the prefix assignment.
     *
     * @return the priority
     */
    int getPriority();

    /**
     * Gets the actual prefix string.
     *
     * @return the prefix
     */
    @NonNull String getPrefix();

    /**
     * Gets a representation of this instance as a {@link Map.Entry}.
     *
     * @return a map entry representation of the priority and prefix string
     */
    Map.@NonNull Entry<Integer, String> getAsEntry();

}
