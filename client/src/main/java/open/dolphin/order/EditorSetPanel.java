package open.dolphin.order;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import open.dolphin.client.ClientContext;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.infomodel.*;
import open.dolphin.stampbox.StampTree;
import open.dolphin.stampbox.StampTreeNode;
import open.dolphin.util.BeanUtils;

/**
 * EditorSetPanel
 *
 * @author Minagawa,Kazushi
 * @author modified by masuda, Masuda Naika
 */
public class EditorSetPanel extends JPanel implements PropertyChangeListener, TreeSelectionListener {

    public static final String EDITOR_VALUE_PROP = "editorValue";

    private static final String TITLE_NEW = "新規";
    private static final String TITLE_REPLACE = "置換";
    private static final String TITLE_IMPORT = "取込";
    private static final String TITLE_TO_LAB = "検体";
    private static final String TITLE_TO_PHYSIO = "生体";
    private static final String TITLE_TO_BACTERIA = "細菌";
    private static final String ICON_FORWARD = "forwd_16.gif";
    private static final String ICON_BACK = "back_16.gif";
    private static final String TITLE_FROM_EDITOR = "エディタから発行...";

    // エディタ の組
    private AbstractStampEditor bacteria;
    private AbstractStampEditor baseCharge;
    private AbstractStampEditor diagnosis;
    private AbstractStampEditor general;
    private AbstractStampEditor injection;
    private AbstractStampEditor instraction;
    private AbstractStampEditor med;
    private AbstractStampEditor other;
    private AbstractStampEditor physiology;
    private AbstractStampEditor radiology;
    private AbstractStampEditor surgery;
    private AbstractStampEditor test;
    private AbstractStampEditor treatment;
//masuda^   テキストスタンプエディタ
    private AbstractStampEditor text;
//masuda$
    
    // 上記エディタを格納するカードパネル
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // 現在使用中のエディタ
    private AbstractStampEditor curEditor;
    
    // 辞書
    private HashMap<String, AbstractStampEditor> table;

    // StampBox とやりとりするボタン
    private JButton rightNew;       // 新規スタンプとして保存ボタン
    private JButton rightReplace;   // 上書きボタン
    private JButton leftImport;     // 取り込みボタン
    private JButton right6001;
    private JButton right6002;

    //-------------------------------------
    // StampBox と連携するためのオブジェクト
    //-------------------------------------

    // 右側のStampTreeで選択された Node
    // これが編集領域（エディタ）に取り込まれる
    private StampTreeNode selectedNode;
    private boolean isImport;
    
//masuda^   複数スタンプ
    // エディタで編集された Stamp
    private IInfoModel[] editorValue;
//masuda$
    
    // 上記編集値（束縛属性）をStampBoxへ通知するサポート
    private PropertyChangeSupport boundSupport = new PropertyChangeSupport(this);
    
    
    /**
     * 編集したスタンプオブジェクトを返す。
     * @return 編集したスタンプオブジェクト
     */
    public IInfoModel[] getEditorValue() {
        return editorValue;
    }
    
    /**
     * 編集値をセットする。この属性は束縛プロパティであり、リスナ（StampBoxPlugin）へ通知される。
     * @param value 編集されたスタンプ
     */
    public void setEditorValue(IInfoModel[] value) {
        isImport = false;
        editorValue = value;
        boundSupport.firePropertyChange(EditorSetPanel.EDITOR_VALUE_PROP, null, editorValue);
    }
    
    public StampTreeNode getSelectedNode() {
        return selectedNode;
    }
    
    /**
     * スタンプボックスで選択されているノード（スタンプ）をセットする。
     * @param node スタンプボックスで選択されているスタンプノード
     */
    public void setSelectedNode(StampTreeNode node) {
        selectedNode = node;
    }

    /**
     * EditorSet を終了する。
     */
    public void close() {
        if (curEditor!=null) {
            curEditor.dispose();
            curEditor.remopvePropertyChangeListener(AbstractStampEditor.EMPTY_DATA_PROP, this);
            curEditor.remopvePropertyChangeListener(AbstractStampEditor.VALIDA_DATA_PROP, this);
        }
    }
    
    /**
     * プロパティチェンジリスナを登録する。
     * @param prop プロパティ名
     * @param listener プロパティチェンジリスナ
     */
    @Override
    public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
        boundSupport.addPropertyChangeListener(prop, listener);
    }
    
    /**
     * プロパティチェンジリスナを削除する。
     * @param prop プロパティ名
     * @param listener プロパティチェンジリスナ
     */
    public void remopvePropertyChangeListener(String prop, PropertyChangeListener listener) {
        boundSupport.removePropertyChangeListener(prop, listener);
    }
    
    /**
     * スタンプボックスのタブが切り替えられた時、対応するエディタを show する。
     * @param show するエディタのエンティティ名
     */
    public void show(String entity) {

        rightNew.setEnabled(false);
        rightReplace.setEnabled(false);
        right6001.setEnabled(false);
        right6002.setEnabled(false);
        leftImport.setEnabled(false);

        // 現在エディタがあれば後始末する
        if (curEditor != null) {
            curEditor.dispose(); // setTable, searchResultTable が clear される
            curEditor.remopvePropertyChangeListener(AbstractStampEditor.EMPTY_DATA_PROP, this);
            curEditor.remopvePropertyChangeListener(AbstractStampEditor.VALIDA_DATA_PROP, this);
        }

        // ORCA及びパスタブの場合
        if (table.get(entity)==null) {
            return;
        }

        // 要求されたエディタに切り替える
        curEditor = table.get(entity);

        if (curEditor == diagnosis) {
            curEditor.setValue(null);
            
        } else {

            boolean visible = false;

            if (curEditor == test) {
                right6001.setText(TITLE_TO_PHYSIO);
                right6002.setText(TITLE_TO_BACTERIA);
                visible = true;

            } else if (curEditor == physiology) {
                right6001.setText(TITLE_TO_LAB);
                right6002.setText(TITLE_TO_BACTERIA);
                visible = true;

            } else if (curEditor == bacteria) {
                right6001.setText(TITLE_TO_LAB);
                right6002.setText(TITLE_TO_PHYSIO);
                visible = true;
            }
            
            right6001.setVisible(visible);
            right6002.setVisible(visible);

            //----------------------------------------------
            // 空のStamp(ModuleModel)をエディタに設定しておく
            //----------------------------------------------
            ModuleModel stamp = new ModuleModel();
            ModuleInfoBean stampInfo = stamp.getModuleInfoBean();
            stampInfo.setStampName(TITLE_FROM_EDITOR);
            stampInfo.setStampRole(IInfoModel.ROLE_P);
            stampInfo.setEntity(entity);
            
//masuda^   複数スタンプ
            curEditor.setValue(new ModuleModel[]{stamp});
//masuda$
        }

        // 最初にclearされるイベントを受けない
        curEditor.addPropertyChangeListener(AbstractStampEditor.EMPTY_DATA_PROP, this);
        curEditor.addPropertyChangeListener(AbstractStampEditor.VALIDA_DATA_PROP, this);
        
        cardLayout.show(cardPanel, entity);
        
//masuda    フォーカスを当てる
        curEditor.setFocusOnSearchTextFld();
    }
    
    /**
     * 編集中のスタンプの有効/無効の属性通知を受け、右向きボタンを制御する。
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        
        String prop = e.getPropertyName();
        
        if (prop.equals(AbstractStampEditor.VALIDA_DATA_PROP)) {

            // 有効か無効かで右矢印ボタンを制御する
            Boolean i = (Boolean) e.getNewValue();
            boolean valid = i.booleanValue();

            // 新規スタンプとして保存
            rightNew.setEnabled(valid);

            // 上書き保存 valid かつ imported!=null
            boolean diseaseEditor = "傷病名".equals(curEditor.getOrderName());
            rightReplace.setEnabled(valid && isImport && !diseaseEditor);

            // 600
            right6001.setEnabled(valid && (right6001.isVisible()));
            right6002.setEnabled(valid && (right6002.isVisible()));

        } else if (prop.equals(AbstractStampEditor.EMPTY_DATA_PROP)) {
//masuda^
            // 空なら取り込みはすべてdisable
            rightNew.setEnabled(false);
            rightReplace.setEnabled(false);
            right6001.setEnabled(false);
            right6002.setEnabled(false);
//masuda$
        }
    }
    
    /**
     * スタンプツリーで選択されたスタンプに応じて取り込みボタンを制御する。
     */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        
        StampTree tree = (StampTree) e.getSource();
        String treeEntity = tree.getTreeInfo().getEntity();

//masuda^   テキストスタンプエディタと病名エディタは利用可能にする
        if (treeEntity.equals(IInfoModel.ENTITY_PATH) ||
            treeEntity.equals(IInfoModel.ENTITY_ORCA)) {
//masuda$
            leftImport.setEnabled(false);
            setSelectedNode(null);
            return;
        }

        // ノードが葉で傷病名でない時取り込みボタン（左矢印）をenabled にする
        // またその時以外は選択ノード属性をnullにする
        StampTreeNode node =(StampTreeNode) tree.getLastSelectedPathComponent();
        boolean enabled = node != null && node.isLeaf()
                && ((ModuleInfoBean) node.getUserObject()).isSerialized();
        StampTreeNode selected = enabled ? node : null;
        
        leftImport.setEnabled(enabled);

        // 選択されている TreeNode を保存する
        setSelectedNode(selected);
    }

    //--------------------------
    // ボタンへListnerを登録する
    //--------------------------
    private void connect() {

        //--------------------------------------------------
        // 取り込みボタン　左矢印
        //--------------------------------------------------
        leftImport.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                // StampTreeで選択されているNodeを得る
                StampTreeNode node = getSelectedNode();
                if (node == null || !(node.getUserObject() instanceof ModuleInfoBean)) {
                    return;
                }

                // Nodeから StampInfo(UserObject)を得る
                final ModuleInfoBean stampInfo = (ModuleInfoBean) node.getUserObject();

                SwingWorker worker = new SwingWorker<StampModel, Void>() {

                    @Override
                    protected StampModel doInBackground() throws Exception {
                        //-----------------------------------------------------------
                        // stampId から StampModel(DBのレコード:EntityBean)をフェッチする
                        //-----------------------------------------------------------
//masuda^   シングルトン化
                        //StampDelegater sdl = new StampDelegater();
                        StampDelegater sdl = StampDelegater.getInstance();
//masuda$
                        StampModel stampModel = sdl.getStamp(stampInfo.getStampId());
                        return stampModel;
                    }

                    @Override
                    protected void done() {
                        try {
                            StampModel stampModel = get();
                            if (stampModel != null && curEditor != null) {
//masuda^   複数スタンプ
                                //----------------------------------------------------
                                // 取得したEntityの binary object をdecodeして Modelを得る
                                //----------------------------------------------------
                                IInfoModel model = (IInfoModel) BeanUtils.xmlDecode(stampModel.getStampBytes());
                                if (model != null) {
                                    // 病名の場合
                                    if ("diagnosis".equals(stampModel.getEntity())) {
                                        RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) model;
                                        curEditor.setValue(new RegisteredDiagnosisModel[]{rd});
                                    //　それ以外
                                    } else {
                                        ModuleModel stampToEdit = new ModuleModel();
                                        stampToEdit.setModel(model);
                                        stampToEdit.setModuleInfoBean(stampInfo);
                                        leftImport.setEnabled(false);
                                        curEditor.setValue(new ModuleModel[]{stampToEdit});
                                    }
                                }
                                isImport = true;
                            }
//masuda$
                        } catch (InterruptedException ex) {
                            System.err.println(ex);

                        } catch (ExecutionException ex) {
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(cardPanel),
                                                ex.getMessage(),
                                                ClientContext.getFrameTitle("Stamp取得"),
                                                JOptionPane.WARNING_MESSAGE);
                        }
                    }
                };

                worker.execute();
            }
        });

        //--------------------------------------------------
        // 新規スタンプとして保存ボタン　右矢印
        //--------------------------------------------------
        rightNew.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                // cureditor から値を取得し、自分自身のプロパティに設定する。
                // 束縛プロパティによりリスナのStampBoxへこの値が通知される。
//masuda^   複数スタンプ
                IInfoModel[] values = curEditor.getValue();
//masuda$
                setEditorValue(values);
                curEditor.setValue(null);
            }
        });

        //--------------------------------------------------
        // 置き換えスタンプボタン　右矢印
        //--------------------------------------------------
        rightReplace.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                // cureditor から値を取得し、自分自身のプロパティに設定する。
                // 束縛プロパティによりリスナへこの値が通知される。
//masuda^   複数スタンプ
                IInfoModel[] values = curEditor.getValue();

                if (values == null || values.length == 0) {
                    return;
                }
                
                // 病名以外
                if (!(values[0] instanceof RegisteredDiagnosisModel)) {
                    // 編集元のstampIdを取得する
                    IInfoModel[] oldValues = curEditor.getOldValue();
                    String stampId = null;
                    if (oldValues != null && oldValues.length > 0) {
                        ModuleModel stamp = (ModuleModel) oldValues[0];
                        stampId = stamp.getModuleInfoBean().getStampId();
                    }
                    // １個目はstampIdを引き継ぐ。その他は新規としてstampId = null
                    for (int i = 0; i < values.length; ++i) {
                        ModuleModel stamp = (ModuleModel) values[i];
                        if (i == 0) {
                            stamp.getModuleInfoBean().setStampId(stampId);
                        } else {
                            stamp.getModuleInfoBean().setStampId(null);
                        }
                    }
                }
                
                setEditorValue(values);
//masuda$
                curEditor.setValue(null);
            }
        });

        //--------------------------------------------------
        // 600 入れ替えスタンプボタン　右矢印
        //--------------------------------------------------
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                // cureditor から値を取得し、自分自身のプロパティに設定する。
                // 束縛プロパティによりリスナへこの値が通知される。
//masuda^   複数スタンプ
                IInfoModel[] values = curEditor.getValue();

                for (IInfoModel model : values) {
                    ModuleModel stamp = (ModuleModel) model;
                    ModuleInfoBean info = stamp.getModuleInfoBean();
                    info.setStampId(null); // 新規スタンプ

                    // entity 入れ替え
                    JButton btn = (JButton) ae.getSource();
                    String text = btn.getText();

                    if (text.equals(TITLE_TO_PHYSIO)) {
                        info.setEntity(IInfoModel.ENTITY_PHYSIOLOGY_ORDER);

                    } else if (text.equals(TITLE_TO_BACTERIA)) {
                        info.setEntity(IInfoModel.ENTITY_BACTERIA_ORDER);

                    } else if (text.equals(TITLE_TO_LAB)) {
                        info.setEntity(IInfoModel.ENTITY_LABO_TEST);
                    }
                }

                setEditorValue(values);
//masuda$
                rightNew.setEnabled(false);
                rightReplace.setEnabled(false);
                curEditor.setValue(null);
            }
        };

        right6001.addActionListener(al);
        right6002.addActionListener(al);
    }
    
    /**
     * GUI コンポーネントを生成する。
     */
    private void initComponent() {
        
        // 編集したスタンプをボックスへ登録する右向きボタンを生成する
        rightNew = new JButton(TITLE_NEW, ClientContext.getImageIcon(ICON_FORWARD));
        rightNew.setEnabled(false);

        // 編集したスタンプを上書きする右向きボタンを生成する
        rightReplace = new JButton(TITLE_REPLACE, ClientContext.getImageIcon(ICON_FORWARD));
        rightReplace.setEnabled(false);

        // 診区 600 （検体、生体、細菌）入れ替えボタン
        right6001 = new JButton(ClientContext.getImageIcon(ICON_FORWARD));
        right6002 = new JButton(ClientContext.getImageIcon(ICON_FORWARD));
        right6001.setVisible(false);
        right6002.setVisible(false);
        
        // スタンプボックスのスタンプをセットテーブルへ取り込む左向きのボタンを生成する
        leftImport = new JButton(TITLE_IMPORT, ClientContext.getImageIcon(ICON_BACK));
        leftImport.setEnabled(false);

        //-----------------------------------
        // 個別のエディタを生成する
        //-----------------------------------
        bacteria = new BaseEditor(IInfoModel.ENTITY_BACTERIA_ORDER, false);
        baseCharge = new BaseEditor(IInfoModel.ENTITY_BASE_CHARGE_ORDER, false);
        diagnosis = new DiseaseEditor(false);
        general = new BaseEditor(IInfoModel.ENTITY_GENERAL_ORDER, false);
        injection = new InjectionEditor(IInfoModel.ENTITY_INJECTION_ORDER, false);
        instraction = new BaseEditor(IInfoModel.ENTITY_INSTRACTION_CHARGE_ORDER, false);
        med = new RpEditor(IInfoModel.ENTITY_MED_ORDER, false);
        other = new BaseEditor(IInfoModel.ENTITY_OTHER_ORDER, false);
        physiology = new BaseEditor(IInfoModel.ENTITY_PHYSIOLOGY_ORDER, false);
        radiology = new RadEditor(IInfoModel.ENTITY_RADIOLOGY_ORDER, false);
        surgery = new BaseEditor(IInfoModel.ENTITY_SURGERY_ORDER, false);
        test = new BaseEditor(IInfoModel.ENTITY_LABO_TEST, false);
        treatment = new BaseEditor(IInfoModel.ENTITY_TREATMENT, false);
//masuda^   テキストスタンプエディタ
        text = new TextStampEditor("text", false);
//masuda$
        
        // Hashテーブルに登録し show(entity) で使用する
        table = new HashMap<String, AbstractStampEditor>();
        table.put(IInfoModel.ENTITY_BACTERIA_ORDER, bacteria);
        table.put(IInfoModel.ENTITY_BASE_CHARGE_ORDER, baseCharge);
        table.put(IInfoModel.ENTITY_DIAGNOSIS, diagnosis);
        table.put(IInfoModel.ENTITY_GENERAL_ORDER, general);
        table.put(IInfoModel.ENTITY_INJECTION_ORDER, injection);
        table.put(IInfoModel.ENTITY_INSTRACTION_CHARGE_ORDER, instraction);
        table.put(IInfoModel.ENTITY_MED_ORDER, med);
        table.put(IInfoModel.ENTITY_OTHER_ORDER, other);
        table.put(IInfoModel.ENTITY_PHYSIOLOGY_ORDER, physiology);
        table.put(IInfoModel.ENTITY_RADIOLOGY_ORDER, radiology);
        table.put(IInfoModel.ENTITY_SURGERY_ORDER, surgery);
        table.put(IInfoModel.ENTITY_LABO_TEST, test);
        table.put(IInfoModel.ENTITY_TREATMENT, treatment);
//masuda^   テキストスタンプエディタ
        table.put("text", text);
//masuda$

        //-----------------------------------
        // カードパネルにエディタを追加する
        //-----------------------------------
        cardPanel.add(bacteria.getView(), IInfoModel.ENTITY_BACTERIA_ORDER);
        cardPanel.add(baseCharge.getView(), IInfoModel.ENTITY_BASE_CHARGE_ORDER);
        cardPanel.add(diagnosis.getView(), IInfoModel.ENTITY_DIAGNOSIS);
        cardPanel.add(general.getView(), IInfoModel.ENTITY_GENERAL_ORDER);
        cardPanel.add(injection.getView(), IInfoModel.ENTITY_INJECTION_ORDER);
        cardPanel.add(instraction.getView(), IInfoModel.ENTITY_INSTRACTION_CHARGE_ORDER);
        cardPanel.add(med.getView(), IInfoModel.ENTITY_MED_ORDER);
        cardPanel.add(other.getView(), IInfoModel.ENTITY_OTHER_ORDER);
        cardPanel.add(physiology.getView(), IInfoModel.ENTITY_PHYSIOLOGY_ORDER);
        cardPanel.add(radiology.getView(), IInfoModel.ENTITY_RADIOLOGY_ORDER);
        cardPanel.add(surgery.getView(), IInfoModel.ENTITY_SURGERY_ORDER);
        cardPanel.add(test.getView(), IInfoModel.ENTITY_LABO_TEST);
        cardPanel.add(treatment.getView(), IInfoModel.ENTITY_TREATMENT);
//masuda^   テキストスタンプエディタ
        cardPanel.add(text.getView(), "text");
//masuda$
        
        // StampBox との間にある矢印ボタンパネル
        JPanel btnPanel = new JPanel();
        BoxLayout box = new BoxLayout(btnPanel, BoxLayout.Y_AXIS);
        btnPanel.setLayout(box);
        btnPanel.add(Box.createVerticalStrut(70));
        btnPanel.add(right6001);
        btnPanel.add(right6002);
        btnPanel.add(Box.createVerticalStrut(30));
        btnPanel.add(rightNew);
        btnPanel.add(rightReplace);
        btnPanel.add(leftImport);
        btnPanel.add(Box.createVerticalGlue());

        // 配置する
        this.setLayout(new BorderLayout(0, 0));
        this.add(cardPanel, BorderLayout.CENTER);
        this.add(btnPanel, BorderLayout.EAST);
    }

    public AbstractStampEditor getCurrentEditor() {
        return curEditor;
    }

    /** EditorSetPanel を生成する。 */
    public EditorSetPanel() {
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        initComponent();
        connect();
    }
}
