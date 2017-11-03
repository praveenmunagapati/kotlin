/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.inline.clean

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.SpecialFunction
import org.jetbrains.kotlin.js.backend.ast.metadata.isSuspend
import org.jetbrains.kotlin.js.backend.ast.metadata.specialFunction
import org.jetbrains.kotlin.js.coroutine.isStateMachineResult

class SuspendExpressionElimination(private val body: JsBlock) {
    fun apply(): Boolean {
        var changed = false

        object : JsVisitorWithContextImpl() {
            override fun endVisit(x: JsExpressionStatement, ctx: JsContext<*>) {
                x.expression.extractSuspend()?.let { suspendExpr ->
                    if (suspendExpr.isSpecialStateMachineResult()) {
                        ctx.removeMe()
                        changed = true
                    }
                }
            }
        }.accept(body)

        return changed
    }

    private fun JsExpression.extractSuspend(): JsExpression? {
        if (isSuspend) return this
        if (this !is JsInvocation) return null

        return if (isSpecialFunction(SpecialFunction.SUSPEND_CALL)) return arguments[0] else null
    }

    private fun JsExpression.isSpecialStateMachineResult(): Boolean {
        if (isStateMachineResult()) return true
        if (this !is JsInvocation) return false

        if (!isSpecialFunction(SpecialFunction.COROUTINE_RESULT)) return false

        val arg = arguments.getOrNull(0) as? JsInvocation ?: return false
        return arg.isSpecialFunction(SpecialFunction.COROUTINE_RECEIVER) && arg.arguments.isEmpty()
    }

    private fun JsInvocation.isSpecialFunction(f: SpecialFunction): Boolean {
        val name = (qualifier as? JsNameRef)?.name ?: return false
        return name.specialFunction == f
    }
}
