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

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A sub-type of {@link Node} used to store regex permissions.
 *
 * @since 4.3
 */
public interface RegexPermissionNode extends ScopedNode<RegexPermissionNode, RegexPermissionNode.Builder> {

    /**
     * Gets the non-compiled pattern string.
     *
     * @return the pattern string
     */
    @NonNull String getPatternString();

    /**
     * Gets the pattern for the regex node.
     *
     * <p>Will return an empty optional if the Pattern could not be parsed.</p>
     *
     * @return the pattern
     */
    @NonNull Optional<Pattern> getPattern();

    /**
     * TODO
     *
     * @return
     */
    static @NonNull Builder builder() {
        return LuckPerms.getApi().getNodeBuilderRegistry().forRegexPermission();
    }

    /**
     * TODO
     *
     * @param pattern
     * @return
     */
    static @NonNull Builder builder(@NonNull String pattern) {
        return builder().pattern(pattern);
    }

    /**
     * TODO
     *
     * @param pattern
     * @return
     */
    static @NonNull Builder builder(@NonNull Pattern pattern) {
        return builder().pattern(pattern);
    }

    /**
     * TODO
     */
    interface Builder extends NodeBuilder<RegexPermissionNode, Builder> {

        /**
         * TODO
         *
         * @param pattern
         * @return
         */
        @NonNull Builder pattern(@NonNull String pattern);

        /**
         * TODO
         *
         * @param pattern
         * @return
         */
        @NonNull Builder pattern(@NonNull Pattern pattern);

    }

}
