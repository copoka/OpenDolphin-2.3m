package open.dolphin.impl.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.util.Date;
import open.dolphin.client.ClientContext;

/**
 * PvtClaimAcceptHandler, client
 *
 * @author masuda, Masuda Naika
 * http://itpro.nikkeibp.co.jp/article/COLUMN/20060515/237871/
 */
public class PvtClaimAcceptHandler implements IHandler {

    private PVTClientServer server;

    public PvtClaimAcceptHandler(PVTClientServer server) {
        this.server = server;
    }

    @Override
    public void handle(SelectionKey key) throws ClosedChannelException, IOException {

        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();

        // アクセプト処理
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);

        // 入出力用のハンドラを生成し、アタッチする
        // 監視する操作は読み込みのみ
        PvtClaimIOHandler handler = new PvtClaimIOHandler(server);
        channel.register(key.selector(), SelectionKey.OP_READ, handler);

        // ログ出力
        String addr = channel.socket().getInetAddress().getHostAddress();
        String time = DateFormat.getDateTimeInstance().format(new Date());
        ClientContext.getPvtLogger().info("Connected from " + addr + " at " + time);
    }
}
