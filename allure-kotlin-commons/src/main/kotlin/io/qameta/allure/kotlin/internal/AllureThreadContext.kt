package io.qameta.allure.kotlin.internal

import java.util.*

/**
 * Storage that stores information about not finished tests and steps.
 *
 * Handles cross-thread scenarios (e.g. Robolectric) where the test case is started
 * on one thread but the test body runs on another. The active root UUID is tracked
 * in a shared volatile field, and the per-thread step stack falls back to it
 * when the thread-local context is empty or stale.
 */
class AllureThreadContext {
    private val context = Context()
    @Volatile
    private var activeRoot: String? = null

    /**
     * Returns last (most recent) uuid — current step, or test case if no steps.
     * Falls back to the active root when the thread-local context is empty or stale.
     */
    val current: String?
        get() {
            val steps = context.get()
            if (steps.isNotEmpty()) {
                if (steps.last() == activeRoot) {
                    return steps.first()
                }
                context.remove()
            }
            return activeRoot
        }

    /**
     * Returns first (oldest) uuid — the root test case.
     * Falls back to the active root when the thread-local context is empty or stale.
     */
    val root: String?
        get() {
            val steps = context.get()
            if (steps.isNotEmpty()) {
                if (steps.last() == activeRoot) {
                    return steps.last()
                }
                context.remove()
            }
            return activeRoot
        }

    /**
     * Registers a root context (test case) and initializes the thread-local stack.
     */
    fun startRoot(uuid: String) {
        activeRoot = uuid
        context.remove()
        context.get().push(uuid)
    }

    /**
     * Unregisters a root context (test case) and clears the thread-local stack.
     */
    fun stopRoot(uuid: String) {
        if (activeRoot == uuid) {
            activeRoot = null
        }
        context.remove()
    }

    /**
     * Adds new uuid (step) to the current thread's stack.
     * If the stack is empty or stale (cross-thread scenario), injects the active root first.
     */
    fun start(uuid: String) {
        val steps = context.get()
        val root = activeRoot
        if (steps.isEmpty() || steps.last() != root) {
            steps.clear()
            root?.let { steps.push(it) }
        }
        steps.push(uuid)
    }

    /**
     * Removes latest added uuid. Ignores empty context.
     *
     * @return removed uuid.
     */
    fun stop(): String? {
        val uuids: LinkedList<String> = context.get()
        return if (!uuids.isEmpty()) uuids.pop() else null
    }

    /**
     * Removes all the data stored for current thread.
     */
    fun clear() {
        context.remove()
    }

    /**
     * Thread local context that stores information about not finished tests and steps.
     */
    private class Context : InheritableThreadLocal<LinkedList<String>>() {
        public override fun initialValue(): LinkedList<String> = LinkedList()

        override fun childValue(parentStepContext: LinkedList<String>): LinkedList<String> =
            LinkedList(parentStepContext)
    }
}