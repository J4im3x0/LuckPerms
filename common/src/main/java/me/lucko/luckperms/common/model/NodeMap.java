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

package me.lucko.luckperms.common.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

import me.lucko.luckperms.api.NodeEqualityPredicate;
import me.lucko.luckperms.api.context.ContextSet;
import me.lucko.luckperms.api.context.ImmutableContextSet;
import me.lucko.luckperms.api.node.Node;
import me.lucko.luckperms.api.node.metadata.types.InheritedFromMetadata;
import me.lucko.luckperms.api.node.types.InheritanceNode;
import me.lucko.luckperms.api.query.QueryOptions;
import me.lucko.luckperms.common.cache.Cache;
import me.lucko.luckperms.common.context.ContextSetComparator;
import me.lucko.luckperms.common.node.comparator.NodeComparator;
import me.lucko.luckperms.common.node.comparator.NodeWithContextComparator;
import me.lucko.luckperms.common.node.model.InheritedFrom;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A map of nodes held by a {@link PermissionHolder}.
 *
 * <p>Permissions are stored in Multimaps, with the context of the node being the key, and the actual Node object being
 * the value. The keys (context sets) are ordered according to their weight {@link ContextSetComparator}, and the values
 * are ordered according to the priority of the node, according to {@link NodeComparator}.</p>
 *
 * <p>Each holder has two of these maps, one for enduring and transient nodes.</p>
 */
public final class NodeMap {
    private static final Function<ImmutableContextSet, SortedSet<Node>> VALUE_SET_SUPPLIER = k -> new ConcurrentSkipListSet<>(NodeComparator.reverse());
    private static final Function<ImmutableContextSet, SortedSet<InheritanceNode>> INHERITANCE_VALUE_SET_SUPPLIER = k -> new ConcurrentSkipListSet<>(NodeComparator.reverse());
    /**
     * The holder which this map is for
     */
    private final PermissionHolder holder;

    /**
     * The backing data map.
     *
     * <p>Nodes are mapped by the result of {@link Node#getContexts()}, and keys are sorted by the weight of the
     * ContextSet. ContextSets are ordered first by the presence of a server key, then by the presence of a world
     * key, and finally by the overall size of the set. Nodes are ordered according to the priority rules
     * defined in {@link NodeComparator}.</p>
     */
    private final SortedMap<ImmutableContextSet, SortedSet<Node>> map = new ConcurrentSkipListMap<>(ContextSetComparator.reverse());

    /**
     * Copy of {@link #map} which only contains group nodes
     * @see InheritanceNode
     */
    private final SortedMap<ImmutableContextSet, SortedSet<InheritanceNode>> inheritanceMap = new ConcurrentSkipListMap<>(ContextSetComparator.reverse());

    /**
     * A cache which holds an immutable copy of the backing map
     */
    private final NodeMapCache cache = new NodeMapCache(this);

    NodeMap(PermissionHolder holder) {
        this.holder = holder;
    }

    public List<Node> asList() {
        List<Node> list = new ArrayList<>();
        copyTo(list);
        return list;
    }

    public LinkedHashSet<Node> asSet() {
        LinkedHashSet<Node> set = new LinkedHashSet<>();
        copyTo(set);
        return set;
    }

    public SortedSet<Node> asSortedSet() {
        SortedSet<Node> ret = new TreeSet<>(NodeWithContextComparator.reverse());
        copyTo(ret);
        return ret;
    }

    public void copyTo(Collection<? super Node> collection) {
        for (SortedSet<Node> nodes : this.map.values()) {
            collection.addAll(nodes);
        }
    }

    public void copyTo(Collection<? super Node> collection, QueryOptions filter) {
        for (Map.Entry<ImmutableContextSet, SortedSet<Node>> e : this.map.entrySet()) {
            if (filter.satisfies(e.getKey())) {
                collection.addAll(e.getValue());
            }
        }
    }

    public void copyGroupNodesTo(Collection<? super InheritanceNode> collection, QueryOptions filter) {
        for (Map.Entry<ImmutableContextSet, SortedSet<InheritanceNode>> e : this.inheritanceMap.entrySet()) {
            if (filter.satisfies(e.getKey())) {
                collection.addAll(e.getValue());
            }
        }
    }

    /**
     * Returns an immutable representation of the maps current state.
     *
     * @return an immutable copy
     */
    public ImmutableSetMultimap<ImmutableContextSet, Node> immutable() {
        return this.cache.get();
    }

    /**
     * Invalidates the cache
     */
    void invalidate() {
        this.cache.invalidate();
    }

    private Node localise(Node node) {
        Optional<InheritedFromMetadata> metadata = node.getMetadata(InheritedFromMetadata.KEY);
        if (metadata.map(InheritedFromMetadata::getLocation).equals(Optional.of(this.holder.getObjectName()))) {
            return node;
        }

        return node.toBuilder().withMetadata(InheritedFromMetadata.KEY, new InheritedFrom(this.holder.getObjectName())).build();
    }

    void add(Node node) {
        ImmutableContextSet context = node.getContexts();
        Node n = localise(node);

        SortedSet<Node> nodesInContext = this.map.computeIfAbsent(context, VALUE_SET_SUPPLIER);
        nodesInContext.removeIf(e -> e.equals(node, NodeEqualityPredicate.IGNORE_EXPIRY_TIME_AND_VALUE));
        nodesInContext.add(n);

        if (n instanceof InheritanceNode) {
            SortedSet<InheritanceNode> groupNodesInContext = this.inheritanceMap.computeIfAbsent(context, INHERITANCE_VALUE_SET_SUPPLIER);
            groupNodesInContext.removeIf(e -> e.equals(node, NodeEqualityPredicate.IGNORE_EXPIRY_TIME_AND_VALUE));
            if (n.getValue()) {
                groupNodesInContext.add((InheritanceNode) n);
            }
        }
    }

    void remove(Node node) {
        ImmutableContextSet context = node.getContexts();
        SortedSet<Node> nodesInContext = this.map.get(context);
        if (nodesInContext != null) {
            nodesInContext.removeIf(e -> e.equals(node, NodeEqualityPredicate.IGNORE_EXPIRY_TIME_AND_VALUE));
        }

        if (node instanceof InheritanceNode) {
            SortedSet<InheritanceNode> groupNodesInContext = this.inheritanceMap.get(context);
            if (groupNodesInContext != null) {
                groupNodesInContext.removeIf(e -> e.equals(node, NodeEqualityPredicate.IGNORE_EXPIRY_TIME_AND_VALUE));
            }
        }
    }

    private void removeExact(Node node) {
        ImmutableContextSet context = node.getContexts();
        SortedSet<Node> nodesInContext = this.map.get(context);
        if (nodesInContext != null) {
            //noinspection SuspiciousMethodCalls
            nodesInContext.remove(node);
        }

        if (node instanceof InheritanceNode && node.getValue()) {
            SortedSet<InheritanceNode> groupNodesInContext = this.inheritanceMap.get(context);
            if (groupNodesInContext != null) {
                //noinspection SuspiciousMethodCalls
                groupNodesInContext.remove(node);
            }
        }
    }

    void replace(Node node, Node previous) {
        removeExact(previous);
        add(node);
    }

    void clear() {
        this.map.clear();
        this.inheritanceMap.clear();
    }

    void clear(ContextSet contextSet) {
        ImmutableContextSet context = contextSet.makeImmutable();
        this.map.remove(context);
        this.inheritanceMap.remove(context);
    }

    void setContent(Collection<? extends Node> set) {
        this.map.clear();
        this.inheritanceMap.clear();
        for (Node n : set) {
            add(n);
        }
    }

    void setContent(Multimap<ImmutableContextSet, ? extends Node> multimap) {
        setContent(multimap.values());
    }

    boolean removeIf(Predicate<? super Node> predicate) {
        boolean ret = false;
        for (SortedSet<Node> valueSet : this.map.values()) {
            if (valueSet.removeIf(predicate)) {
                ret = true;
            }
        }
        for (SortedSet<InheritanceNode> valueSet : this.inheritanceMap.values()) {
            valueSet.removeIf(predicate);
        }
        return ret;
    }

    boolean removeIf(ContextSet contextSet, Predicate<? super Node> predicate) {
        ImmutableContextSet context = contextSet.makeImmutable();

        boolean ret = false;

        SortedSet<Node> nodesInContext = this.map.get(context);
        if (nodesInContext != null) {
            ret = nodesInContext.removeIf(predicate);
        }

        SortedSet<InheritanceNode> groupNodesInContext = this.inheritanceMap.get(context);
        if (groupNodesInContext != null) {
            groupNodesInContext.removeIf(predicate);
        }

        return ret;
    }

    boolean auditTemporaryNodes(@Nullable Set<? super Node> removed) {
        boolean work = false;

        for (SortedSet<Node> valueSet : this.map.values()) {
            Iterator<Node> it = valueSet.iterator();
            while (it.hasNext()) {
                Node entry = it.next();
                if (!entry.hasExpired()) {
                    continue;
                }

                // remove
                if (removed != null) {
                    removed.add(entry);
                }
                if (entry instanceof InheritanceNode && entry.getValue()) {
                    SortedSet<InheritanceNode> groupNodesInContext = this.inheritanceMap.get(entry.getContexts());
                    if (groupNodesInContext != null) {
                        groupNodesInContext.remove(entry);
                    }
                }
                it.remove();
                work = true;
            }
        }

        return work;
    }

    private static final class NodeMapCache extends Cache<ImmutableSetMultimap<ImmutableContextSet, Node>> {
        private static final Constructor<ImmutableSetMultimap> IMMUTABLE_SET_MULTIMAP_CONSTRUCTOR;
        static {
            try {
                IMMUTABLE_SET_MULTIMAP_CONSTRUCTOR = ImmutableSetMultimap.class.getDeclaredConstructor(ImmutableMap.class, int.class, Comparator.class);
                IMMUTABLE_SET_MULTIMAP_CONSTRUCTOR.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private final NodeMap handle;

        private NodeMapCache(NodeMap handle) {
            this.handle = handle;
        }

        @Override
        protected @NonNull ImmutableSetMultimap<ImmutableContextSet, Node> supply() {
            ImmutableMap.Builder<ImmutableContextSet, ImmutableSet<Node>> builder = ImmutableMap.builder();
            int size = 0;

            for (Map.Entry<ImmutableContextSet, SortedSet<Node>> entry : this.handle.map.entrySet()) {
                ImmutableContextSet key = entry.getKey();
                ImmutableSet<Node> values = ImmutableSet.copyOf(entry.getValue());
                if (!values.isEmpty()) {
                    builder.put(key, values);
                    size += values.size();
                }
            }

            try {
                //noinspection unchecked
                return IMMUTABLE_SET_MULTIMAP_CONSTRUCTOR.newInstance(builder.build(), size, null);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
