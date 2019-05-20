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

package me.lucko.luckperms.api.node.types;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.node.Node;
import me.lucko.luckperms.api.node.NodeBuilder;
import me.lucko.luckperms.api.node.ScopedNode;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A sub-type of {@link Node} used to store meta assignments.
 *
 * @since 4.2
 */
public interface MetaNode extends ScopedNode<MetaNode, MetaNode.Builder> {

    /**
     * Gets the meta key.
     *
     * @return the meta key
     */
    @NonNull String getMetaKey();

    /**
     * Gets the meta value.
     *
     * @return the meta value
     */
    @NonNull String getMetaValue();

    /**
     * TODO
     *
     * @return
     */
    static @NonNull Builder builder() {
        return LuckPerms.getApi().getNodeBuilderRegistry().forMeta();
    }

    /**
     * TODO
     *
     * @param key
     * @param value
     * @return
     */
    static @NonNull Builder builder(@NonNull String key, @NonNull String value) {
        return builder().key(key).value(value);
    }

    /**
     * TODO
     */
    interface Builder extends NodeBuilder<MetaNode, Builder> {

        /**
         * TODO
         *
         * @param key
         * @return
         */
        @NonNull Builder key(@NonNull String key);

        /**
         * TODO
         *
         * @param value
         * @return
         */
        @NonNull Builder value(@NonNull String value);

    }

}
