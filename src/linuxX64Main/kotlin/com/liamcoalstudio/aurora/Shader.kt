package com.liamcoalstudio.aurora

import com.liamcoalstudio.aurora.enums.ShaderStage
import kotlinx.cinterop.*

actual class Shader internal actual constructor(
    actual val id: UInt,
    actual val stage: ShaderStage
) : Resource() {
    actual constructor(stage: ShaderStage, source: String) : this(
        glCreateShader!!(stage.native),
        stage
    ) {
        pushSource(source)
    }

    actual override fun delete() {
        glDeleteShader!!(id)
    }

    @Deprecated("Not bindable.", level = DeprecationLevel.HIDDEN)
    actual override fun bind() {
        throw IllegalStateException("cannot bind singular shader; only Programs can be bound")
    }

    actual fun pushSource(src: String): String? {
        val v = src.cstr.getPointer(Arena())
        val n = nativeHeap.alloc<CPointerVar<ByteVar>>()
        n.value = v

        glShaderSource!!(id, 1, n.ptr, null)
        glCompileShader!!(id)

        val s = nativeHeap.alloc(8192, 1)
        val i = nativeHeap.alloc<GLsizeiVar>()
        glGetShaderInfoLog!!(id, 8192, i.ptr, s.reinterpret())

        val returnValue: String? = if(i.value > 0) {
            s.reinterpret<CPointerVar<ByteVar>>().value!!.toKString()
        } else {
            null
        }

        val status = nativeHeap.alloc<GLintVar>()
        glGetShaderiv!!(id, GL_COMPILE_STATUS.convert(), status.ptr)

        if(status.value == 0) {
            returnValue?.let(::error)
            throw IllegalStateException("Shader failed to compile")
        } else {
            return returnValue
        }
    }

    actual override val isValid: Boolean
        get() = glIsShader!!(id) > 0u
}