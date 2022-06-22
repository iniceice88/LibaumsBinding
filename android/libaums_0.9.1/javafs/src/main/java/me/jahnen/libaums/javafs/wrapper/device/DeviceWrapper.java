package me.jahnen.libaums.javafs.wrapper.device;

import me.jahnen.libaums.core.driver.BlockDeviceDriver;
import me.jahnen.libaums.core.partition.PartitionTableEntry;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;

/**
 * Created by magnusja on 2/28/17.
 */

public class DeviceWrapper extends Device {

    public DeviceWrapper(BlockDeviceDriver blockDevice, PartitionTableEntry entry) {
        super("");
        FSBlockDeviceWrapper wrapper = new FSBlockDeviceWrapper(blockDevice, entry);
        registerAPI(FSBlockDeviceAPI.class, wrapper);
        registerAPI(BlockDeviceAPI.class, wrapper);
    }
}
