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

package me.lucko.luckperms.api.context;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;

final class MutableContextSetImpl extends AbstractContextSet implements MutableContextSet {

    static MutableContextSet fromSet(ContextSet contextSet) {
        Objects.requireNonNull(contextSet, "contextSet");

        if (contextSet instanceof ImmutableContextSet) {
            SetMultimap<String, String> map = ((ImmutableContextSetImpl) contextSet).backing();
            return new MutableContextSetImpl(map);
        } else if (contextSet instanceof MutableContextSetImpl) {
            return contextSet.mutableCopy();
        } else {
            MutableContextSet set = new MutableContextSetImpl();
            set.addAll(contextSet);
            return set;
        }
    }

    private final SetMultimap<String, String> map;

    MutableContextSetImpl() {
        this.map = Multimaps.synchronizedSetMultimap(HashMultimap.create());
    }

    private MutableContextSetImpl(SetMultimap<String, String> other) {
        this.map = Multimaps.synchronizedSetMultimap(HashMultimap.create(other));
    }

    @Override
    protected SetMultimap<String, String> backing() {
        return this.map;
    }

    @Override
    protected void copyTo(SetMultimap<String, String> other) {
        synchronized (this.map) {
            other.putAll(this.map);
        }
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public @NonNull ImmutableContextSet makeImmutable() {
        // if the map is empty, don't create a new instance
        if (this.map.isEmpty()) {
            return ImmutableContextSet.empty();
        }
        synchronized (this.map) {
            return new ImmutableContextSetImpl(ImmutableSetMultimap.copyOf(this.map));
        }
    }

    @Override
    public @NonNull MutableContextSetImpl mutableCopy() {
        synchronized (this.map) {
            return new MutableContextSetImpl(this.map);
        }
    }

    @Override
    public @NonNull Set<Map.Entry<String, String>> toSet() {
        synchronized (this.map) {
            // map.entries() returns immutable Map.Entry instances, so we can just call copyOf
            return ImmutableSet.copyOf(this.map.entries());
        }
    }

    @Deprecated
    @Override
    public @NonNull Map<String, String> toMap() {
        ImmutableMap.Builder<String, String> m = ImmutableMap.builder();
        synchronized (this.map) {
            for (Map.Entry<String, String> e : this.map.entries()) {
                m.put(e.getKey(), e.getValue());
            }
        }
        return m.build();
    }

    @Override
    public @NonNull Multimap<String, String> toMultimap() {
        synchronized (this.map) {
            return ImmutableSetMultimap.copyOf(this.map);
        }
    }

    @Override
    public @NonNull Iterator<Map.Entry<String, String>> iterator() {
        return toSet().iterator();
    }

    @Override
    public Spliterator<Map.Entry<String, String>> spliterator() {
        return toSet().spliterator();
    }

    @Override
    public void add(@NonNull String key, @NonNull String value) {
        this.map.put(sanitizeKey(key), sanitizeValue(value));
    }

    @Override
    public void addAll(@NonNull ContextSet contextSet) {
        Objects.requireNonNull(contextSet, "contextSet");
        if (contextSet instanceof AbstractContextSet) {
            AbstractContextSet other = ((AbstractContextSet) contextSet);
            synchronized (this.map) {
                other.copyTo(this.map);
            }
        } else {
            addAll(contextSet.toMultimap());
        }
    }

    @Override
    public void remove(@NonNull String key, @NonNull String value) {
        this.map.remove(sanitizeKey(key), sanitizeValue(value));
    }

    @Override
    public void removeAll(@NonNull String key) {
        this.map.removeAll(sanitizeKey(key));
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ContextSet)) return false;
        final ContextSet that = (ContextSet) o;

        final Multimap<String, String> thatBacking;
        if (that instanceof AbstractContextSet) {
            thatBacking = ((AbstractContextSet) that).backing();
        } else {
            thatBacking = that.toMultimap();
        }

        return backing().equals(thatBacking);
    }

    @Override
    public String toString() {
        return "MutableContextSet(contexts=" + this.map + ")";
    }
}
