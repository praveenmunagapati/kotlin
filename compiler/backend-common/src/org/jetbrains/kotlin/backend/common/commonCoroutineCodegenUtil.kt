/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.backend.common

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorEquivalenceForOverrides
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeConstructor
import org.jetbrains.kotlin.types.checker.KotlinTypeChecker
import org.jetbrains.kotlin.types.checker.KotlinTypeCheckerImpl
import org.jetbrains.kotlin.types.isError
import org.jetbrains.kotlin.types.typeUtil.supertypes
import java.util.ArrayList
import java.util.HashMap

val SUSPEND_COROUTINE_OR_RETURN_NAME = Name.identifier("suspendCoroutineOrReturn")
val COROUTINE_SUSPENDED_NAME = Name.identifier("COROUTINE_SUSPENDED")

val COROUTINES_INTRINSICS_PACKAGE_FQ_NAME = DescriptorUtils.COROUTINES_PACKAGE_FQ_NAME.child(Name.identifier("intrinsics"))
val COROUTINE_CONTEXT_FQ_NAME = COROUTINES_INTRINSICS_PACKAGE_FQ_NAME.child(Name.identifier("coroutineContext"))

val SUSPEND_COROUTINE_UNINTERCEPTED_OR_RETURN_NAME = Name.identifier("suspendCoroutineUninterceptedOrReturn")
val SUSPEND_COROUTINE_UNINTERCEPTED_OR_RETURN_FQ_NAME = COROUTINES_INTRINSICS_PACKAGE_FQ_NAME.child(SUSPEND_COROUTINE_UNINTERCEPTED_OR_RETURN_NAME)

fun FunctionDescriptor.isBuiltInSuspendCoroutineOrReturn(): Boolean {
    if (name != SUSPEND_COROUTINE_OR_RETURN_NAME) return false

    val originalDeclaration = getBuiltInSuspendCoroutineOrReturn() ?: return false

    return DescriptorEquivalenceForOverrides.areEquivalent(
            originalDeclaration, this
    )
}

fun FunctionDescriptor.getBuiltInSuspendCoroutineOrReturn() =
        module.getPackage(COROUTINES_INTRINSICS_PACKAGE_FQ_NAME).memberScope
                .getContributedFunctions(SUSPEND_COROUTINE_OR_RETURN_NAME, NoLookupLocation.FROM_BACKEND)
                .singleOrNull()

fun FunctionDescriptor.isBuiltInCoroutineContext() =
        (this as? PropertyGetterDescriptor)?.correspondingProperty?.fqNameSafe == COROUTINE_CONTEXT_FQ_NAME

fun KotlinTypeChecker.areTypeParametersEquivalent(
        firstTypeParameter: TypeParameterDescriptor,
        secondTypeParameter: TypeParameterDescriptor
): Boolean {
    val superBounds = firstTypeParameter.upperBounds
    val subBounds = ArrayList(secondTypeParameter.upperBounds)
    if (superBounds.size != subBounds.size) return false

    outer@ for (superBound in superBounds) {
        val it = subBounds.listIterator()
        while (it.hasNext()) {
            val subBound = it.next()
            if (areTypesEquivalent(superBound, subBound)) {
                it.remove()
                continue@outer
            }
        }
        return false
    }

    return true
}

fun KotlinTypeChecker.areTypesEquivalent(
        firstType: KotlinType,
        secondType: KotlinType
): Boolean {
    val bothErrors = firstType.isError && secondType.isError
    return bothErrors || equalTypes(firstType, secondType)
}

fun FunctionDescriptor.isBuiltInSuspendCoroutineUninterceptedOrReturn(): Boolean {
    fun getOriginal() = module.getPackage(COROUTINES_INTRINSICS_PACKAGE_FQ_NAME).memberScope
            .getContributedFunctions(SUSPEND_COROUTINE_UNINTERCEPTED_OR_RETURN_NAME, NoLookupLocation.FROM_BACKEND)
            .singleOrNull() as CallableDescriptor

    fun createTypeChecker(
            firstParameters: List<TypeParameterDescriptor>,
            secondParameters: List<TypeParameterDescriptor>
    ): KotlinTypeChecker {
        val equalityAxioms = KotlinTypeChecker.TypeConstructorEquality { a, b -> a == b }
        assert(firstParameters.size == secondParameters.size) { "Should be the same number of type parameters: $firstParameters vs $secondParameters" }
        if (firstParameters.isEmpty()) return KotlinTypeCheckerImpl.withAxioms(equalityAxioms)

        val matchingTypeConstructors = HashMap<TypeConstructor, TypeConstructor>()
        for (i in firstParameters.indices) {
            matchingTypeConstructors.put(firstParameters[i].typeConstructor, secondParameters[i].typeConstructor)
        }

        return KotlinTypeCheckerImpl.withAxioms(KotlinTypeChecker.TypeConstructorEquality { a, b ->
            if (equalityAxioms.equals(a, b)) return@TypeConstructorEquality true
            val img1 = matchingTypeConstructors[a]
            val img2 = matchingTypeConstructors[b]
            img1 != null && img1 == b || img2 != null && img2 == a
        })
    }

    if (fqNameSafe != SUSPEND_COROUTINE_UNINTERCEPTED_OR_RETURN_FQ_NAME) return false
    // original does not have continuation, so we check it manually
    val original = getOriginal()
    if (original.typeParameters.size != typeParameters.size) return false
    if (original.valueParameters.size != valueParameters.size - 1) return false
    val typeChecker = createTypeChecker(original.typeParameters, typeParameters)
    for (i in 0 until original.typeParameters.size) {
        if (!typeChecker.areTypeParametersEquivalent(original.typeParameters[i], typeParameters[i])) return false
    }
    for (i in 0 until original.valueParameters.size) {
        if (!typeChecker.areTypesEquivalent(original.valueParameters[i].type, valueParameters[i].type)) return false
    }
    return valueParameters[original.valueParameters.size].type.constructor.declarationDescriptor?.fqNameSafe == DescriptorUtils.CONTINUATION_INTERFACE_FQ_NAME
}
