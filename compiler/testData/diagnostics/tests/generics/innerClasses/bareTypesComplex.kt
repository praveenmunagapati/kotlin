// !WITH_NEW_INFERENCE
open class SuperOuter<E> {
    inner open class SuperInner<F>
}

class DerivedOuter<G> : SuperOuter<G>() {
    inner class DerivedInner<H> : SuperOuter<G>.<!NI;UNRESOLVED_REFERENCE_WRONG_RECEIVER!><!NI;DEBUG_INFO_UNRESOLVED_WITH_TARGET!>SuperInner<!><!><H>()
}

fun bare(x: SuperOuter<*>.SuperInner<*>, y: Any?) {
    if (<!NI;USELESS_IS_CHECK!><!USELESS_IS_CHECK!>x is SuperOuter.SuperInner<!><!>) return
    if (y is <!NI;NO_TYPE_ARGUMENTS_ON_RHS!><!NO_TYPE_ARGUMENTS_ON_RHS!>SuperOuter.SuperInner<!><!>) {
        return
    }
}
