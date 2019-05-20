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

package me.lucko.luckperms.api.query;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.context.ContextSet;
import me.lucko.luckperms.api.context.ImmutableContextSet;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * TODO
 */
public interface QueryOptions {

    /**
     * TODO
     *
     * @param mode
     * @return
     */
    static @NonNull Builder builder(@NonNull QueryMode mode) {
        return LuckPerms.getApi().getContextManager().queryOptionsBuilder(mode);
    }

    /**
     * TODO
     *
     * @param context
     * @param flags
     * @return
     */
    static @NonNull QueryOptions contextual(@NonNull ContextSet context, @NonNull Set<Flag> flags) {
        return builder(QueryMode.CONTEXTUAL).context(context).flags(flags).build();
    }

    /**
     * TODO
     *
     * @param flags
     * @return
     */
    static @NonNull QueryOptions nonContextual(@NonNull Set<Flag> flags) {
        return builder(QueryMode.NON_CONTEXTUAL).flags(flags).build();
    }

    /**
     * TODO
     *
     * @return
     */
    static @NonNull QueryOptions nonContextual() {
        return DefaultQueryOptions.NON_CONTEXTUAL;
    }

    /**
     * TODO
     *
     * @return
     */
    static @NonNull QueryOptions defaultContextualOptions() {
        return DefaultQueryOptions.CONTEXTUAL;
    }

    /**
     * TODO
     *
     * @return
     */
    @NonNull QueryMode mode();

    /**
     * TODO
     *
     * @return
     */
    @Nullable ImmutableContextSet context();

    /**
     * TODO
     *
     * @param flag
     * @return
     */
    boolean flag(@NonNull Flag flag);

    /**
     * TODO
     *
     * @return
     */
    @NonNull Set<Flag> flags();

    /**
     * TODO
     *
     * @param key
     * @param <O>
     * @return
     */
    <O> @NonNull Optional<O> option(@NonNull OptionKey<O> key);

    /**
     * TODO
     *
     * @return
     */
    @NonNull Map<OptionKey<?>, Object> options();

    /**
     * Gets whether this {@link QueryOptions} satisfies the given required
     * {@link ContextSet context}.
     *
     * @param contextSet the contexts
     * @return the result
     */
    boolean satisfies(@NonNull ContextSet contextSet);

    /**
     * TODO
     *
     * @return
     */
    @NonNull Builder toBuilder();

    /**
     * TODO
     */
    interface Builder {

        /**
         * TODO
         *
         * @param context
         * @return
         */
        @NonNull Builder context(@NonNull ContextSet context);

        /**
         * TODO
         *
         * @param flag
         * @return
         */
        @NonNull Builder flag(@NonNull Flag flag);

        /**
         * TODO
         *
         * @param flag
         * @param value
         * @return
         */
        @NonNull Builder flag(@NonNull Flag flag, boolean value);

        /**
         * TODO
         *
         * @param flags
         * @return
         */
        @NonNull Builder flags(@NonNull Set<Flag> flags);

        /**
         * TODO
         *
         * @param key
         * @param value
         * @param <O>
         * @return
         */
        <O> @NonNull Builder option(@NonNull OptionKey<O> key, @Nullable O value);

        /**
         * TODO
         *
         * @return
         */
        @NonNull QueryOptions build();

    }

}
