// !WITH_NEW_INFERENCE
abstract class Runnable {
    abstract fun run()
}

fun foo(): Int {
    val c: Int? = null
    val a: Int? = 1
    if (c is Int) {
        val k = object: Runnable() {
            init {
                a!!.toInt()
            }
            override fun run() = Unit
        }
        k.run()
        val d: Int = <!NI;DEBUG_INFO_SMARTCAST!><!DEBUG_INFO_SMARTCAST!>c<!><!>
        // a is not null because of k constructor, but we do not know it
        return a <!NI;UNRESOLVED_REFERENCE_WRONG_RECEIVER!><!NI;DEBUG_INFO_UNRESOLVED_WITH_TARGET!><!UNSAFE_OPERATOR_CALL!>+<!><!><!> d
    }
    else return -1
}
