// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER
// JSR305_GLOBAL_REPORT warn

// FILE: test/NonNullApi.java
package test;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.PACKAGE})
@Nonnull
@TypeQualifierDefault({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NonNullApi {
}

// FILE: test/AnnotatedWithJsr.java
package test;

@NonNullApi
public class AnnotatedWithJsr {
    public String getString() {
        return "Hello";
    }
}

// FILE: test/AnnotatedWithKnown.java
package test;

import javax.annotation.Nonnull;

public class AnnotatedWithKnown {
    public @Nonnull String getString() {
        return "Hello"
    }
}

// FILE: test/NotAnnotated.java
package test;

public class NotAnnotated {
    public String getString() {
        return "Hello";
    }
}

// FILE: bar.kt
package test

val jsr = AnnotatedWithJsr()
val jsrNullable: AnnotatedWithJsr? = null

val known = AnnotatedWithKnown()
val knownNullable: AnnotatedWithKnown? = null

val notAnnotated = NotAnnotated()
val notAnnotatedNullable: NotAnnotated? = null

val a = jsr.string
val b = jsrNullable?.string
val c = known.string
val d = knownNullable?.string
val e = notAnnotated.string
val f = notAnnotatedNullable?.string

fun exclExcl() {
    a<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    b!!

    c<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    d!!

    e!!
    f!!
}

fun smartcast() {
    if (<!SENSELESS_COMPARISON!>a != null<!>) a.length else <!DEBUG_INFO_CONSTANT!>a<!><!UNSAFE_CALL!>.<!>length
    if (b != null) <!DEBUG_INFO_SMARTCAST!>b<!>.length else <!DEBUG_INFO_CONSTANT!>b<!><!UNSAFE_CALL!>.<!>length

    if (<!SENSELESS_COMPARISON!>c != null<!>) c.length else c.length
    if (d != null) <!DEBUG_INFO_SMARTCAST!>d<!>.length else <!DEBUG_INFO_CONSTANT!>d<!><!UNSAFE_CALL!>.<!>length

    if (e != null) e.length else <!DEBUG_INFO_CONSTANT!>e<!><!UNSAFE_CALL!>.<!>length
    if (f != null) <!DEBUG_INFO_SMARTCAST!>f<!>.length else <!DEBUG_INFO_CONSTANT!>f<!><!UNSAFE_CALL!>.<!>length
}