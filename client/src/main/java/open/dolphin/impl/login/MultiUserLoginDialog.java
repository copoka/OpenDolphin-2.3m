package open.dolphin.impl.login;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import open.dolphin.client.AutoRomanListener;
import open.dolphin.client.ClientContext;
import open.dolphin.delegater.UserDelegater;
import open.dolphin.helper.SimpleWorker;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.UserModel;
import open.dolphin.project.Project;

/**
 * ログインダイアログ　クラス。
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class MultiUserLoginDialog extends AbstractLoginDialog {
    
    private LoginPanelUser view;
    private StateMgr stateMgr;

    
    /** Creates new LoginService */
    public MultiUserLoginDialog() {
    }
    
    @Override
    protected void tryLogin() {
        
        // User 情報を取得するためのデリゲータを得る
        if (userDlg == null) {
//masuda^   シングルトン化
            //userDlg = new UserDelegater();
            userDlg = UserDelegater.getInstance();
//masuda$
        }
        
        // トライ出来る最大回数を得る
        if (maxTryCount == 0) {
            maxTryCount = ClientContext.getInt("loginDialog.maxTryCount");
        }
        
        ClientContext.getPart11Logger().info("認証を開始します");
        
        // 試行回数 += 1
        tryCount++;

        //Task
        worker = new SimpleWorker<UserModel, Void>() {

            @Override
            protected UserModel doInBackground() throws Exception {
                String fid = Project.getFacilityId();
                String uid = (String)view.getUsersCmb().getSelectedItem();
                String password = new String(view.getPasswordField().getPassword());
                UserModel userModel = userDlg.login(fid, uid, password);
                return userModel;
            }
            
            @Override
            protected void succeeded(UserModel userModel) {
                if (userModel != null) {   
                    // 認証成功
                    String time = ModelUtils.getDateTimeAsString(new Date());
                    StringBuilder sb = new StringBuilder();
                    sb.append(time).append(":");
                    sb.append(userModel.getUserId()).append(" がログインしました");
                    ClientContext.getPart11Logger().info(sb.toString());

                    //----------------------------------
                    // ユーザモデルを ProjectStub へ保存する
                    //----------------------------------
                    Project.getProjectStub().setUserModel(userModel);
                    Project.getProjectStub().setFacilityId(userModel.getFacilityModel().getFacilityId());
                    Project.getProjectStub().setUserId(userModel.idAsLocal());  // facilityId無し

                    setResult(LoginStatus.AUTHENTICATED);
                     
                } else {
                    if (tryCount <= maxTryCount) {
                        showUserIdPasswordError();

                    } else {
                        showTryOutError();
                        setResult(LoginStatus.NOT_AUTHENTICATED);
                    }
                }
            }
            
            @Override
            protected void failed(java.lang.Throwable cause) {
                ClientContext.getPart11Logger().warn("Task failed");
                ClientContext.getPart11Logger().warn(cause.getCause());
                ClientContext.getPart11Logger().warn(cause.getMessage());

                if (tryCount <= maxTryCount) {
                    showMessageDialog(cause.getMessage());

                } else {
                    showTryOutError();
                    setResult(LoginStatus.NOT_AUTHENTICATED);
                }
            }
        };
        
        worker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                if (SwingWorker.StateValue.DONE == evt.getNewValue()) {
                    setBusy(false);
                    worker.removePropertyChangeListener(this);
                } else if (SwingWorker.StateValue.STARTED == evt.getNewValue()) {
                    setBusy(true);
                }
            }  
        });
        
        worker.execute();
    }
    
    /**
     * GUI を構築する。
     */
    @Override
    protected JPanel createComponents() {

        String names = Project.getString("login.set.users");
        String[] name = names.split("\\s*,\\s*");
        int cnt = name.length;
        String[] data = new String[cnt];
        System.arraycopy(name, 0, data, 0, cnt);
        DefaultComboBoxModel cmbModel = new DefaultComboBoxModel(data);

        view = new LoginPanelUser();
        view.getUsersCmb().setModel(cmbModel);

        // イベント接続を行う
        connect();

        return view;
    }
    
    /**
     * イベント接続を行う。
     */
    private void connect() {
        
        // Mediator ライクな StateMgr
        stateMgr = new StateMgr();
        
        // フィールドにリスナを登録する
        DocumentListener dl = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                stateMgr.checkButtons();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                stateMgr.checkButtons();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                stateMgr.checkButtons();
            }
        };

        view.getUsersCmb().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    stateMgr.onUserIdAction();
                }
            }
        });
        
        JPasswordField passwdField = view.getPasswordField();
        passwdField.getDocument().addDocumentListener(dl);
        passwdField.addFocusListener(AutoRomanListener.getInstance());
        passwdField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stateMgr.onPasswordAction();
            }
        });
    }

    @Override
    protected JButton getLoginButton() {
        return view.getLoginBtn();
    }

    @Override
    protected JButton getCancelButton() {
        return view.getCancelBtn();
    }

    @Override
    protected JButton getSettingButton() {
        return view.getSettingBtn();
    }

    @Override
    protected JProgressBar getProgressBar() {
        return view.getProgressBar();
    }
    
    /**
     * モデルを表示する。
     */
    @Override
    protected void doWindowOpened() {
        String uid = Project.getUserId();
        if (uid==null || uid.equals("")) {
            return;
        }
        int cnt = view.getUsersCmb().getItemCount();
        for (int i=0; i < cnt; i++) {
            String test = (String)view.getUsersCmb().getItemAt(i);
            if (test.equals(uid)) {
                view.getUsersCmb().setSelectedIndex(i);
                view.getPasswordField().requestFocusInWindow();
                break;
            }
        }
    }
    
    /**
     * 設定ダイアログから通知を受ける。
     * 有効なプロジェクトでればユーザIDをフィールドに設定しパスワードフィールドにフォーカスする。
     **/
    @Override
    public void setNewParams(Boolean newValue) {
        boolean valid = newValue.booleanValue();
        if (valid) {
            doWindowOpened();
        }
    }
    
    /**
     * ログインボタンを制御する簡易 StateMgr クラス。
     */
    class StateMgr  {
        
        private boolean okState;
        
        public StateMgr() {
        }
        
        /**
         * ログインボタンの enable/disable を制御する。
         */
        public void checkButtons() {
            boolean newOKState = true;
            newOKState = newOKState &&  (view.getUsersCmb().getSelectedItem()!=null);
            newOKState = newOKState && (view.getPasswordField().getPassword().length >0);
            
            if (newOKState != okState) {
                loginAction.setEnabled(newOKState);
                okState = newOKState;
            }
        }
        
        /**
         * UserId フィールドでリターンきーが押された時の処理を行う。
         */
        public void onUserIdAction() {
            view.getPasswordField().requestFocusInWindow();
        }
        
        /**
         * Password フィールドでリターンきーが押された時の処理を行う。
         */
        public void onPasswordAction() {
            if (view.getUsersCmb().getSelectedItem()==null) {
                view.getUsersCmb().requestFocusInWindow();
                
            } else if (view.getPasswordField().getPassword().length != 0 && okState) {
                view.getLoginBtn().doClick();
            }
        }
    }
}