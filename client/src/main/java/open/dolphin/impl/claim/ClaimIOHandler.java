package open.dolphin.impl.claim;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import open.dolphin.client.ClaimMessageEvent;

/**
 * ClaimIOHandler
 * @author masuda, Masuda Naika
 */
public class ClaimIOHandler {
 
    // Socket constants
    private static final int EOT = 0x04;
    private static final int ACK = 0x06;
    private static final int NAK = 0x15;
    
    private SendClaimImpl context;
    private ClaimMessageEvent evt;
    private boolean noError;

    
    public ClaimIOHandler(SendClaimImpl context, ClaimMessageEvent evt) {
        this.context = context;
        this.evt = evt;
    }
    
    public ClaimMessageEvent getClaimEvent() {
        return evt;
    }

    public boolean isNoError() {
        return noError;
    }
    
    public void handle(SelectionKey key) throws ClaimException {

        if (key.isConnectable()) {
            doConnect(key);
        } else {
            if (key.isValid() && key.isReadable()) {
                doRead(key);
            }
            if (key.isValid() && key.isWritable()) {
                doWrite(key);
            }
        }
    }

    // 接続する
    private void doConnect(SelectionKey key) throws ClaimException {
        
        SocketChannel channel = (SocketChannel) key.channel();

        try {
            if (channel.isConnectionPending()) {
                channel.finishConnect();
            }
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException ex) {
            throw new ClaimException(ClaimException.ERROR_CODE.IO_ERROR, evt);
        }
    }
    
    // データを書き出す
    private void doWrite(SelectionKey key) throws ClaimException {

        SocketChannel channel = (SocketChannel) key.channel();

        try {
            byte[] bytes = evt.getClaimInsutance().getBytes(context.getEncoding());
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 1);
            buffer.put(bytes);
            buffer.put((byte) EOT);
            buffer.flip();
            channel.write(buffer);
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException ex) {
            throw new ClaimException(ClaimException.ERROR_CODE.IO_ERROR, evt);
        }
    }

    // 返事を受け取る
    private void doRead(SelectionKey key) throws ClaimException {

        SocketChannel channel = (SocketChannel) key.channel();
        ClaimException.ERROR_CODE errorCode = null;

        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1);
            channel.read(byteBuffer);
            byteBuffer.flip();
            int c = byteBuffer.get();

            switch (c) {
                case ACK:
                    errorCode = ClaimException.ERROR_CODE.NO_ERROR;
                    noError = true;
                    break;
                case NAK:
                    errorCode = ClaimException.ERROR_CODE.NAK_SIGNAL;
                    break;
                default:
                    errorCode = ClaimException.ERROR_CODE.IO_ERROR;
                    break;
            }
        } catch (IOException ex) {
            throw new ClaimException(ClaimException.ERROR_CODE.IO_ERROR, evt);
        } finally {
            try {
                channel.close();
                if (errorCode != ClaimException.ERROR_CODE.NO_ERROR) {
                    throw new ClaimException(errorCode, evt);
                }
            } catch (IOException ex) {
                throw new ClaimException(ClaimException.ERROR_CODE.IO_ERROR, evt);
            }
        }
    }
}
