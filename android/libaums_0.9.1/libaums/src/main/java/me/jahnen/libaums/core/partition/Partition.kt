/*
 * (C) Copyright 2014 mjahnen <github@mgns.tech>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package me.jahnen.libaums.core.partition

import android.util.Log
import me.jahnen.libaums.core.driver.BlockDeviceDriver
import me.jahnen.libaums.core.driver.ByteBlockDevice
import me.jahnen.libaums.core.fs.FileSystem
import me.jahnen.libaums.core.fs.FileSystemFactory
import java.io.IOException

/**
 * This class represents a partition on an mass storage device. A partition has
 * a certain file system which can be accessed via [.getFileSystem].
 * This file system is needed to to access the files and directories of a
 * partition.
 *
 *
 * The method [.getVolumeLabel] returns the volume label for the
 * partition. Calling the method is equivalent to calling
 * [FileSystem.getVolumeLabel].
 *
 * @author mjahnen
 */
class Partition(blockDevice: BlockDeviceDriver, entry: PartitionTableEntry) : ByteBlockDevice(blockDevice, entry.logicalBlockAddress) {

    /**
     * The logical block address where on the device this partition starts.
     */
    /**
     *
     * @return the file system on the partition which can be used to access
     * files and directories.
     */
    lateinit var fileSystem: FileSystem
        private set

    /**
     * This method returns the volume label of the file system / partition.
     * Calling this method is equivalent to calling
     * [FileSystem.getVolumeLabel].
     *
     * @return Returns the volume label of this partition.
     */
    val volumeLabel: String
        get() = fileSystem.volumeLabel

    companion object {

        private val TAG = Partition::class.java.simpleName

        /**
         * Creates a new partition with the information given.
         *
         * @param entry
         * The entry the partition shall represent.
         * @param blockDevice
         * The underlying block device. This block device must already been initialized, see
         * [BlockDeviceDriver.init].
         * @return The newly created Partition.
         * @throws IOException
         * If reading from the device fails.
         */
        @Throws(IOException::class)
        fun createPartition(entry: PartitionTableEntry, blockDevice: BlockDeviceDriver): Partition? {
            return try {
                val partition = Partition(blockDevice, entry)
                // TODO weird triangle relationship between partiton and fs??
                val fs = FileSystemFactory.createFileSystem(entry, partition)
                partition.fileSystem = fs
                partition
            } catch (e: FileSystemFactory.UnsupportedFileSystemException) {
                Log.w(TAG, "Unsupported fs on partition")
                null
            }

        }
    }
}
