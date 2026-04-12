/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LazyReflectionTest {

    @Test
    fun lazyReflectedClass_validClass() {
        val lazyClass = lazyReflectedClass("java.lang.String")
        assertThat(lazyClass.value).isEqualTo(String::class.java)
    }

    @Test(expected = ClassNotFoundException::class)
    fun lazyReflectedClass_invalidClass() {
        val lazyClass = lazyReflectedClass("nonexistent.ClassName")
        lazyClass.value // triggers the exception
    }

    // Helper class for testing reflection
    @Suppress("unused")
    class ReflectionTarget {
        private val secretField: String = "hidden"
        private fun secretMethod(): String = "secret"
    }

    @Test
    fun lazyReflectedField_byClass() {
        val lazyField = lazyReflectedField(ReflectionTarget::class.java, "secretField")
        assertThat(lazyField.value).isNotNull()
        assertThat(lazyField.value.name).isEqualTo("secretField")
        assertThat(lazyField.value.isAccessible).isTrue()
        // Verify we can actually read the field
        val target = ReflectionTarget()
        assertThat(lazyField.value.get(target)).isEqualTo("hidden")
    }

    @Test
    fun lazyReflectedField_byClassName() {
        val className = ReflectionTarget::class.java.name
        val lazyField = lazyReflectedField(className, "secretField")
        assertThat(lazyField.value.name).isEqualTo("secretField")
    }

    @Test(expected = NoSuchFieldException::class)
    fun lazyReflectedField_invalidField() {
        val lazyField = lazyReflectedField(String::class.java, "nonexistent")
        lazyField.value
    }

    @Test
    fun lazyReflectedConstructor_byClass() {
        val lazyCtor = lazyReflectedConstructor(String::class.java, ByteArray::class.java)
        assertThat(lazyCtor.value).isNotNull()
        val instance = lazyCtor.value.newInstance(byteArrayOf(65, 66))
        assertThat(instance).isEqualTo("AB")
    }

    @Test
    fun lazyReflectedConstructor_byClassName() {
        val lazyCtor = lazyReflectedConstructor("java.lang.StringBuilder")
        assertThat(lazyCtor.value).isNotNull()
    }

    @Test
    fun lazyReflectedMethod_byClass() {
        val lazyMethod = lazyReflectedMethod(String::class.java, "charAt", Int::class.java)
        assertThat(lazyMethod.value.name).isEqualTo("charAt")
        val result = lazyMethod.value.invoke("Hello", 0) as Char
        assertThat(result).isEqualTo('H')
    }

    @Test
    fun lazyReflectedMethod_byClassName() {
        val lazyMethod = lazyReflectedMethod("java.lang.String", "length")
        assertThat(lazyMethod.value.name).isEqualTo("length")
    }

    @Test(expected = NoSuchMethodException::class)
    fun lazyReflectedMethod_invalidMethod() {
        val lazyMethod = lazyReflectedMethod(String::class.java, "nonexistent")
        lazyMethod.value
    }

    @Test
    fun lazyReflectedMethod_paramTypeAsClassName() {
        val lazyMethod = lazyReflectedMethod(
            ReflectionTarget::class.java, "secretMethod"
        )
        val target = ReflectionTarget()
        assertThat(lazyMethod.value.invoke(target)).isEqualTo("secret")
    }

    @Test
    fun lazyValue_isCached() {
        val lazyClass = lazyReflectedClass("java.lang.String")
        val first = lazyClass.value
        val second = lazyClass.value
        assertThat(first).isSameInstanceAs(second)
    }
}
