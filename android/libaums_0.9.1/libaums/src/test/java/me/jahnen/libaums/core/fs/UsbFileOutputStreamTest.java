package me.jahnen.libaums.core.fs;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by magnusja on 12/08/17.
 */
public class UsbFileOutputStreamTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Captor
    private ArgumentCaptor<Long> longCaptor;
    @Captor
    private ArgumentCaptor<ByteBuffer> byteBufferCaptor;

    @Mock
    private UsbFile file;

    @Test(expected = UnsupportedOperationException.class)
    public void throwIfDirectory() {
        when(file.isDirectory()).thenReturn(true);

        new UsbFileOutputStream(file);
    }

    @Test
    public void close() throws Exception {
        new UsbFileOutputStream(file).close();

        verify(file).close();
    }

    @Test
    public void flush() throws Exception {
        new UsbFileOutputStream(file).flush();

        verify(file).flush();
    }

    @Test
    public void writeInt() throws Exception {
        OutputStream os = new UsbFileOutputStream(file);

        os.write(0);

        verify(file).write(longCaptor.capture(), byteBufferCaptor.capture());
        assertEquals(0, longCaptor.getValue().longValue());
        assertNotNull(byteBufferCaptor.getValue());

        os.write(new byte[1]);
        verify(file, times(2)).write(longCaptor.capture(), byteBufferCaptor.capture());
        assertEquals(1, longCaptor.getValue().longValue());
        assertNotNull(byteBufferCaptor.getValue());
    }

    @Test
    public void write() throws Exception {
        OutputStream os = new UsbFileOutputStream(file);

        os.write(new byte[20]);

        verify(file).write(longCaptor.capture(), byteBufferCaptor.capture());
        assertEquals(0, longCaptor.getValue().longValue());
        assertEquals(20, byteBufferCaptor.getValue().remaining());

        os.write(new byte[20]);
        verify(file, times(2)).write(longCaptor.capture(), byteBufferCaptor.capture());
        assertEquals(20, longCaptor.getValue().longValue());
        assertEquals(20, byteBufferCaptor.getValue().remaining());
    }

    @Test
    public void writeOffset() throws Exception {

        OutputStream os = new UsbFileOutputStream(file);

        os.write(new byte[20], 10, 5);

        verify(file).write(longCaptor.capture(), byteBufferCaptor.capture());
        assertEquals(0, longCaptor.getValue().longValue());
        assertEquals(10, byteBufferCaptor.getValue().position());
        assertEquals(5, byteBufferCaptor.getValue().remaining());

        os.write(new byte[20], 5, 10);
        verify(file, times(2)).write(longCaptor.capture(), byteBufferCaptor.capture());
        assertEquals(5, longCaptor.getValue().longValue());
        assertEquals(5, byteBufferCaptor.getValue().position());
        assertEquals(10, byteBufferCaptor.getValue().remaining());
    }


    @Test
    public void closeSetLength() throws Exception {
        OutputStream os = new UsbFileOutputStream(file);

        os.write(new byte[20]);
        os.close();
        verify(file).setLength(longCaptor.capture());

        assertEquals(20, longCaptor.getValue().longValue());
    }

    @Test
    public void testAppend() throws Exception {
        when(file.getLength()).thenReturn(20L);
        OutputStream os = new UsbFileOutputStream(file, true);

        os.write(new byte[20]);
        os.close();

        verify(file).setLength(longCaptor.capture());

        assertEquals(40, longCaptor.getValue().longValue());
    }


}