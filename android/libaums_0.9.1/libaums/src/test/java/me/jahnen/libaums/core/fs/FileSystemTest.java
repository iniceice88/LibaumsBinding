package me.jahnen.libaums.core.fs;

import com.eclipsesource.json.JsonObject;
import me.jahnen.libaums.core.util.Pair;

import org.junit.After;
import org.junit.Before;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;
import org.xenei.junit.contract.IProducer;

import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by magnusja on 02/08/17.
 */
@Contract(FileSystem.class)
public class FileSystemTest {
    private IProducer<Pair<FileSystem, JsonObject>> producer;
    private FileSystem fs;
    private JsonObject expectedValues;

    @Contract.Inject
    public void setFileSystem(IProducer<Pair<FileSystem, JsonObject>> producer) {
        this.producer = producer;
    }

    @Before
    public void setUp() {
        newInstance();
    }

    @After
    public void cleanup() {
        producer.cleanUp();
    }

    private void newInstance() {
        Pair<FileSystem, JsonObject> pair = producer.newInstance();
        fs = pair.getLeft();
        expectedValues = pair.getRight();
    }

    @ContractTest
    public void getRootDirectory() throws Exception {
        UsbFile root = fs.getRootDirectory();
        assertNotNull(root);
        assertEquals("/", root.getName());
        assertTrue(root.isDirectory());
        assertTrue(root.isRoot());
    }

    @ContractTest
    public void getVolumeLabel() throws Exception {
        assertEquals(expectedValues.get("volumeLabel").asString(), fs.getVolumeLabel());
    }

    @ContractTest
    public void getCapacity() throws Exception {
        assertEquals(expectedValues.get("capacity").asLong(), fs.getCapacity());
    }

    @ContractTest
    public void getOccupiedSpace() throws Exception {
        newInstance();
        assertEquals(expectedValues.get("occupiedSpace").asLong(), fs.getOccupiedSpace());

        UsbFile root = fs.getRootDirectory();
        UsbFile file = root.createFile("bar.txt");

        OutputStream os = new UsbFileOutputStream(file);
        for(int i = 0; i < 4096; i++) {
            os.write("hello".getBytes());
        }
        os.close();

        assertNotEquals(expectedValues.get("occupiedSpace").asLong(), fs.getOccupiedSpace());
    }

    @ContractTest
    public void getFreeSpace() throws Exception {
        newInstance();
        assertEquals(expectedValues.get("freeSpace").asLong(), fs.getFreeSpace());

        UsbFile root = fs.getRootDirectory();
        UsbFile file = root.createFile("bar2.txt");

        OutputStream os = new UsbFileOutputStream(file);
        for(int i = 0; i < 4096; i++) {
            os.write("hello".getBytes());
        }
        os.close();

        assertNotEquals(expectedValues.get("freeSpace").asLong(), fs.getFreeSpace());
    }

    @ContractTest
    public void getChunkSize() throws Exception {
        assertEquals(expectedValues.get("chunkSize").asInt(), fs.getChunkSize());
    }

}