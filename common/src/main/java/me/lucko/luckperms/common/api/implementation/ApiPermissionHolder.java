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

package me.lucko.luckperms.common.api.implementation;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;

import me.lucko.luckperms.api.DataMutateResult;
import me.lucko.luckperms.api.NodeEqualityPredicate;
import me.lucko.luckperms.api.TemporaryDataMutateResult;
import me.lucko.luckperms.api.TemporaryMergeBehaviour;
import me.lucko.luckperms.api.Tristate;
import me.lucko.luckperms.api.caching.CachedData;
import me.lucko.luckperms.api.context.ContextSet;
import me.lucko.luckperms.api.context.ImmutableContextSet;
import me.lucko.luckperms.api.node.Node;
import me.lucko.luckperms.api.node.types.InheritanceNode;
import me.lucko.luckperms.api.query.QueryOptions;
import me.lucko.luckperms.common.model.NodeMapType;
import me.lucko.luckperms.common.model.PermissionHolder;
import me.lucko.luckperms.common.node.comparator.NodeWithContextComparator;
import me.lucko.luckperms.common.node.utils.MetaType;
import me.lucko.luckperms.common.node.utils.NodeTools;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class ApiPermissionHolder implements me.lucko.luckperms.api.PermissionHolder {
    private final PermissionHolder handle;

    private final Enduring enduringData;
    private final Transient transientData;

    ApiPermissionHolder(PermissionHolder handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.enduringData = new Enduring();
        this.transientData = new Transient();
    }

    PermissionHolder getHandle() {
        return this.handle;
    }

    @Override
    public @NonNull String getObjectName() {
        return this.handle.getObjectName();
    }

    @Override
    public @NonNull String getFriendlyName() {
        return this.handle.getPlainDisplayName();
    }

    @Override
    public @NonNull CachedData getCachedData() {
        return this.handle.getCachedData();
    }

    @Override
    public @NonNull CompletableFuture<Void> refreshCachedData() {
        return CompletableFuture.runAsync(() -> this.handle.getCachedData().invalidate());
    }

    @Override
    public Data getData(@NonNull DataType dataType) {
        switch (dataType) {
            case ENDURING:
                return this.enduringData;
            case TRANSIENT:
                return this.transientData;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public @NonNull Data enduringData() {
        return this.enduringData;
    }

    @Override
    public @NonNull Data transientData() {
        return this.transientData;
    }

    @Override
    public @NonNull List<Node> getNodes() {
        return this.handle.getOwnNodes();
    }

    @Override
    public @NonNull SortedSet<Node> getDistinctNodes() {
        return this.handle.getOwnNodesSorted();
    }

    @Override
    public @NonNull List<Node> resolveInheritedNodes(@NonNull QueryOptions queryOptions) {
        return this.handle.resolveInheritances(queryOptions);
    }

    @Override
    public @NonNull SortedSet<Node> resolveDistinctInheritedNodes(@NonNull QueryOptions queryOptions) {
        List<Node> entries = this.handle.getAllEntries(queryOptions);

        NodeTools.removeSamePermission(entries.iterator());
        SortedSet<Node> ret = new TreeSet<>(NodeWithContextComparator.reverse());
        ret.addAll(entries);

        return ImmutableSortedSet.copyOfSorted(ret);
    }

    @Override
    public void auditTemporaryPermissions() {
        this.handle.auditTemporaryPermissions();
    }

    @Override
    public @NonNull Tristate inheritsNode(@NonNull Node node, @NonNull NodeEqualityPredicate equalityPredicate) {
        return this.handle.inheritsPermission(node, equalityPredicate);
    }

    private final class Enduring implements Data {
        @Override
        public @NonNull ImmutableSetMultimap<ImmutableContextSet, Node> getNodes() {
            return ApiPermissionHolder.this.handle.enduringData().immutable();
        }

        @Override
        public @NonNull Set<Node> getDistinctNodes() {
            return ApiPermissionHolder.this.handle.enduringData().asSet();
        }

        @Override
        public @NonNull Tristate hasNode(@NonNull Node node, @NonNull NodeEqualityPredicate equalityPredicate) {
            return ApiPermissionHolder.this.handle.hasPermission(NodeMapType.ENDURING, node, equalityPredicate);
        }

        @Override
        public @NonNull DataMutateResult addNode(@NonNull Node node) {
            return ApiPermissionHolder.this.handle.setPermission(node);
        }

        @Override
        public @NonNull TemporaryDataMutateResult addNode(@NonNull Node node, @NonNull TemporaryMergeBehaviour temporaryMergeBehaviour) {
            return ApiPermissionHolder.this.handle.setPermission(node, temporaryMergeBehaviour);
        }

        @Override
        public @NonNull DataMutateResult removeNode(@NonNull Node node) {
            return ApiPermissionHolder.this.handle.unsetPermission(node);
        }

        @Override
        public void clearMatching(@NonNull Predicate<Node> test) {
            ApiPermissionHolder.this.handle.removeIfEnduring(test);
        }

        @Override
        public void clearNodes() {
            ApiPermissionHolder.this.handle.clearEnduringNodes();
        }

        @Override
        public void clearNodes(@NonNull ContextSet contextSet) {
            ApiPermissionHolder.this.handle.clearEnduringNodes(contextSet);
        }

        @Override
        public void clearParents() {
            ApiPermissionHolder.this.handle.clearEnduringParents(true);
        }

        @Override
        public void clearParents(@NonNull ContextSet contextSet) {
            ApiPermissionHolder.this.handle.clearEnduringParents(contextSet, true);
        }

        @Override
        public void clearMeta() {
            ApiPermissionHolder.this.handle.removeIfEnduring(MetaType.ANY::matches);
        }

        @Override
        public void clearMeta(@NonNull ContextSet contextSet) {
            ApiPermissionHolder.this.handle.removeIfEnduring(contextSet, MetaType.ANY::matches);
        }
    }

    private final class Transient implements Data {
        @Override
        public @NonNull ImmutableSetMultimap<ImmutableContextSet, Node> getNodes() {
            return ApiPermissionHolder.this.handle.transientData().immutable();
        }

        @Override
        public @NonNull Set<Node> getDistinctNodes() {
            return ApiPermissionHolder.this.handle.transientData().asSet();
        }

        @Override
        public @NonNull Tristate hasNode(@NonNull Node node, @NonNull NodeEqualityPredicate equalityPredicate) {
            return ApiPermissionHolder.this.handle.hasPermission(NodeMapType.TRANSIENT, node, equalityPredicate);
        }

        @Override
        public @NonNull DataMutateResult addNode(@NonNull Node node) {
            return ApiPermissionHolder.this.handle.setTransientPermission(node);
        }

        @Override
        public @NonNull TemporaryDataMutateResult addNode(@NonNull Node node, @NonNull TemporaryMergeBehaviour temporaryMergeBehaviour) {
            return ApiPermissionHolder.this.handle.setTransientPermission(node, temporaryMergeBehaviour);
        }

        @Override
        public @NonNull DataMutateResult removeNode(@NonNull Node node) {
            return ApiPermissionHolder.this.handle.unsetTransientPermission(node);
        }

        @Override
        public void clearMatching(@NonNull Predicate<Node> test) {
            ApiPermissionHolder.this.handle.removeIfTransient(test);
        }

        @Override
        public void clearNodes() {
            ApiPermissionHolder.this.handle.clearTransientNodes();
        }

        @Override
        public void clearNodes(@NonNull ContextSet contextSet) {
            ApiPermissionHolder.this.handle.clearTransientNodes(contextSet);
        }

        @Override
        public void clearParents() {
            ApiPermissionHolder.this.handle.removeIfTransient(n -> n instanceof InheritanceNode);
        }

        @Override
        public void clearParents(@NonNull ContextSet contextSet) {
            ApiPermissionHolder.this.handle.removeIfTransient(contextSet, n -> n instanceof InheritanceNode);
        }

        @Override
        public void clearMeta() {
            ApiPermissionHolder.this.handle.removeIfTransient(MetaType.ANY::matches);
        }

        @Override
        public void clearMeta(@NonNull ContextSet contextSet) {
            ApiPermissionHolder.this.handle.removeIfTransient(contextSet, MetaType.ANY::matches);
        }
    }

}
