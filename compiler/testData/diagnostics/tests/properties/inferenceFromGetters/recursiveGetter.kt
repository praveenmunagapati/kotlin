// !WITH_NEW_INFERENCE
// !CHECK_TYPE

val x get() = <!NI;TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!><!NI;DEBUG_INFO_MISSING_UNRESOLVED!><!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>x<!><!><!>

class A {
    val y get() = <!NI;TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!><!NI;DEBUG_INFO_MISSING_UNRESOLVED!><!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>y<!><!><!>

    val a get() = <!NI;DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!><!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>b<!><!>
    val b get() = <!NI;TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!><!NI;DEBUG_INFO_MISSING_UNRESOLVED!><!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>a<!><!><!>

    val <!NI;IMPLICIT_NOTHING_PROPERTY_TYPE!>z1<!> get() = id(<!NI;TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!><!NI;DEBUG_INFO_MISSING_UNRESOLVED!><!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!><!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>z1<!><!><!><!>)
    val z2 get() = l(<!NI;TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!><!NI;DEBUG_INFO_MISSING_UNRESOLVED!><!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!><!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>z2<!><!><!><!>)

    val u get() = <!NI;UNRESOLVED_REFERENCE!><!UNRESOLVED_REFERENCE!>field<!><!>
}

fun <E> id(x: E) = x
fun <E> l(<!NI;UNUSED_PARAMETER!><!UNUSED_PARAMETER!>x<!><!>: E): List<E> = null!!
