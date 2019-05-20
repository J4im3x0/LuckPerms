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

package me.lucko.luckperms.api.node;

import me.lucko.luckperms.api.node.types.DisplayNameNode;
import me.lucko.luckperms.api.node.types.InheritanceNode;
import me.lucko.luckperms.api.node.types.MetaNode;
import me.lucko.luckperms.api.node.types.PermissionNode;
import me.lucko.luckperms.api.node.types.PrefixNode;
import me.lucko.luckperms.api.node.types.RegexPermissionNode;
import me.lucko.luckperms.api.node.types.SuffixNode;
import me.lucko.luckperms.api.node.types.WeightNode;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * TODO
 */
public interface NodeBuilderRegistry {

    /**
     * TODO
     *
     * @param key
     * @return
     */
    @NonNull NodeBuilder<?, ?> forKey(String key);

    /**
     * TODO
     *
     * @return
     */
    PermissionNode.@NonNull Builder forPermission();

    /**
     * TODO
     *
     * @return
     */
    RegexPermissionNode.@NonNull Builder forRegexPermission();

    /**
     * TODO
     *
     * @return
     */
    InheritanceNode.@NonNull Builder forInheritance();

    /**
     * TODO
     *
     * @return
     */
    PrefixNode.@NonNull Builder forPrefix();

    /**
     * TODO
     *
     * @return
     */
    SuffixNode.@NonNull Builder forSuffix();

    /**
     * TODO
     *
     * @return
     */
    MetaNode.@NonNull Builder forMeta();

    /**
     * TODO
     *
     * @return
     */
    WeightNode.@NonNull Builder forWeight();

    /**
     * TODO
     *
     * @return
     */
    DisplayNameNode.@NonNull Builder forDisplayName();

}
