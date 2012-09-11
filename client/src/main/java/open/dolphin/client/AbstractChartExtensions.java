package open.dolphin.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import open.dolphin.infomodel.AdmissionModel;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.project.Project;
import open.dolphin.setting.MiscSettingPanel;

/**
 * AbstractChartExtensions
 * 
 * @author masuda, Masuda Naika
 */
public abstract class AbstractChartExtensions {
    
    protected JButton baseChargeBtn;
    protected JButton rpLabelBtn;
    
    private static final ImageIcon ICON_WIZ = ClientContext.getImageIcon("wiz_24.gif");
    private static final ImageIcon ICON_LBL = ClientContext.getImageIcon("prtpv_24.gif");
    
    protected Chart context;
    // タイマー、ChartImplから移動
    private static final long DELAY = 10L;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> beeperHandle;
    private long statred;
    private long delay = DELAY;
    
    // 抽象メソッド
    public abstract JToolBar createToolBar();
    
    protected abstract Chart getContext();

    
    // 共通ボタンを追加。基本料入力とラベル印刷
    protected void addCommonBtn(JToolBar myToolBar) {

        // toolBarに基本料入力ボタンと処方ラベル印刷ボタンを追加
            baseChargeBtn = new JButton();
            baseChargeBtn.setEnabled(false);
            baseChargeBtn.setIcon(ICON_WIZ);
            baseChargeBtn.setToolTipText("基本料スタンプを挿入します。");
            myToolBar.add(baseChargeBtn);
            baseChargeBtn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    insertBaseChargeStamp();
                }
            });
        // toolBarにラベルプリンタのアドレスが設定されていればラベルプリンタのボタンを追加。
        // 空白なら不使用として非表示
        String lblPrtAddress = Project.getString(MiscSettingPanel.LBLPRT_ADDRESS, null);
        if (lblPrtAddress != null && !"".equals(lblPrtAddress)) {
            rpLabelBtn = new JButton();
            rpLabelBtn.setEnabled(false);
            rpLabelBtn.setIcon(ICON_LBL);
            rpLabelBtn.setToolTipText("処方ラベルを印刷します。");
            myToolBar.add(rpLabelBtn);
            rpLabelBtn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    printMedicineLabel();
                }
            });
        }
    }
    
    // 薬剤ラベルを印刷
    private void printMedicineLabel() {

        KarteEditor editor = context.getKarteEditor();
        if (editor == null) {
            return;
        }
        PrintLabel pl = new PrintLabel();
        pl.enter(editor.getPPane());
    }

    // 基本料入力
    private void insertBaseChargeStamp() {

        final KarteEditor editor = context.getKarteEditor();
        if (editor == null) {
            return;
        }

        MakeBaseChargeStamp mbcs = new MakeBaseChargeStamp();
        mbcs.enter(editor);
        if (mbcs.isModified()) {
            ModuleModel mm = mbcs.getBaseChargeStamp();
            editor.getPPane().getTextPane().setCaretPosition(0);
            editor.getPPane().stamp(mm);
        }
        mbcs = null;
    }
    
    // 診察時間タイマーと基本料入力・薬剤ラベル印刷ボタンをenableする。
    public void enableBtnTimer() {

        // 基本料・ラベル印刷ボタンをenableする。
        enableExtBtn(true);

        // timer 開始
        statred = System.currentTimeMillis();
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }
        final Runnable beeper = new Runnable() {

            @Override
            public void run() {
                long time = System.currentTimeMillis() - statred;
                time = time / 1000L;
                context.getStatusPanel().setTimeInfo(time);
            }
        };
        beeperHandle = scheduler.scheduleAtFixedRate(beeper, delay, delay, TimeUnit.SECONDS);
    }

    public void shutdownTimer() {
        if (beeperHandle != null) {
            beeperHandle.cancel(true);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
    
    // 基本料・ラベル印刷ボタンをenableする。
    public void enableExtBtn(boolean b) {

        KarteEditor editor = getContext().getKarteEditor();
        AdmissionModel admission = (editor == null)
                ? null 
                : editor.getModel().getDocInfoModel().getAdmissionModel();
        
        // 入院ならば基本料スタンプボタンは無効
        if (admission != null) {
            baseChargeBtn.setEnabled(false);
            baseChargeBtn.setVisible(false);
        } else {
            baseChargeBtn.setEnabled(b);
            baseChargeBtn.setVisible(b);
        }
        if (rpLabelBtn != null) {
            rpLabelBtn.setEnabled(b);
            rpLabelBtn.setVisible(b);
        }
    }
}
