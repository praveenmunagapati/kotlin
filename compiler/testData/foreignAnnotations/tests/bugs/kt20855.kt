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

// FILE: test/package-info.java
@NonNullApi
package test;

// FILE: test/Java.java
package test;

public class J {
    public String getNonNullString() {
        return "Hello";
    }
}

// FILE: bar.kt
package test

val nullableInstance: J? = null

fun safeCall() {
    val a: Int? = nullableInstance?.getNonNullString()?.length
}

fun exclExcl() {
    val a: String = nullableInstance?.getNonNullString()!!
}

fun comparisonsWithNull() {
    val a = nullableInstance?.getNonNullString() == null
    val b = nullableInstance?.getNonNullString() === null
    val c = nullableInstance?.getNonNullString() != null
    val d = nullableInstance?.getNonNullString() !== null
}