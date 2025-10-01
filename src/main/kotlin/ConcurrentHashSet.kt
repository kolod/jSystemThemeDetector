/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.github.kolod.jthemedetecor.util

import java.util.concurrent.ConcurrentHashMap

class ConcurrentHashSet<E : Any> : MutableSet<E> {
    private val set = ConcurrentHashMap.newKeySet<E>()

    override val size: Int
        get() = set.size

    override fun isEmpty(): Boolean = set.isEmpty()
    override fun contains(element: E): Boolean = set.contains(element)
    override fun containsAll(elements: Collection<E>): Boolean = set.containsAll(elements)
    override fun iterator(): MutableIterator<E> = set.iterator()
    override fun add(element: E): Boolean = set.add(element)
    override fun addAll(elements: Collection<E>): Boolean = set.addAll(elements)
    override fun remove(element: E): Boolean = set.remove(element)
    override fun removeAll(elements: Collection<E>): Boolean = set.removeAll(elements)
    override fun retainAll(elements: Collection<E>): Boolean = set.retainAll(elements)
    override fun clear() = set.clear()
}
