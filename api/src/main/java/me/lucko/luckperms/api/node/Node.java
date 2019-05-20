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

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.NodeEqualityPredicate;
import me.lucko.luckperms.api.context.ContextSet;
import me.lucko.luckperms.api.context.ImmutableContextSet;
import me.lucko.luckperms.api.node.metadata.NodeMetadata;
import me.lucko.luckperms.api.node.metadata.NodeMetadataKey;
import me.lucko.luckperms.api.node.types.DisplayNameNode;
import me.lucko.luckperms.api.node.types.InheritanceNode;
import me.lucko.luckperms.api.node.types.MetaNode;
import me.lucko.luckperms.api.node.types.PermissionNode;
import me.lucko.luckperms.api.node.types.PrefixNode;
import me.lucko.luckperms.api.node.types.RegexPermissionNode;
import me.lucko.luckperms.api.node.types.SuffixNode;
import me.lucko.luckperms.api.node.types.WeightNode;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a LuckPerms "node".
 *
 * <p>The {@link Node} class encapsulates more than just permission assignments.
 * Nodes are used to store data about inherited groups, as well as assigned
 * prefixes, suffixes and meta values.</p>
 *
 * <p>Combining these various states into one object (a "node") means that a
 * holder only has to have one type of data set (a set of nodes) in order to
 * take on various properties.</p>
 *
 * <p>It is recommended that users of the API make use of {@link Stream}s
 * to manipulate data and obtain the required information.</p>
 *
 * <p>This interface provides a number of methods to read the attributes of the
 * node, as well as methods to query and extract additional state and properties
 * from these settings.</p>
 *
 * <p>Nodes have the following attributes:</p>
 * <p></p>
 * <ul>
 * <li>{@link #getKey() key} - the key of the node</li>
 * <li>{@link #getValue() value} - the value of the node (false for negated)</li>
 * <li>{@link #getContexts() context} - the contexts required for this node to apply </li>
 * <li>{@link #getExpiry() expiry} - the time when this node should expire</li>
 * </ul>
 *
 * <p>There are a number of node types, all of which extend from this class:</p>
 * <p></p>
 * <ul>
 * <li>{@link PermissionNode} - represents an assigned permission</li>
 * <li>{@link RegexPermissionNode} - represents an assigned regex permission</li>
 * <li>{@link InheritanceNode} - an "inheritance node" marks that the holder should inherit data from another group</li>
 * <li>{@link PrefixNode} - represents an assigned prefix</li>
 * <li>{@link SuffixNode} - represents an assigned suffix</li>
 * <li>{@link MetaNode} - represents an assigned meta option</li>
 * <li>{@link WeightNode} - marks the weight of the object holding this node</li>
 * <li>{@link DisplayNameNode} - marks the display name of the object holding this node</li>
 * </ul>
 *
 * <p>The core node state must be immutable in all implementations.</p>
 *
 * @since 2.6
 */
public interface Node {

    /**
     * TODO
     *
     * @param key
     * @return
     * @since 5.0
     */
    static @NonNull NodeBuilder builder(@NonNull String key) {
        return LuckPerms.getApi().getNodeBuilderRegistry().forKey(key);
    }

    /**
     * TODO
     *
     * @return
     * @since 5.0
     */
    @NonNull String getKey();

    /**
     * Gets the value of the node.
     *
     * <p>A negated setting would result in a value of <code>false</code>.</p>
     *
     * @return the nodes value
     */
    boolean getValue();

    /**
     * Gets if the node is negated.
     *
     * <p>This is the inverse of the {@link #getValue() value}.</p>
     *
     * @return true if the node is negated
     */
    default boolean isNegated() {
        return !getValue();
    }

    /**
     * Gets if this node applies globally, and therefore has no specific context.
     *
     * @return true if this node applies globally, and has no specific context
     * @since 3.1
     */
    boolean appliesGlobally();

    /**
     * Gets if this node should apply in the given context
     *
     * @param contextSet the context set
     * @return true if the node should apply
     * @since 2.13
     */
    boolean shouldApplyWithContext(@NonNull ContextSet contextSet);

    /**
     * Resolves any shorthand parts of this node and returns the full list of
     * resolved nodes.
     *
     * <p>The list will not contain the exact permission itself.</p>
     *
     * @return a list of full nodes
     */
    @NonNull List<String> resolveShorthand();

    /**
     * Gets if this node is assigned temporarily.
     *
     * @return true if this node will expire in the future
     */
    boolean hasExpiry();

    /**
     * Gets the time when this node will expire.
     *
     * @return the {@link Instant} when this node will expire, or null if it
     * doesn't have an expiry time
     */
    @Nullable Instant getExpiry() throws IllegalStateException;

    /**
     * Gets if the node has expired.
     *
     * <p>This returns false if the node is not temporary.</p>
     *
     * @return true if this node has expired
     */
    boolean hasExpired();

    /**
     * Gets the contexts required for this node to apply.
     *
     * @return the contexts required for this node to apply
     */
    @NonNull ImmutableContextSet getContexts();

    /**
     * Gets the metadata corresponding to the given <code>key</code>, if present.
     *
     * @param key the key
     * @param <T> the {@link NodeMetadata} type
     * @return the data, if present
     * @since 5.0
     */
    <T extends NodeMetadata> Optional<T> getMetadata(NodeMetadataKey<T> key);

    /**
     * Gets the metadata corresponding to the given <code>key</code>, throwing an exception
     * if no data is present.
     *
     * @param key the key
     * @param <T> the {@link NodeMetadata} type
     * @return the data
     * @throws IllegalStateException if data isn't present
     * @since 5.0
     */
    default <T extends NodeMetadata> T metadata(NodeMetadataKey<T> key) throws IllegalStateException {
        return getMetadata(key).orElseThrow(() -> new IllegalStateException("Node '" + getKey() + "' does not have '" + key.getTypeName() + "' attached."));
    }

    /**
     * Gets if this Node is equal to another node.
     *
     * @param obj the other node
     * @return true if this node is equal to the other provided
     * @see NodeEqualityPredicate#EXACT
     */
    @Override
    boolean equals(Object obj);

    /**
     * Gets if this Node is equal to another node as defined by the given
     * {@link NodeEqualityPredicate}.
     *
     * @param other             the other node
     * @param equalityPredicate the predicate
     * @return true if this node is considered equal
     * @since 4.1
     */
    boolean equals(@NonNull Node other, @NonNull NodeEqualityPredicate equalityPredicate);

    /**
     * TODO
     *
     * @return
     * @since 5.0
     */
    @NonNull NodeBuilder<?, ?> toBuilder();

}
