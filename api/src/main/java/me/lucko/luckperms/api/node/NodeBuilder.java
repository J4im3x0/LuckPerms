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

import com.google.common.base.Preconditions;

import me.lucko.luckperms.api.context.ContextSet;
import me.lucko.luckperms.api.node.metadata.NodeMetadata;
import me.lucko.luckperms.api.node.metadata.NodeMetadataKey;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @param <N>
 * @param <B>
 */
public interface NodeBuilder<N extends ScopedNode<N, B>, B extends NodeBuilder<N, B>> {

    /**
     * Sets the value of the node.
     *
     * @param value the value
     * @return the builder
     * @see Node#getValue()
     */
    @NonNull B value(boolean value);

    /**
     * Sets the value of negated for the node.
     *
     * @param negated the value
     * @return the builder
     * @see Node#isNegated()
     */
    @NonNull B negated(boolean negated);

    /**
     * Sets the time when the node should expire.
     *
     * <p>The parameter passed to this method must be the unix timestamp
     * (in seconds) when the node should expire.</p>
     *
     * @param expiryUnixTimestamp the expiry timestamp (unix seconds)
     * @return the builder
     * @see Node#getExpiry()
     */
    @NonNull B expiry(long expiryUnixTimestamp);

    /**
     * Sets the time when the node should expire.
     *
     * <p>The expiry timestamp is calculated relative to the current
     * system time.</p>
     *
     * @param duration how long the node should be added for
     * @param unit     the unit <code>duration</code> is measured in
     * @return the builder
     */
    default @NonNull B expiry(long duration, TimeUnit unit) {
        Preconditions.checkArgument(duration > 0, "duration must be positive");
        long seconds = Objects.requireNonNull(unit, "unit").toSeconds(duration);
        long timeNow = System.currentTimeMillis() / 1000L;
        return expiry(timeNow + seconds);
    }

    /**
     * Marks that the node being built should never expire.
     *
     * @return the builder
     */
    @NonNull B clearExpiry();

    /**
     * Sets the extra contexts for the node.
     *
     * @param contextSet a context set
     * @return the builder
     * @see ContextSet
     * @see Node#getContexts()
     */
    @NonNull B context(@NonNull ContextSet contextSet);

    /**
     * Appends an extra context onto the node.
     *
     * @param key   the context key
     * @param value the context value
     * @return the builder
     * @see ContextSet
     * @see Node#getContexts()
     */
    @NonNull B withContext(@NonNull String key, @NonNull String value);

    /**
     * Appends extra contexts onto the node.
     *
     * @param contextSet a context set
     * @return the builder
     * @see ContextSet
     * @see Node#getContexts()
     * @since 4.2
     */
    @NonNull B withContext(@NonNull ContextSet contextSet);

    /**
     * TODO
     *
     * @param key
     * @param metadata
     * @param <T>
     * @return
     */
    @NonNull <T extends NodeMetadata> B withMetadata(@NonNull NodeMetadataKey<T> key, @Nullable T metadata);

    /**
     * Creates a {@link Node} instance from the builder.
     *
     * @return a new node instance
     */
    @NonNull N build();
}
