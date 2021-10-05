package com.liamcoalstudio.aurora.dsl

import com.liamcoalstudio.aurora.glfwInit
import com.liamcoalstudio.aurora.glfwTerminate
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.*

actual fun asset(path: String): Asset {
    val file = fopen("assets/$path", "r")
    fseek(file, 0, SEEK_END)
    val length = ftell(file)
    fseek(file, 0, SEEK_SET)
    val bytes = ByteArray(length.toInt())
    bytes.usePinned { fread(it.addressOf(0), 1, length.convert(), file) }
    fclose(file)
    return Asset(bytes)
}

actual fun program(f: ProgramBuilder.() -> Unit) {
    glfwInit()
    ProgramBuilder().also(f).run()
    glfwTerminate()
}
