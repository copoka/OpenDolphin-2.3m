package open.dolphin.client;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.project.Project;

/**
 *
 * @author Kazushi Minagawa.
 */
public class PatientInspector {
    
    // 個々のインスペクタ
    // 患者基本情報
    private BasicInfoInspector basicInfoInspector;
    // 来院歴
    private PatientVisitInspector patientVisitInspector;
    // 患者メモ
    private MemoInspector memoInspector;
    // 文書履歴
    private DocumentHistory docHistory;
    // アレルギ
    private AllergyInspector allergyInspector;
    // 身長体重
    private PhysicalInspector physicalInspector;
    
    // インスペクタを格納するタブペイン View
    private JTabbedPane tabbedPane;
    // このクラスのコンテナパネル View
    private JPanel container;
    // Context このインスペクタの親コンテキスト
    private ChartImpl context;
    
    private boolean bMemo;
    private boolean bAllergy;
    private boolean bPhysical;
    private boolean bCalendar;
    
//masuda^
    // 検査履歴
    private ExamHistory examHistory;
    // 検索結果
    private SearchResultInspector searchResult;

    // 検査履歴タブの色を検査の有無に応じて赤黒に変えるため。ExamHistroyから利用
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
//masuda$
    
    /**
     * 患者インスペクタクラスを生成する。
     *
     * @param context インスペクタの親コンテキスト
     */
    public PatientInspector(ChartImpl context) {
        
        // このインスペクタが格納される Chart Object
        this.context = context;
        
        // GUI を初期化する
        initComponents();
    }
    
    public void dispose() {
        // List をクリアする
        docHistory.clear();
        allergyInspector.clear();
        physicalInspector.clear();
        memoInspector.save();
//masuda^
        // 検査履歴、検索結果
        examHistory.clear();
        searchResult.clear();
//masuda$
    }
    
    /**
     * コンテキストを返す。
     */
    public ChartImpl getContext() {
        return context;
    }
    
    /**
     * コンテキストを設定する。
     */
    public void setContext(ChartImpl context) {
        this.context = context;
    }
    
    /**
     * 患者カルテを返す。
     * @return  患者カルテ
     */
    public KarteBean getKarte() {
        return context.getKarte();
    }
    
    /**
     * 患者を返す。
     * @return 患者
     */
    public PatientModel getPatient() {
        return context.getKarte().getPatientModel();
    }
    
    /**
     * 基本情報インスペクタを返す。
     * @return 基本情報インスペクタ
     */
    public BasicInfoInspector getBasicInfoInspector() {
        return basicInfoInspector;
    }
    
    /**
     * 来院歴インスペクタを返す。
     * @return 来院歴インスペクタ
     */
    public PatientVisitInspector getPatientVisitInspector() {
        return patientVisitInspector;
    }
    
    /**
     * 患者メモインスペクタを返す。
     * @return 患者メモインスペクタ
     */
    public MemoInspector getMemoInspector() {
        return memoInspector;
    }
    
    /**
     * 文書履歴インスペクタを返す。
     * @return 文書履歴インスペクタ
     */
    public DocumentHistory getDocumentHistory() {
        return docHistory;
    }
    
    /**
     * レイアウトのためにインスペクタのコンテナパネルを返す。
     * @return インスペクタのコンテナパネル
     */
    public JPanel getPanel() {
        return container;
    }
    
    
    private void initComponents() {
        
        // 来院歴
        String pvtTitle = ClientContext.getString("patientInspector.pvt.title");
        // 文書履歴
        String docHistoryTitle = ClientContext.getString("patientInspector.docHistory.title");
        // アレルギ
        String allergyTitle = ClientContext.getString("patientInspector.allergy.title");
        // 身長体重
        String physicalTitle = ClientContext.getString("patientInspector.physical.title");
        // メモ
        String memoTitle = ClientContext.getString("patientInspector.memo.title");
        
        String topInspector = Project.getString("topInspector", "メモ");
        String secondInspector = Project.getString("secondInspector", "カレンダ");
        String thirdInspector = Project.getString("thirdInspector", "文書履歴");
        String forthInspector = Project.getString("forthInspector", "アレルギ");
        
        // 各インスペクタを生成する
        basicInfoInspector = new BasicInfoInspector(context);
        patientVisitInspector = new PatientVisitInspector(context);
        memoInspector = new MemoInspector(context);
        //memoInspector.getPanel().setBorder(BorderFactory.createEtchedBorder());
        docHistory = new DocumentHistory(getContext());
        allergyInspector = new AllergyInspector(context);
        physicalInspector = new PhysicalInspector(context);
        
        // タブパネルへ格納する(文書履歴、健康保険、アレルギ、身長体重はタブパネルで切り替え表示する)
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(docHistoryTitle, docHistory.getPanel());
        
//masuda^   検査歴パネルを追加
        String examHistoryTitle= ExamHistory.ExamHistoryTitle;
        examHistory = new ExamHistory(this);
        // 検査歴はタブパネルに入れておく
        tabbedPane.addTab(examHistoryTitle, examHistory.getPanel());
        // タブ変更時、DocumentHistoryの文書タイプがカルテでなかったらカルテに変更する。
        tabbedPane.addChangeListener(new ChangeListener() {

            private static final String docTypeKarte = IInfoModel.DOCTYPE_KARTE;

            @Override
            public void stateChanged(ChangeEvent e) {

                String newDocType = docHistory.getExtractionContent();
                JTabbedPane jtab = (JTabbedPane) e.getSource();
                if (jtab.getSelectedComponent() == examHistory.getPanel()) {
                    // 文書タイプがカルテでなかったらカルテに切り替える
                    if (!docTypeKarte.equals(newDocType)) {
                        docHistory.setExtractionContent(docTypeKarte);
                    }
                }
            }
        });
        // 検索結果もタブパネルに
        searchResult = new SearchResultInspector(this);
        tabbedPane.addTab(SearchResultInspector.SearchResultTitle, searchResult.getPanel());
        docHistory.setSearchResult(searchResult);

        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // 左側のレイアウトを行う
        container.add(basicInfoInspector.getPanel());   // basicInfoも左側に入れる。必ずトップ
        layoutRow(container, topInspector);
        layoutRow(container, secondInspector);
        layoutRow(container, thirdInspector);
        layoutRow(container, forthInspector);
//masuda$
        
        // 左側にレイアウトされなかったものをタブに格納する
        if (!bMemo) {
            tabbedPane.addTab(memoTitle, memoInspector.getPanel());
        }
        
        if (!bCalendar) {
            tabbedPane.addTab(pvtTitle, patientVisitInspector.getPanel());
        }
        
        if (!bAllergy) {
            tabbedPane.addTab(allergyTitle, allergyInspector.getPanel());
        }
        
        if (!bPhysical) {
            tabbedPane.addTab(physicalTitle, physicalInspector.getPanel());
        }
    }
    
    private void layoutRow(JPanel content, String itype) {
        
        if (itype.equals("メモ")) {
//masuda^   TitledBorderを復活
           memoInspector.getPanel().setBorder(BorderFactory.createTitledBorder("メモ"));
//masuda$
           content.add(memoInspector.getPanel());
           bMemo = true;
        
        } else if (itype.equals("カレンダ")) {
//masuda^
            patientVisitInspector.setTitle();
//masuda$
            content.add(patientVisitInspector.getPanel());
            bCalendar = true;
        
        } else if (itype.equals("文書履歴")) {
            content.add(tabbedPane);
        
        } else if (itype.equals("アレルギ")) {
            content.add(allergyInspector.getPanel());
            bAllergy = true;
        
        } else if (itype.equals("身長体重")) {
            content.add(physicalInspector.getPanel());
            bPhysical = true;
        }
    }
}