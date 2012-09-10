package open.dolphin.impl.labrcv;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import open.dolphin.client.*;
import open.dolphin.delegater.LaboDelegater;
import open.dolphin.infomodel.*;
import open.dolphin.project.Project;
import open.dolphin.table.ListTableModel;
import open.dolphin.table.StripeTableCellRenderer;

/**
 * LabTestImporter
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class NLaboTestImporter extends AbstractMainComponent {
    
    private static final String NAME = "ラボレシーバ";
    private static final String SUCCESS = "成功";
    private static final String ERROR = "エラー";
    //private static final Color ODD_COLOR = ClientContext.getColor("color.odd");
    //private static final Color EVEN_COLOR = ClientContext.getColor("color.even");
    private static final Color UNCONSTRAINED_COLOR = new Color(255,102,102);
    
    // 選択されている患者情報
    private NLaboImportSummary selectedLabo;
   
    // View
    private ListTableModel<NLaboImportSummary> tableModel;
    private NLabTestImportView view;
    
    private String clientUUID;
    private ChartEventListener scl;
    
    
    /** Creates new NLaboTestImporter */
    public NLaboTestImporter() {
        setName(NAME);
        clientUUID = Dolphin.getInstance().getClientUUID();
        scl = ChartEventListener.getInstance();
    }
    
    @Override
    public void start() {
        initComponents();
        connect();
        enter();
    }
    
    @Override
    public void enter() {
        controlMenu();
    }
    
    @Override
    public void stop() {
    }

    
    public JProgressBar getProgressBar() {
        return getContext().getProgressBar();
    }

    @SuppressWarnings("unchecked")
    public ListTableModel<NLaboImportSummary> getTableModel() {
        return (ListTableModel<NLaboImportSummary>) view.getTable().getModel();
    }
    
    public NLaboImportSummary getSelectedLabo() {
        return selectedLabo;
    }

    public void setSelectedLabo(NLaboImportSummary selectedLabo) {
        this.selectedLabo = selectedLabo;
        controlMenu();
    }
    
    public void openKarte() {
        
        boolean showReceiptMessage = Project.getBoolean("showReceiptMessage", true);
        if (showReceiptMessage) {
            JLabel msg1 = new JLabel("受付リストからオープンしないと診療データをレセコンに");
            JLabel msg2 = new JLabel("送信することができません。続けますか?");
            final JCheckBox cb = new JCheckBox("今後このメッセージを表示しない");
            cb.setFont(new Font("Dialog", Font.PLAIN, 10));
            cb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Project.setBoolean("showReceiptMessage", !cb.isSelected());
                }
            });
            JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            p1.add(msg1);
            JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            p2.add(msg2);
            JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            p3.add(cb);
            JPanel box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.add(p1);
            box.add(p2);
            box.add(p3);
            box.setBorder(BorderFactory.createEmptyBorder(0, 0, 11, 11));
            
            int option = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(getUI()),
                    new Object[]{box},
                    ClientContext.getFrameTitle(getName()),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    ClientContext.getImageIcon("about_32.gif"));
            
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // 来院情報を生成する
        PatientModel patient = selectedLabo.getPatient();
        PatientVisitModel pvt = scl.createFakePvt(patient);

        // カルテコンテナを生成する
        getContext().openKarte(pvt);
    }
    
    
    /**
     * 検索結果件数を設定しステータスパネルへ表示する。
     * @param cnt 件数
     */
    public void updateCount() {
        int count = getTableModel().getObjectCount();
        String text = String.valueOf(count);
        text += "件";
        view.getCountLbl().setText(text);
    }
    
    /**
     * メニューを制御する
     */
    private void controlMenu() {
        
        PatientModel pvt = getSelectedLabo() != null 
                         ? getSelectedLabo().getPatient() 
                         : null;
        
        boolean enabled = canOpen(pvt);
        getContext().enabledAction(GUIConst.ACTION_OPEN_KARTE, enabled);
    }
    
    /**
     * カルテを開くことが可能かどうかを返す。
     * @return 開くことが可能な時 true
     */
    private boolean canOpen(PatientModel patient) {
        if (patient == null) {
            return false;
        }
        
        if (isKarteOpened(patient)) {
            return false;
        }
     
        return true;
    }
    
    /**
     * カルテがオープンされているかどうかを返す。
     * @return オープンされている時 true
     */
    private boolean isKarteOpened(PatientModel patient) {
        if (patient != null) {
            boolean opened = false;
            List<ChartImpl> allCharts = Dolphin.getInstance().getAllCharts();
            for (ChartImpl chart : allCharts) {
                if (chart.getPatient().getId() == patient.getId()) {
                    opened = true;
                    break;
                }
            }
            return opened;
        }
        return false;
    }
    
    /**
     * 検査結果ファイルを選択し、パースする。
     */
    private void selectAndParseLabFile() {

        // 検査結果ファイルを選択する
        JFileChooser chooser = new JFileChooser();
//masuda^
        //FileNameExtensionFilter filter = new FileNameExtensionFilter("Lab Result File", "DAT","dat","DAT2","dat2","HL7","hl7", "TXT", "txt");
        final String[] fileExt = new String[]{"DAT","dat","DAT2","dat2","TXT", "txt","Hl7","hl7", "XML", "xml"};
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Lab Result File", fileExt);
//masuda$
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showOpenDialog(getUI());

        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // パースしてテーブルへ表示する
        // 登録ボタンをアクティブにする
        final File labFile = new File(chooser.getSelectedFile().getPath());

        final javax.swing.SwingWorker worker = new javax.swing.SwingWorker<List<NLaboImportSummary>, Void>() {

            @Override
            protected List<NLaboImportSummary> doInBackground() throws Exception {

                LabResultParser parse = LabParserFactory.getParser(labFile.getName());

                List<NLaboImportSummary> dataList = parse.parse(labFile);

                if (dataList!=null && dataList.size()>0) {

                    List<String> idList = new ArrayList<String>(dataList.size());
                    for (NLaboImportSummary sm : dataList) {
                        idList.add(sm.getPatientId());
                    }

//masuda^   シングルトン化
                    //LaboDelegater laboDelegater = new LaboDelegater();
                    LaboDelegater laboDelegater = LaboDelegater.getInstance();
//masuda$
                    List<PatientLiteModel> pList = laboDelegater.getConstrainedPatients(idList);

                    //for (int i = 0; i < pList.size(); i++) {
                    for (int i = 0; i < dataList.size(); i++) {
                        NLaboImportSummary sm = dataList.get(i);
                         PatientLiteModel pl = pList.get(i);
                        if (pl!=null) {
                            sm.setKarteId(pl.getPatientId());
                            sm.setKarteBirthday(pl.getBirthday());
                            sm.setKarteKanaName(pl.getKanaName());
                            sm.setKarteName(pl.getFullName());
                            sm.setKarteSex(pl.getGenderDesc());
                        }
                    }
                }

                return dataList;
            }

            @Override
            protected void done() {

                try {
                    List<NLaboImportSummary> allModules = get();
                    getTableModel().setDataProvider(allModules);

                } catch (Throwable e) {
                    e.printStackTrace(System.err);
                    String why = e.getMessage();
                    Window parent = SwingUtilities.getWindowAncestor(getUI());
                    StringBuilder sb = new StringBuilder();
                    sb.append("登録できないファイルがあります。").append("\n");
                    sb.append("検査結果ファイルに誤りがある可能性があります。").append("\n");
                    sb.append(why);
                    String message = sb.toString();
                    String title = "ラボレシーバ";
                    JOptionPane.showMessageDialog(parent, message, ClientContext.getFrameTitle(title), JOptionPane.WARNING_MESSAGE);
                }
            }
        };

        worker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue().equals(javax.swing.SwingWorker.StateValue.STARTED)) {
                    getProgressBar().setIndeterminate(true);
                } else if (evt.getNewValue().equals(javax.swing.SwingWorker.StateValue.DONE)) {
                    getProgressBar().setIndeterminate(false);
                    getProgressBar().setValue(0);
                    worker.removePropertyChangeListener(this);
                }
            }
        });

        worker.execute();
    }

    /**
     * パースした検査結果を登録する。
     */
    private void addLabtest() {

        final List<NLaboImportSummary> modules = getTableModel().getDataProvider();

        final javax.swing.SwingWorker worker = new javax.swing.SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {

//masuda^   シングルトン化
                //LaboDelegater laboDelegater = new LaboDelegater();
                LaboDelegater laboDelegater = LaboDelegater.getInstance();
//masuda$

                for (NLaboImportSummary summary : modules) {

//masuda^           // OpenDolphinに登録されていない場合はスキップする　のつはる診療所　白坂先生のご提案
                    if (summary.getKarteId() == null) {
                        summary.setResult(ERROR);
                        continue;
                    }
                    //MMLの場合の処理
                    //PatientModel pm = laboDelegater.putNLaboModule(summary.getModule());
                    PatientModel pm = null;
                    if ("MML".equals(summary.getReportFormat())) {
                        summary.getModule().setPatientName(summary.getKarteName());
                        summary.getModule().setPatientSex(summary.getKarteSex());
                        LaboModuleValue labo13 = summary.getLabo13();
                        if (labo13 != null) {
                            pm = laboDelegater.putMmlLaboModule(summary.getLabo13());
                        }
                    } else {
                        pm = laboDelegater.postNLaboModule(summary.getModule());
                    }
//masuda$
                    if (pm != null) {
                        summary.setPatient(pm);
                        summary.setResult(SUCCESS);

                    } else {
                        summary.setResult(ERROR);
                    }

                    // Table 更新
                    Runnable awt = new Runnable() {
                        @Override
                        public void run() {
                            getTableModel().fireTableDataChanged();
                        }
                    };
                    EventQueue.invokeLater(awt);
                }

                return null;
            }
        };

        worker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue().equals(javax.swing.SwingWorker.StateValue.STARTED)) {
                    getProgressBar().setIndeterminate(true);
                } else if (evt.getNewValue().equals(javax.swing.SwingWorker.StateValue.DONE)) {
                    getProgressBar().setIndeterminate(false);
                    getProgressBar().setValue(0);
                    worker.removePropertyChangeListener(this);
                }
            }
        });

        worker.execute();
    }

    
    /**
     * コンポーンントにリスナを登録し接続する。
     */
    private void connect() {

        // ファイル選択ボタン
        view.getFileBtn().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // ファイル選択
                selectAndParseLabFile();
            }
        });

        // 登録ボタン
        view.getAddBtn().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // 検査結果登録
                view.getAddBtn().setEnabled(false);
                addLabtest();
            }
        });
        view.getAddBtn().setEnabled(false);

        // クリアボタン
        view.getClearBtn().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // 検査結果登録
                getTableModel().setDataProvider(null);
            }
        });
        view.getClearBtn().setEnabled(false);
        
        // 行選択
        view.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    NLaboImportSummary lab = getTableModel().getObject(view.getTable().getSelectedRow());
                    if (lab != null) {
                        setSelectedLabo(lab);
                    }
                }
            }
        });
        
        // ダブルクリック
        view.getTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    NLaboImportSummary lab = getTableModel().getObject(view.getTable().getSelectedRow());
                    if (lab != null && lab.getPatient()!=null) {
                        openKarte();
                    }
                }
            }
        });

        // コンテキストメニューリスナを設定する
        view.getTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                mabeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mabeShowPopup(e);
            }

            private void mabeShowPopup(MouseEvent e) {

                if (e.isPopupTrigger()) {
                    
//masuda^   インポート前はPatientModelが設定されていないのでpopupしない
                    if (selectedLabo == null || selectedLabo.getPatient() == null) {
                        return;
                    }
//masuda$
                    final JPopupMenu contextMenu = new JPopupMenu();

                    JTable table = view.getTable();
                    int row = table.rowAtPoint(e.getPoint());
                    Object obj = (Object) getTableModel().getObject(row);
                    int selected = table.getSelectedRow();

                    if (row == selected && obj != null) {
                        String pop1 = ClientContext.getString("watingList.popup.openKarte");
                        contextMenu.add(new JMenuItem(new ReflectAction(pop1, NLaboTestImporter.this, "openKarte")));
                    }
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // data 件数リスナ
        getTableModel().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                @SuppressWarnings("unchecked")
                List<NLaboImportSummary> list = (List<NLaboImportSummary>) evt.getNewValue();
                boolean enabled = (list != null && list.size() > 0);
                boolean clearOk = enabled;
//mauda^    
                // OpenDolphnに登録されていない患者があってもとりあえずボタンはenableする
                // のつはる診療所　白坂先生のご提案
/*
                if (enabled) {
                    for (NLaboImportSummary sm : list) {
                        if (sm.getKarteId()==null) {
                            enabled = false;
                            break;
                        }
                    }
                }
*/
//masuda$
                view.getAddBtn().setEnabled(enabled);
                view.getClearBtn().setEnabled(clearOk);
                updateCount();
            }
        });
    }

    /**
     * GUI コンポーネントを初期化する。
     */
    private void initComponents() {

        view = new NLabTestImportView();
        setUI(view);

//masuda^
        //String[] columnNames = new String[]{"ラボ", "患者ID", "カナ", "カルテ・カナ", "性別", "カルテ・性別", "検体採取日", "項目数", "登録"};
        //String[] propNames = new String[]{"laboCode", "patientId", "patientName", "karteKanaName", "patientSex", "karteSex", "sampleDate", "numOfTestItems", "result"};
        //int[] columnWidth = new int[]{50, 120, 120, 120, 50, 70, 120, 50, 80};
        String[] columnNames = new String[]{"型", "ラボ", "患者ID", "データ氏名", "デ性別", "カルテ氏名", "カ性別", "検体採取日", "項目数", "登録"};
        String[] propNames = new String[]{"reportFormat", "laboCode", "patientId", "patientName", "patientSexKanji", "karteName", "karteSex", "sampleDate", "numOfTestItems", "result"};
        int[] columnWidth = new int[]{50, 50, 80, 100, 50, 100, 50, 80, 50, 50};
        final int idColumn = 2;

        tableModel = new ListTableModel<NLaboImportSummary>(columnNames, 30, propNames, null) {
            @Override
            public boolean isCellEditable(int row, int col) {
//masuda^   未登録の場合のみID編集可能
                if (col == idColumn && getObject(row) != null && getObject(row).getPatient() == null) {
                    return true;
                }
//masuda$
                return false;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {

                if (col != 1 || value == null || value.equals("")) {
                    return;
                }

                NLaboImportSummary summary = getObject(row);
                if (summary == null) {
                    return;
                }
                updatePatientId(summary);

            }
        };
//masuda$
        
        view.getTable().setModel(tableModel);
        //view.getTable().setRowHeight(ClientContext.getHigherRowHeight());
        view.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        view.getTable().setTransferHandler(new NLaboTestFileTransferHandler(this));

        // カラム幅を変更する
        for (int i = 0; i < columnWidth.length; i++) {
            view.getTable().getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);
        }

        // レンダラを設定する
//masuda^    ストライプテーブル
        //view.getTable().setDefaultRenderer(Object.class, new NLaboTestImporter.LabTestRenderer());
        LabTestRenderer renderer = new LabTestRenderer();
        renderer.setTable(view.getTable());
        renderer.setDefaultRenderer();
        
        // 患者IDカラムにセルエディタを登録
        JTextField tf = new JTextField();
        tf.addFocusListener(AutoRomanListener.getInstance());
        TableColumn column = view.getTable().getColumnModel().getColumn(idColumn);
        column.setCellEditor(new DefaultCellEditor2(tf));
//masuda$
        
        // カウント値０を設定する
        updateCount();
    }

//masuda^
    // 患者IDを更新する
    private void updatePatientId(NLaboImportSummary summary) {
        String ptId = summary.getPatientId();
        NLaboModule module = summary.getModule();
        module.setPatientId(ptId);
        List<NLaboItem> modules = module.getItems();
        for (NLaboItem item : modules) {
            item.setPatientId(ptId);
        }
    }
    
    // ストライプテーブル
    protected class LabTestRenderer extends StripeTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {

            super.getTableCellRendererComponent(table, value, isSelected, isFocused, row, col);

            NLaboImportSummary summary = tableModel.getObject(row);
            if (summary != null && summary.getKarteId() == null) {
                this.setBackground(UNCONSTRAINED_COLOR);
            }

            return this;
        }
    }

    // ChartEventListener
    @Override
    public void onEvent(ChartEventModel evt) {

        int sRow = -1;
        long ptPk = evt.getPtPk();
        List<NLaboImportSummary> list = tableModel.getDataProvider();
        ChartEventModel.EVENT eventType = evt.getEventType();

        switch (eventType) {
            case PVT_STATE:
                for (int row = 0; row < list.size(); ++row) {
                    NLaboImportSummary nlab = list.get(row);
                    PatientModel pm = nlab.getPatient();
                    if (ptPk == pm.getId()) {
                        sRow = row;
                        pm.setOwnerUUID(evt.getOwnerUUID());
                        break;
                    }
                }
                break;
            case PM_MERGE:
                for (int row = 0; row < list.size(); ++row) {
                    NLaboImportSummary nlab = list.get(row);
                    PatientModel pm = nlab.getPatient();
                    if (ptPk == pm.getId()) {
                        sRow = row;
                        nlab.setPatient(evt.getPatientModel());
                        break;
                    }
                }
                break;                
            case PVT_MERGE:
                for (int row = 0; row < list.size(); ++row) {
                    NLaboImportSummary nlab = list.get(row);
                    PatientModel pm = nlab.getPatient();
                    if (ptPk == pm.getId()) {
                        sRow = row;
                        nlab.setPatient(evt.getPatientVisitModel().getPatientModel());
                        break;
                    }
                }
                break;
            default:
                break;
        }
        
        if (sRow != -1) {
            tableModel.fireTableRowsUpdated(sRow, sRow);
        }
    }
//masuda$
}