package open.dolphin.client;

import java.awt.*;
import java.awt.event.*;
import java.awt.im.InputSubset;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import open.dolphin.delegater.UserDelegater;
import open.dolphin.helper.SimpleWorker;
import open.dolphin.infomodel.*;
import open.dolphin.project.Project;
import open.dolphin.table.ListTableModel;
import open.dolphin.table.StripeTableCellRenderer;
import open.dolphin.util.HashUtil;
import org.apache.log4j.Logger;

/**
 * AddUserPlugin
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class AddUserImpl extends AbstractMainTool implements AddUser {
    
    private static final String TITLE = "ユーザ管理";
    private static final String FACILITY_INFO = "施設情報";
    private static final String ADD_USER = "ユーザ登録";
    private static final String LIST_USER = "ユーザリスト";
    private static final String FACILITY_SUCCESS_MSG = "施設情報を更新しました。";
    private static final String ADD_USER_SUCCESS_MSG = "ユーザを登録しました。";
    private static final String DELETE_USER_SUCCESS_MSG = "ユーザを削除しました。";
    private static final String DELETE_OK_USER_ = "選択したユーザを削除します";
    
    private JFrame frame;
    private Logger logger;

    // timerTask 関連
    private SimpleWorker worker;
    private javax.swing.Timer taskTimer;
    private ProgressMonitor monitor;
    private int delayCount;
    private int maxEstimation = 120*1000;   // 120 秒
    private int delay = 300;               // 300 mmsec
    
    /** Creates a new instance of AddUserService */
    public AddUserImpl() {
        setName(TITLE);
        logger = ClientContext.getBootLogger();
    }
    
    public void setFrame(JFrame frame) {
        this.frame = frame;
//masuda^    アイコン設定
        ClientContext.setDolphinIcon(frame);
//masuda$
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    @Override
    public void start() {

        Runnable awt = new Runnable() {

            @Override
            public void run() {

                // Super Class で Frame を初期化する
                String title = ClientContext.getFrameTitle(getName());
                JFrame frm = new JFrame(title);
                setFrame(frm);
                frm.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frm.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        stop();
                    }
                });

                // Component を生成する
                AddUserPanel ap = new AddUserPanel();
                FacilityInfoPanel fp = new FacilityInfoPanel();
                UserListPanel mp = new UserListPanel();
                JTabbedPane tabbedPane = new JTabbedPane();
                
                // 順番変更は可能か
                tabbedPane.addTab(ADD_USER, ap);
                tabbedPane.addTab(FACILITY_INFO, fp);
                tabbedPane.addTab(LIST_USER, mp);
                fp.get();

                // Frame に加える
                getFrame().getContentPane().add(tabbedPane, BorderLayout.CENTER);

                getFrame().pack();
                Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                int x = (size.width - getFrame().getPreferredSize().width) / 2;
                int y = (size.height - getFrame().getPreferredSize().height) / 3;
                getFrame().setLocation(x, y);
                
                getFrame().setVisible(true);

            }
        };
        
        SwingUtilities.invokeLater(awt);
    }
    
    @Override
    public void stop() {
        getFrame().setVisible(false);
        getFrame().dispose();
    }
    
    public void toFront() {
        if (getFrame() != null) {
            getFrame().toFront();
        }
    }
    
    /**
     * 施設（医療機関）情報を変更するクラス。
     */
    protected class FacilityInfoPanel extends JPanel {
        
        // 施設情報フィールド
        private JTextField facilityId;
        private JTextField facilityName;
        private JTextField zipField1;
        private JTextField zipField2;
        private JTextField addressField;
        private JTextField areaField;
        private JTextField cityField;
        private JTextField numberField;
        private JTextField areaFieldFax;
        private JTextField cityFieldFax;
        private JTextField numberFieldFax;
        private JTextField urlField;
        
        // 更新等のボタン
        private JButton updateBtn;
        private JButton clearBtn;
        private JButton closeBtn;
        private boolean hasInitialized;
        
        public FacilityInfoPanel() {
            
            // GUI生成
            FocusAdapter imeOn = new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    JTextField tf = (JTextField) event.getSource();
                    tf.getInputContext().setCharacterSubsets(
                            new Character.Subset[] { InputSubset.KANJI });
                }
            };
            
            FocusAdapter imeOff = new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    JTextField tf = (JTextField) event.getSource();
                    tf.getInputContext().setCharacterSubsets(null);
                }
            };
            
            DocumentListener dl = new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                }
                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkButton();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    checkButton();
                }
            };
//masuda^
            //facilityId = GUIFactory.createTextField(10, null, imeOff, dl);
            facilityId = GUIFactory.createTextField(20, null, imeOff, dl);
//masuda$
            facilityId.setEditable(false);
            facilityId.setEnabled(false);
            facilityName = GUIFactory.createTextField(30, null, imeOn, dl);
            zipField1 = GUIFactory.createTextField(3, null, imeOff, dl);
            zipField2 = GUIFactory.createTextField(3, null, imeOff, dl);
            addressField = GUIFactory.createTextField(30, null, imeOn, dl);
            areaField = GUIFactory.createTextField(3, null, imeOff, dl);
            cityField = GUIFactory.createTextField(3, null, imeOff, dl);
            numberField = GUIFactory.createTextField(3, null, imeOff, dl);
            areaFieldFax = GUIFactory.createTextField(3, null, imeOff, dl);
            cityFieldFax = GUIFactory.createTextField(3, null, imeOff, dl);
            numberFieldFax = GUIFactory.createTextField(3, null, imeOff, dl);
            urlField = GUIFactory.createTextField(30, null, imeOn, dl);
            
            updateBtn = new JButton("更新");
            updateBtn.setEnabled(false);
            //updateBtn.setMnemonic('U');
            updateBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    update();
                }
            });
            
            clearBtn = new JButton("戻す");
            clearBtn.setEnabled(false);
            clearBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    get();
                }
            });
            
            closeBtn = new JButton("閉じる");
            closeBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    stop();
                }
            });
            
            // レイアウト
            JPanel content = new JPanel(new GridBagLayout());
            
            int x = 0;
            int y = 0;
            JLabel label = new JLabel("医療機関コード:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, facilityId, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel("医療機関名:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, facilityName, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel("郵便番号:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, GUIFactory.createZipCodePanel(zipField1, zipField2), x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel("住  所:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, addressField, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel("電話番号:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, GUIFactory.createPhonePanel(areaField, cityField, numberField), x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel("FAX番号:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, GUIFactory.createPhonePanel(areaFieldFax, cityFieldFax, numberFieldFax), x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel("URL:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, urlField, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel(" ", SwingConstants.RIGHT);
            constrain(content, label, x, y, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.EAST);
            
            JPanel btnPanel;
            if (isMac()) {
                btnPanel = GUIFactory.createCommandButtonPanel(new JButton[]{clearBtn, closeBtn, updateBtn});
            } else {
                btnPanel = GUIFactory.createCommandButtonPanel(new JButton[]{updateBtn, clearBtn, closeBtn});
            }
            
            this.setLayout(new BorderLayout(0, 11));
            this.add(content, BorderLayout.CENTER);
            this.add(btnPanel, BorderLayout.SOUTH);
            this.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        }
        
        public void get() {
            
            UserModel user = Project.getUserModel();
            FacilityModel facility = user.getFacilityModel();
            
            if (facility.getFacilityId() != null) {
                facilityId.setText(facility.getFacilityId());
            }
            
            if (facility.getFacilityName() != null) {
                facilityName.setText(facility.getFacilityName());
            }
            
            if (facility.getZipCode() != null) {
                String val = facility.getZipCode();
                try {
                    StringTokenizer st = new StringTokenizer(val, "-");
                    if (st.hasMoreTokens()) {
                        zipField1.setText(st.nextToken());
                        zipField2.setText(st.nextToken());
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            
            if (facility.getAddress() != null) {
                addressField.setText(facility.getAddress());
            }
            
            if (facility.getTelephone() != null) {
                String val = facility.getTelephone();
                try {
                    String[] cmp = val.split("-");
                    areaField.setText(cmp[0]);
                    cityField.setText(cmp[1]);
                    numberField.setText(cmp[2]);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }

            if (facility.getFacsimile() != null) {
                String val = facility.getFacsimile();
                try {
                    String[] cmp = val.split("-");
                    areaFieldFax.setText(cmp[0]);
                    cityFieldFax.setText(cmp[1]);
                    numberFieldFax.setText(cmp[2]);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            
            if (facility.getUrl() != null) {
                urlField.setText(facility.getUrl());
            }
            
            hasInitialized = true;
        }
        
        private void checkButton() {
            
            if (!hasInitialized) {
                return;
            }
            
            boolean nameEmpty = facilityName.getText().trim().isEmpty();
            boolean fidEmpty = facilityId.getText().trim().isEmpty();
            boolean zip1Empty = zipField1.getText().trim().isEmpty();
            boolean zip2Empty = zipField2.getText().trim().isEmpty();
            boolean addressEmpty = addressField.getText().trim().isEmpty();
            boolean areaEmpty = areaField.getText().trim().isEmpty();
            boolean cityEmpty = cityField.getText().trim().isEmpty();
            boolean numberEmpty = numberField.getText().trim().isEmpty();
            
            if (nameEmpty && fidEmpty && zip1Empty && zip2Empty && addressEmpty
                    && areaEmpty && cityEmpty && numberEmpty) {
                
                if (clearBtn.isEnabled()) {
                    clearBtn.setEnabled(false);
                }
            } else {
                if (!clearBtn.isEnabled()) {
                    clearBtn.setEnabled(true);
                }
            }
            
            // 施設名フィールドが空の場合
            if (nameEmpty) {
                if (updateBtn.isEnabled()) {
                    updateBtn.setEnabled(false);
                }
                return;
            }
            
            // 施設名フィールドは空ではない
            if (!updateBtn.isEnabled()) {
                updateBtn.setEnabled(true);
            }
        }
        
        private void update() {
            
            final UserModel user = Project.getUserModel();
            // ディタッチオブジェクトが必要である
            FacilityModel facility = user.getFacilityModel();
            
            // 医療機関コードは変更できない
            
            // 施設名
            String val = facilityName.getText().trim();
            if (!val.equals("")) {
                facility.setFacilityName(val);
            }
            
            // 郵便番号
            val = zipField1.getText().trim();
            String val2 = zipField2.getText().trim();
            if ((!val.equals("")) && (!val2.equals(""))) {
                facility.setZipCode(val + "-" + val2);
            }
            
            // 住所
            val = addressField.getText().trim();
            if (!val.equals("")) {
                facility.setAddress(val);
            }
            
            // 電話番号
            val = areaField.getText().trim();
            val2 = cityField.getText().trim();
            String val3 = numberField.getText().trim();
            if ((!val.equals("")) && (!val2.equals("")) && (!val3.equals(""))) {
                facility.setTelephone(val + "-" + val2 + "-" + val3);
            }

            // Fax番号
            val = areaFieldFax.getText().trim();
            val2 = cityFieldFax.getText().trim();
            val3 = numberFieldFax.getText().trim();
            if ((!val.equals("")) && (!val2.equals("")) && (!val3.equals(""))) {
                facility.setFacsimile(val + "-" + val2 + "-" + val3);
            }
            
            // URL
            val = urlField.getText().trim();
            if (!val.equals("")) {
                facility.setUrl(val);
            }
            
            // 登録日
            // 変更しない
            
            // タスクを実行する
//masuda^   シングルトン化
            //final UserDelegater udl = new UserDelegater();
            final UserDelegater udl = UserDelegater.getInstance();
//masuda$
            worker = new SimpleWorker<Boolean, Void>() {
        
                @Override
                protected Boolean doInBackground() throws Exception {
                    logger.debug("updateUser doInBackground");
                    int cnt = udl.updateFacility(user);
                    return cnt > 0;
                }
                
                @Override
                protected void succeeded(Boolean result) {
                    logger.debug("updateUser succeeded");
                    JOptionPane.showMessageDialog(getFrame(),
                            FACILITY_SUCCESS_MSG,
                            ClientContext.getFrameTitle(getName()),
                            JOptionPane.INFORMATION_MESSAGE);
                }

                @Override
                protected void cancelled() {
                    logger.debug("updateUser cancelled");
                }

                @Override
                protected void failed(java.lang.Throwable cause) {
                    JOptionPane.showMessageDialog(getFrame(),
                                cause.getMessage(),
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.WARNING_MESSAGE);
                    logger.warn("updateUser failed");
                    logger.warn(cause.getCause());
                    logger.warn(cause.getMessage());
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

            Component c = getFrame();
            String message = null;
            String note = ClientContext.getString("task.default.updateMessage");
            maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            delay = ClientContext.getInt("task.default.delay");

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
    }
    
    /**
     * ユーザリストを取得するクラス。名前がいけない。
     */
    protected class UserListPanel extends JPanel {
        
        private ListTableModel<UserModel> tableModel;
        private JTable table;
        private JButton getButton;
        private JButton deleteButton;
        private JButton cancelButton;
        
        public UserListPanel() {
            
            String[] columns = new String[] { "ユーザID", "姓", "名", "医療資格", "診療科" };
            
            // ユーザテーブル
            tableModel = new ListTableModel<UserModel>(columns, 7) {
                
                // 編集不可
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
                
                // オブジェクトをテーブルに表示する
                @Override
                public Object getValueAt(int row, int col) {
                    
                    UserModel entry = getObject(row);
                    if (entry == null) {
                        return null;
                    }
                    
                    String ret = null;
                    
                    switch (col) {
                        
                        case 0:
                            ret = entry.idAsLocal();
                            break;
                            
                        case 1:
                            ret = entry.getSirName();
                            break;
                            
                        case 2:
                            ret = entry.getGivenName();
                            break;
                            
                        case 3:
                            ret = entry.getLicenseModel().getLicenseDesc();
                            break;
                            
                        case 4:
                            ret = entry.getDepartmentModel().getDepartmentDesc();
                            break;
                    }
                    return ret;
                }
            };
            
            table = new JTable(tableModel);
//masuda^   ストライプテーブル
            StripeTableCellRenderer renderer = new StripeTableCellRenderer();
            renderer.setTable(table);
            renderer.setDefaultRenderer();
//masuda$
            // Selection を設定する
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSelectionAllowed(true);
            table.setToolTipText(DELETE_OK_USER_);
            
            ListSelectionModel m = table.getSelectionModel();
            m.addListSelectionListener(new ListSelectionListener() {
                
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting() == false) {
                        // 削除ボタンをコントロールする
                        // 医療資格が other 以外は削除できない
                        int index = table.getSelectedRow();
                        UserModel entry = tableModel.getObject(index);
                        if (entry!=null) {
                            controleDelete(entry);
                        }
                    }
                }
            });
            
            // Layout
            JScrollPane scroller = new JScrollPane(table,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroller.getViewport().setPreferredSize(new Dimension(480,200));
            
            getButton = new JButton("ユーザリスト");
            getButton.setEnabled(true);
            //getButton.setMnemonic('L');
            getButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getUsers();
                }
            });
            
            deleteButton = new JButton("削除");
            deleteButton.setEnabled(false);
            //deleteButton.setMnemonic('D');
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteUser();
                }
            });
            deleteButton.setToolTipText(DELETE_OK_USER_);
            
            cancelButton = new JButton("閉じる");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    stop();
                }
            });
            //cancelButton.setMnemonic('C');
            
            JPanel btnPanel;
            if (isMac()) {
                btnPanel = GUIFactory.createCommandButtonPanel(new JButton[]{deleteButton, cancelButton, getButton});
            } else {
                btnPanel = GUIFactory.createCommandButtonPanel(new JButton[]{getButton, deleteButton, cancelButton});
            }
            this.setLayout(new BorderLayout(0, 17));
            this.add(scroller, BorderLayout.CENTER);
            this.add(btnPanel, BorderLayout.SOUTH);
            this.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        }
        
        /**
         * 医療資格が other 以外は削除できない。
         * @param user
         */
        private void controleDelete(UserModel user) {          
            boolean isMe = (user.getId() == Project.getUserModel().getId());
            deleteButton.setEnabled(!isMe);
        }
        
        /**
         * 施設内の全ユーザを取得する。
         */
        private void getUsers() {

//masuda^   シングルトン化
            //final UserDelegater udl = new UserDelegater();
            final UserDelegater udl = UserDelegater.getInstance();
//masuda$
            worker = new SimpleWorker<List<UserModel>, Void>() {
        
                @Override
                protected List<UserModel> doInBackground() throws Exception {
                    logger.debug("getUsers doInBackground");
                    List<UserModel> result = udl.getAllUser();
                    return result;
                }
                
                @Override
                protected void succeeded(List<UserModel> results) {
                    logger.debug("getUsers succeeded");
                    tableModel.setDataProvider(results);
                }

                @Override
                protected void cancelled() {
                    logger.debug("getUsers cancelled");
                }

                @Override
                protected void failed(java.lang.Throwable cause) {
                    JOptionPane.showMessageDialog(getFrame(),
                                cause.getMessage(),
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.WARNING_MESSAGE);
                    logger.warn("getUsers failed");
                    logger.warn(cause.getCause());
                    logger.warn(cause.getMessage());
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
            
            Component c = getFrame();
            String message = null;
            String note = ClientContext.getString("task.default.searchMessage");
            maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            delay = ClientContext.getInt("task.default.delay");

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
         * 選択したユーザを削除する。
         *
         */
        private void deleteUser() {
            
            int row = table.getSelectedRow();
            UserModel entry = tableModel.getObject(row);
            if (entry == null) {
                return;
            }
            
            //
            // 作成したドキュメントも削除するかどうかを選ぶ
            //
            boolean deleteDoc = true;
            if (entry.getLicenseModel().getLicense().equals("doctor")) {
                deleteDoc = false;
            }
            
//masuda^   シングルトン化
            //final UserDelegater udl = new UserDelegater();
            final UserDelegater udl = UserDelegater.getInstance();
//masuda$
            final String deleteId = entry.getUserId();
            
            worker = new SimpleWorker<List<UserModel>, Void>() {
        
                @Override
                protected List<UserModel> doInBackground() throws Exception {
                    logger.debug("deleteUser doInBackground");
                    List<UserModel> result = null;
                    if (udl.deleteUser(deleteId) > 0) {
                        result = udl.getAllUser();
                    } 
                    return result;
                }
                
                @Override
                protected void succeeded(List<UserModel> results) {
                    logger.debug("deleteUser succeeded");
                    tableModel.setDataProvider(results);
                    JOptionPane.showMessageDialog(getFrame(),
                            DELETE_USER_SUCCESS_MSG,
                            ClientContext.getFrameTitle(getName()),
                            JOptionPane.INFORMATION_MESSAGE);
                }

                @Override
                protected void cancelled() {
                    logger.debug("deleteUser cancelled");
                }

                @Override
                protected void failed(java.lang.Throwable cause) {
                    JOptionPane.showMessageDialog(getFrame(),
                                cause.getMessage(),
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.WARNING_MESSAGE);
                    logger.warn("deleteUser failed");
                    logger.warn(cause.getCause());
                    logger.warn(cause.getMessage());
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

            Component c = getFrame();
            String message = null;
            String note = ClientContext.getString("task.default.deleteMessage");
            maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            delay = ClientContext.getInt("task.default.delay");
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
    }
    
    /**
     * 施設内ユーザ登録クラス。
     */
    protected class AddUserPanel extends JPanel {
        
        private JTextField uid; // 利用者ID
        private JPasswordField userPassword1; // パスワード
        private JPasswordField userPassword2; // パスワード
        private JTextField sn; // 姓
        private JTextField givenName; // 名
        // private String cn; // 氏名(sn & ' ' & givenName)
        private LicenseModel[] licenses; // 職種(MML0026)
        private JComboBox licenseCombo;
        private DepartmentModel[] depts; // 診療科(MML0028)
        private JComboBox deptCombo;
        // private String authority; // LASに対する権限(admin:管理者,user:一般利用者)
        private JTextField emailField; // メールアドレス
        
        // JTextField description;
        private JButton okButton;
        private JButton cancelButton;
        
        private boolean ok;
        
        // UserId と Password の長さ
        private int[] userIdLength; // min,max
        private int[] passwordLength; // min,max
        private String idPassPattern;
        private String usersRole; // user に与える role 名
        
        public AddUserPanel() {
            
            userIdLength = ClientContext.getIntArray("addUser.userId.length");
            passwordLength = ClientContext.getIntArray("addUser.password.length");
            usersRole = ClientContext.getString("addUser.user.roleName");
            idPassPattern = ClientContext.getString("addUser.pattern.idPass");
            
            FocusAdapter imeOn = new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    JTextField tf = (JTextField) event.getSource();
                    tf.getInputContext().setCharacterSubsets(
                            new Character.Subset[] { InputSubset.KANJI });
                }
            };
            
            FocusAdapter imeOff = new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    JTextField tf = (JTextField) event.getSource();
                    tf.getInputContext().setCharacterSubsets(null);
                }
            };
            
            // DocumentListener
            DocumentListener dl = new DocumentListener() {
                
                @Override
                public void changedUpdate(DocumentEvent e) {
                }
                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkButton();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    checkButton();
                }
            };
            
            uid = GUIFactory.createTextField(10, null, imeOff, dl);
            uid.setDocument(new RegexConstrainedDocument(idPassPattern));
            uid.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    userPassword1.requestFocusInWindow();
                }
            });
            
            userPassword1 = GUIFactory.createPassField(10, null, imeOff, dl);
            userPassword1.setDocument(new RegexConstrainedDocument(idPassPattern));
            userPassword1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    userPassword2.requestFocusInWindow();
                }
            });
            
            userPassword2 = GUIFactory.createPassField(10, null, imeOff, dl);
            userPassword2.setDocument(new RegexConstrainedDocument(idPassPattern));
            userPassword2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sn.requestFocusInWindow();
                }
            });
            
            sn = GUIFactory.createTextField(10, null, imeOn, dl);
            sn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    givenName.requestFocusInWindow();
                }
            });
            
            givenName = GUIFactory.createTextField(10, null, imeOn, dl);
            givenName.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    emailField.requestFocusInWindow();
                }
            });
            
            emailField = GUIFactory.createTextField(15, null, imeOff, dl);
            emailField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    uid.requestFocusInWindow();
                }
            });
            
            licenses = ClientContext.getLicenseModel();
            licenseCombo = new JComboBox(licenses);
            
            depts = ClientContext.getDepartmentModel();
            deptCombo = new JComboBox(depts);
            
            ActionListener al = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addUserEntry();
                }
            };
            
            okButton = new JButton("追加");
            okButton.addActionListener(al);
            //okButton.setMnemonic('A');
            okButton.setEnabled(false);
            cancelButton = new JButton("閉じる");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    stop();
                }
            });
            //cancelButton.setMnemonic('C');
            
            // レイアウト
            JPanel content = new JPanel(new GridBagLayout());
            
            int x = 0;
            int y = 0;
            JLabel label = new JLabel("ユーザID:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, uid, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel("パスワード:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, userPassword1, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            label = new JLabel("確認:", SwingConstants.RIGHT);
            constrain(content, label, x + 2, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, userPassword2, x + 3, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel("姓:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, sn, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            label = new JLabel("名:", SwingConstants.RIGHT);
            constrain(content, label, x + 2, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, givenName, x + 3, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel("医療資格:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, licenseCombo, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            label = new JLabel("診療科:", SwingConstants.RIGHT);
            constrain(content, label, x + 2, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, deptCombo, x + 3, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel("電子メール:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, emailField, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel(" ", SwingConstants.RIGHT);
            constrain(content, label, x, y, 4, 1, GridBagConstraints.BOTH, GridBagConstraints.EAST);
            
            x = 0;
            y += 1;
            label = new JLabel("ユーザID - 半角英数記で" + userIdLength[0] + "文字以上" + userIdLength[1] + "文字以内");
            constrain(content, label, x, y, 4, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
            x = 0;
            y += 1;
            label = new JLabel("パスワード - 半角英数記で" + passwordLength[0] + "文字以上" + passwordLength[1] + "文字以内");
            constrain(content, label, x, y, 4, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
            
            JPanel btnPanel;
            if (isMac()) {
                btnPanel = GUIFactory.createCommandButtonPanel(new JButton[]{cancelButton, okButton});
            } else {
                btnPanel = GUIFactory.createCommandButtonPanel(new JButton[]{okButton, cancelButton});
            }
            
            this.setLayout(new BorderLayout(0, 17));
            this.add(content, BorderLayout.CENTER);
            this.add(btnPanel, BorderLayout.SOUTH);
            
            this.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        }
        
        private void addUserEntry() {
            
            if (!userIdOk()) {
                return;
            }
            
            if (!passwordOk()) {
                return;
            }
            
            String userId = uid.getText().trim();
            String pass = new String(userPassword1.getPassword());
            UserModel loginUser = Project.getUserModel();
            String facilityId = loginUser.getFacilityModel().getFacilityId();
            
            String Algorithm = ClientContext.getString("addUser.password.hash.algorithm");
            String encoding = ClientContext.getString("addUser.password.hash.encoding");
            //String charset = ClientContext.getString("addUser.password.hash.charset");
            String charset = null;
            String hashPass = HashUtil.MD5(pass);
            
            final UserModel user = new UserModel();
            StringBuilder sb = new StringBuilder(facilityId);
            sb.append(IInfoModel.COMPOSITE_KEY_MAKER);
            sb.append(userId);
            user.setUserId(sb.toString());
            user.setPassword(hashPass);
            user.setSirName(sn.getText().trim());
            user.setGivenName(givenName.getText().trim());
            user.setCommonName(user.getSirName() + " " + user.getGivenName());
            
            // 施設情報
            // 管理者のものを使用する
            user.setFacilityModel(Project.getUserModel().getFacilityModel());
            
            // 医療資格
            int index = licenseCombo.getSelectedIndex();
            user.setLicenseModel(licenses[index]);
            
            // 診療科
            index = deptCombo.getSelectedIndex();
            user.setDepartmentModel(depts[index]);
            
            // MemberType
            // 管理者のものを使用する
            user.setMemberType(Project.getUserModel().getMemberType());
            
            // RegisteredDate
            if (Project.getUserModel().getMemberType().equals("ASP_TESTER")) {
                user.setRegisteredDate(Project.getUserModel().getRegisteredDate());
            } else {
                user.setRegisteredDate(new Date());
            }
            
            // Email
            user.setEmail(emailField.getText().trim());
            
            // Role = user
            RoleModel rm = new RoleModel();
            rm.setRole(usersRole);
            user.addRole(rm);
            rm.setUserModel(user);
            rm.setUserId(user.getUserId()); // 必要
            
            // タスクを実行する
//masuda^   シングルトン化
            //final UserDelegater udl = new UserDelegater();
            final UserDelegater udl = UserDelegater.getInstance();
//masuda$
            
            worker = new SimpleWorker<Boolean, Void>() {
        
                @Override
                protected Boolean doInBackground() throws Exception {
                    logger.debug("addUserEntry doInBackground");
                    int cnt = udl.addUser(user);
                    return true;
                }
                
                @Override
                protected void succeeded(Boolean results) {
                    logger.debug("addUserEntry succeeded");
                    JOptionPane.showMessageDialog(getFrame(),
                            ADD_USER_SUCCESS_MSG,
                            ClientContext.getFrameTitle(getName()),
                            JOptionPane.INFORMATION_MESSAGE);
                }

                @Override
                protected void cancelled() {
                    logger.debug("addUserEntry cancelled");
                }

                @Override
                protected void failed(java.lang.Throwable cause) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("診療録と関連しているため、既存及び削除したユーザーと").append("\n");
                    sb.append("同じIDのユーザーを再度登録することはできません。");
                    JOptionPane.showMessageDialog(getFrame(),
                                sb.toString(),
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.WARNING_MESSAGE);
                    logger.warn("addUserEntry failed");
                    //logger.warn(cause.getCause());
                    //logger.warn(cause.getMessage());
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

            Component c = getFrame();
            String message = null;
            String note = ClientContext.getString("task.default.addMessage");
            maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            delay = ClientContext.getInt("task.default.delay");
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
        
        private boolean userIdOk() {
            
            String userId = uid.getText().trim();
            if (userId.equals("")) {
                return false;
            }
            
            int len = userId.length();
            return (len >= userIdLength[0] && len <= userIdLength[1]);
        }
        
        private boolean passwordOk() {
            
            String passwd1 = new String(userPassword1.getPassword());
            String passwd2 = new String(userPassword2.getPassword());
            
            if (passwd1.equals("") || passwd2.equals("")) {
                return false;
            }
            
            if ((passwd1.length() < passwordLength[0])
            || (passwd1.length() > passwordLength[1])) {
                return false;
            }
            
            if ((passwd2.length() < passwordLength[0])
            || (passwd2.length() > passwordLength[1])) {
                return false;
            }
            
            return passwd1.equals(passwd2);
        }
        
        private void checkButton() {
            
            boolean userOk = userIdOk();
            boolean passwordOk = passwordOk();
            boolean snOk = !sn.getText().trim().isEmpty();
            boolean givenOk = !givenName.getText().isEmpty();
            boolean emailOk = !emailField.getText().trim().isEmpty();
            
            boolean newOk = (userOk && passwordOk && snOk && givenOk && emailOk);
            
            if (ok != newOk) {
                ok = newOk;
                okButton.setEnabled(ok);
            }
        }
    }
    
    private void constrain(JPanel container, Component cmp, int x, int y,
            int width, int height, int fill, int anchor) {
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
        c.fill = fill;
        c.anchor = anchor;
        c.insets = new Insets(0, 0, 5, 7);
        ((GridBagLayout) container.getLayout()).setConstraints(cmp, c);
        container.add(cmp);
    }
    
    /**
     * タイムアウト警告表示を行う。
     */
    private void wraningTimeOut() {
        StringBuilder sb = new StringBuilder();
        sb.append(ClientContext.getString("task.timeoutMsg1"));
        sb.append("\n");
        sb.append(ClientContext.getString("task.timeoutMsg1"));
        JOptionPane.showMessageDialog(getFrame(),
                sb.toString(),
                ClientContext.getFrameTitle(getName()),
                JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * OSがmacかどうかを返す。
     * @return mac の時 true
     */
    private boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.startsWith("mac");
    }
}
