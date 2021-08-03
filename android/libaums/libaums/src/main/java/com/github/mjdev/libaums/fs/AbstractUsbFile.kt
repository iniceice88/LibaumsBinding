package com.github.mjdev.libaums.fs

import android.util.Log

import java.io.IOException

/**
 * Created by magnusja on 3/1/17.
 */

abstract class AbstractUsbFile : UsbFile {

    override val absolutePath: String
        get() {
            if (isRoot) {
                return "/"
            }

            return parent?.let { parent ->
                if (parent.isRoot) {
                    "/$name"
                } else parent.absolutePath + UsbFile.separator + name
            }.orEmpty() // should never happen
        }

    @Throws(IOException::class)
    override fun search(path: String): UsbFile? {
        var p = path

        if (!isDirectory) {
            throw UnsupportedOperationException("This is a file!")
        }

        Log.d(TAG, "search file: $p")

        if (isRoot && p == UsbFile.separator) {
            return this
        }

        if (isRoot && p.startsWith(UsbFile.separator)) {
            p = p.substring(1)
        }
        if (p.endsWith(UsbFile.separator)) {
            p = p.substring(0, p.length - 1)
        }

        val index = p.indexOf(UsbFile.separator)

        if (index < 0) {
            Log.d(TAG, "search entry: $p")

            return searchThis(p)
        } else {
            val subPath = p.substring(index + 1)
            val dirName = p.substring(0, index)
            Log.d(TAG, "search recursively $subPath in $dirName")

            val file = searchThis(dirName)
            if (file != null && file.isDirectory) {
                Log.d(TAG, "found directory $dirName")
                return file.search(subPath)
            }
        }

        Log.d(TAG, "not found $p")

        return null
    }

    @Throws(IOException::class)
    private fun searchThis(name: String): UsbFile? {
        for (file in listFiles()) {
            if (file.name == name)
                return file
        }

        return null
    }

    override fun hashCode(): Int {
        return absolutePath.hashCode()
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        // TODO add getFileSystem and check if file system is the same
        // TODO check reference
        return other is UsbFile && absolutePath == other.absolutePath
    }

    companion object {
        private val TAG = AbstractUsbFile::class.java.simpleName
    }
}
