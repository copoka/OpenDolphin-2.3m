package open.dolphin.stampbox;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import open.dolphin.client.ClientContext;
import open.dolphin.dao.SqlOrcaSetDao;
import open.dolphin.helper.SimpleWorker;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.OrcaInputCd;
import open.dolphin.project.Project;

/**
 * ORCA StampTree クラス。
 *
 * @author Kazushi Minagawa
 */
public class OrcaTree extends StampTree {
    
    private static final String MONITOR_TITLE = "ORCAセット検索";
    
    /** ORCA 入力セットをフェッチしたかどうかのフラグ */
    private boolean fetched;

    private ProgressMonitor monitor;
    private Timer taskTimer;
    private int delayCount;

    /** 
     * Creates a new instance of OrcaTree 
     */
    public OrcaTree(TreeModel model) {
        super(model);
    }
    
    /**
     * ORCA 入力セットをフェッチしたかどうかを返す。
     * @return 取得済みのとき true
     */
    public boolean isFetched() {
        return fetched;
    }
    
    /**
     * ORCA 入力セットをフェッチしたかどうかを設定する。
     * @param fetched 取得済みのとき true
     */
    public void setFetched(boolean fetched) {
        this.fetched = fetched;
    }
    
    /**
     * StampBox のタブでこのTreeが選択された時コールされる。
     */
    @Override
    public void enter() {
        
        if (!fetched) {

            // CLAIM(Master) Address が設定されていない場合に警告する
            String address = Project.getString(Project.CLAIM_ADDRESS);
            if (address == null || address.equals("")) {
//                if (SwingUtilities.isEventDispatchThread()) {
//                    String msg0 = "レセコンのIPアドレスが設定されていないため、マスターを検索できません。";
//                    String msg1 = "環境設定メニューからレセコンのIPアドレスを設定してください。";
//                    Object message = new String[]{msg0, msg1};
//                    Window parent = SwingUtilities.getWindowAncestor(OrcaTree.this);
//                    String title = ClientContext.getFrameTitle(MONITOR_TITLE);
//                    JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
//                }
                return;
            }

            if (SwingUtilities.isEventDispatchThread()) {
                fetchOrcaSet();
            } else {
                fetchOrcaSet2();
            }
        }
    }
    
    /**
     * ORCA の入力セットを取得しTreeに加える。
     */
    private void fetchOrcaSet2() {
        
        try {
            SqlOrcaSetDao dao = new SqlOrcaSetDao();
            
            ArrayList<OrcaInputCd> inputSet = dao.getOrcaInputSet();
            StampTreeNode root = (StampTreeNode) this.getModel().getRoot();
            
            for (OrcaInputCd set : inputSet) {
                ModuleInfoBean stampInfo = set.getStampInfo();
                StampTreeNode node = new StampTreeNode(stampInfo);
                root.add(node);
            }
            
            DefaultTreeModel model = (DefaultTreeModel) this.getModel();
            model.reload(root);
            
            setFetched(true);
            
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
      
    /**
     * ORCA の入力セットを取得しTreeに加える。
     */
    private void fetchOrcaSet() {

        String message = MONITOR_TITLE;
        String note = "入力セットを検索しています...  ";
        final Component c = SwingUtilities.getWindowAncestor(this);
        int maxEstimation = 60 * 1000;
        int delay = 300;

        final SimpleWorker worker = new SimpleWorker<List<OrcaInputCd>, Void>() {

            @Override
            protected List<OrcaInputCd> doInBackground() throws Exception {
                SqlOrcaSetDao dao = new SqlOrcaSetDao();
                List<OrcaInputCd> result = dao.getOrcaInputSet();
                if (dao.isNoError()) {
                    return result;
                } else {
                    throw new Exception(dao.getErrorMessage());
                }
            }

            @Override
            protected void succeeded(List<OrcaInputCd> result) {
                processResult(result);
            }

            @Override
            protected void failed(Throwable e) {
                String title = ClientContext.getFrameTitle(MONITOR_TITLE);
                JOptionPane.showMessageDialog(c, e.getMessage(), title, JOptionPane.WARNING_MESSAGE);
            }

            @Override
            protected void startProgress() {
                delayCount = 0;
                taskTimer.start();
            }

            @Override
            protected void stopProgress() {
                taskTimer.stop();
                monitor.close();
                taskTimer = null;
                monitor = null;
            }
        };

        monitor = new ProgressMonitor(c, message, note, 0, maxEstimation / delay);
        taskTimer = new Timer(delay, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                delayCount++;

                if (monitor.isCanceled() && (!worker.isCancelled())) {
                    worker.cancel(true);

                } else {
                    monitor.setProgress(delayCount);
                }
            }
        });

        worker.execute();
    }
    
    /**
     * ORCAセットのStampTreeを構築する。
     */
    private void processResult(List<OrcaInputCd> inputSet) {
        
        StampTreeNode root = (StampTreeNode) this.getModel().getRoot();

        for (OrcaInputCd set : inputSet) {
            ModuleInfoBean stampInfo = set.getStampInfo();
            StampTreeNode node = new StampTreeNode(stampInfo);
            root.add(node);
        }

        DefaultTreeModel model = (DefaultTreeModel) this.getModel();
        model.reload(root);

        setFetched(true);
    }
}
