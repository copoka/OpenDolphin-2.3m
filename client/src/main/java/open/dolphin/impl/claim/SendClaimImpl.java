package open.dolphin.impl.claim;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JOptionPane;
import open.dolphin.client.ClaimMessageEvent;
import open.dolphin.client.ClaimMessageListener;
import open.dolphin.client.ClientContext;
import open.dolphin.client.MainWindow;
import open.dolphin.project.Project;
import org.apache.log4j.Logger;

/**
 * SendClaimImpl
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author modified by masuda, Masuda Naika こりゃ失敗ｗ
 */
public class SendClaimImpl implements ClaimMessageListener {

    // Strings
    private final String retryString = "再試行";
    private final String dumpString = "ログへ記録";
    
    // Properties
    private String host;
    private int port;
    private String enc;
    private String name;
    private MainWindow context;
    private Logger logger;

    private InetSocketAddress address;
    private Thread thread;

    private SendQueueTask sendQueueTask;

    /**
     * Creates new ClaimQue
     */
    public SendClaimImpl() {
        logger = ClientContext.getClaimLogger();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public MainWindow getContext() {
        return context;
    }

    @Override
    public void setContext(MainWindow context) {
        this.context = context;
    }

    /**
     * プログラムを開始する。
     */
    @Override
    public void start() {

        setHost(Project.getString(Project.CLAIM_ADDRESS));
        setPort(Project.getInt(Project.CLAIM_PORT));
        setEncoding(Project.getString(Project.CLAIM_ENCODING));

        address = new InetSocketAddress(getHost(), getPort());

        sendQueueTask = new SendQueueTask();
        thread = new Thread(sendQueueTask, "Claim send thread");
        thread.start();

        logger.info("SendClaim started with = host = " + getHost() + " port = " + getPort());
    }

    /**
     * プログラムを終了する。
     */
    @Override
    public void stop() {

        // 未送信キューがあるならば警告する
        if (sendQueueTask.getQueueSize() > 0) {
            int option = alertDialog(ClaimException.ERROR_CODE.QUEUE_NOT_EMPTY, null);
            if (option == 1) {
                sendQueueTask.sendQueue();
            }
        }

        logDump();
        
        sendQueueTask.stop();

        thread.interrupt();
        thread = null;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getEncoding() {
        return enc;
    }

    @Override
    public void setEncoding(String enc) {
        this.enc = enc;
    }

    /**
     * カルテで CLAIM データが生成されるとこの通知を受ける。
     */
    @Override
    public void claimMessageEvent(ClaimMessageEvent e) {
        sendQueueTask.addQueue(e);
        sendQueueTask.sendQueue();
    }

    private class SendQueueTask implements Runnable {
        
        private String encoding;
        private List<ClaimMessageEvent> queue;
        private List<ClaimMessageEvent> toSend;
        private Selector selector;
        private boolean isRunning;
        
        private SendQueueTask() {
            
            queue = new CopyOnWriteArrayList<ClaimMessageEvent>();
            toSend = new CopyOnWriteArrayList<ClaimMessageEvent>();
            
            encoding = getEncoding();
            try {
                // セレクタの生成
                selector = Selector.open();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
        
        private List<ClaimMessageEvent> getQueue() {
            return queue;
        }
        
        private int getQueueSize() {
            return queue.size();
        }

        private void addQueue(ClaimMessageEvent evt) {
            queue.add(evt);
        }

        private void sendQueue() {
            // selectorを起こす
            toSend.clear();
            toSend.addAll(queue);
            selector.wakeup();
        }

        @Override
        public void run() {
            
            isRunning = true;
            int cnt;
            
            try {
                while (isRunning && (cnt = selector.select()) >= 0) {
                    
                    // wakeupしたら登録されたClaimIOHandlerをselectorに登録する
                    if (cnt == 0) {
                        for (ClaimMessageEvent evt : toSend) {
                            SocketChannel channel = SocketChannel.open();
                            channel.socket().setReuseAddress(true);
                            channel.configureBlocking(false);
                            channel.connect(address);
                            // registerは同一スレッド内でないとダメ!!
                            ClaimIOHandler handler = new ClaimIOHandler(evt, encoding);
                            channel.register(selector, SelectionKey.OP_CONNECT, handler);
                        }
                        toSend.clear();
                        continue;
                    }
                    
                    // 実際の処理
                    for (Iterator<SelectionKey> itr = selector.selectedKeys().iterator(); itr.hasNext();) {
                        SelectionKey key = itr.next();
                        itr.remove();
                        ClaimIOHandler handler = (ClaimIOHandler) key.attachment();
                        try {
                            handler.handle(key);
                        } catch (ClaimException ex) {
                            processError(ex);
                            break;
                        }
                        // 成功したものはキューから除去する
                        if (handler.isNoError()) {
                            ClaimMessageEvent evt = handler.getClaimEvent();
                            queue.remove(evt);
                        }
                    }
                }
            } catch (IOException ex) {
                logger.warn("通信エラーが発生しました" + ex);
            } catch (ClosedSelectorException ex) {
                
            } finally {
                closeAllChannel();
            }
        }

        private void closeAllChannel() {
            try {
                for (SelectionKey key : selector.keys()) {
                    try {
                        key.channel().close();
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            } catch (ClosedSelectorException ex) {
            }
        }
        
        private void stop() {
            isRunning = false;
            selector.wakeup();
        }
    }
    
    private void processError(ClaimException ex) {
        ClaimException.ERROR_CODE code = ex.getErrorCode();
        ClaimMessageEvent evt = ex.getClaimEvent();
        alertDialog(code, evt);
    }

    /**
     * Queue内の CLAIM message をログへ出力する。
     */
    private void logDump() {

        List<ClaimMessageEvent> queue = sendQueueTask.getQueue();
        for (ClaimMessageEvent claimEvent : queue) {
            logger.warn(claimEvent.getClaimInsutance());
        }
        queue.clear();
    }

    private void warnLog(String result, ClaimMessageEvent evt) {
        logger.warn(getBasicInfo(result, evt));
        logger.warn(evt.getClaimInsutance());
    }

    private void log(String result, ClaimMessageEvent evt) {
        logger.info(getBasicInfo(result, evt));
    }

    private String getBasicInfo(String result, ClaimMessageEvent evt) {

        String id = evt.getPatientId();
        String nm = evt.getPatientName();
        String sex = evt.getPatientSex();
        String title = evt.getTitle();
        String timeStamp = evt.getConfirmDate();

        StringBuilder buf = new StringBuilder();
        buf.append(result);
        buf.append("[");
        buf.append(id);
        buf.append(" ");
        buf.append(nm);
        buf.append(" ");
        buf.append(sex);
        buf.append(" ");
        buf.append(title);
        buf.append(" ");
        buf.append(timeStamp);
        buf.append("]");

        return buf.toString();
    }

    private String getErrorInfo(ClaimException.ERROR_CODE errorCode) {

        String ret;
        switch (errorCode) {
            case NO_ERROR:
                ret = "No Error";
                break;
            case QUEUE_NOT_EMPTY:
                ret = "Queue is not empty";
                break;
            case NAK_SIGNAL:
                ret = "NAK signal received from ORCA";
                break;
            case IO_ERROR:
                ret = "I/O error";
                break;
            case CONNECTION_REJECT:
                ret = "CLAIM connection rejected";
                break;
            default:
                ret = "Unknown Error";
                break;
        }
        return ret;
    }

    private int alertDialog(ClaimException.ERROR_CODE code, ClaimMessageEvent evt) {

        final String title = "OpenDolphin: CLAIM 送信";
        StringBuilder sb = new StringBuilder();

        switch (code) {
            case QUEUE_NOT_EMPTY:
                sb.append("未送信のCLAIM(レセプト)データが").append(sendQueueTask.getQueueSize());
                sb.append(" 個あります。\n");
                sb.append("CLAIM サーバとの接続を確認してください。\n");
                break;
            case NAK_SIGNAL:
                sb.append("CLAIM(レセプト)データがサーバにより拒否されました。\n");
                sb.append("送信中のデータはログに記録します。診療報酬の自動入力はできません。\n");
                break;
            case IO_ERROR:
                sb.append("CLAIM(レセプト)データの送信中にエラーがおきました。\n");
                sb.append("送信中のデータはログに記録します。診療報酬の自動入力はできません。\n");
                break;
            case CONNECTION_REJECT:
                sb.append("CLAIM(レセプト)サーバ ");
                sb.append("Host=").append(host);
                sb.append(" Port=").append(port);
                sb.append(" が応答しません。\n");
                sb.append("サーバの電源及び接続を確認してください。\n");
                break;
        }

        sb.append("1. 処理を再試行することもできます。\n");
        sb.append("2. 未送信データをログに記録して処理を継続することができます。\n");
        sb.append("   この場合、データは送信されず、診療報酬は手入力となります。\n");
        int option = JOptionPane.showOptionDialog(
                null,
                sb.toString(),
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null,
                new String[]{retryString, dumpString}, retryString);
        if (option == 1 && evt != null) {
            warnLog(getErrorInfo(code), evt);
        }
        return option;
    }
}