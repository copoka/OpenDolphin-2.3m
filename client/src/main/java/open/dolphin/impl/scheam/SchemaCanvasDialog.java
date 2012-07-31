/*
 * SchemaCanvasDialog.java
 *
 * Created on 2011/02/15, 20:33:42
 */

package open.dolphin.impl.scheam;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import open.dolphin.impl.scheam.schemahelper.SchemaTitleBorder;

/**
 * SchemaCanvasEditor で使うカスタム dialog
 * @author pns
 */
public class SchemaCanvasDialog extends javax.swing.JDialog {

    private int result;
    private SchemaCanvasView parent;

    public SchemaCanvasDialog(SchemaCanvasView parent, boolean modal) {
        super(parent, modal);
        this.parent = parent;

        initComponents();
        initTitlePanel();

        okBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                result = JOptionPane.OK_OPTION;
                setVisible(false);
                dispose();
            }
        });
        cancelBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                result = JOptionPane.CANCEL_OPTION;
                setVisible(false);
                dispose();
            }
        });
        // ショートカット登録
        ActionMap am = getRootPane().getActionMap();
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        // Enter で OK
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ok");
        am.put("ok", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                okBtn.doClick();
            }
        });
        // ESC でキャンセル
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        am.put("cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cancelBtn.doClick();
            }
        });
    }

    @Override
    public void setVisible(boolean b) {
        parent.setAlwaysOnTop(!b);
        //this.setLocationRelativeTo(parent);
        super.setVisible(b);
    }

    /**
     * JDialog を undecorated にして
     * titlePanel をつかんで移動できるようにする
     */
    private void initTitlePanel() {
        TitlePanelListener l = new TitlePanelListener();
        titlePanel.addMouseListener(l);
        titlePanel.addMouseMotionListener(l);
        titlePanel.setBorder(new SchemaTitleBorder());
        titleLbl.setText(SchemaEditorImpl.TITLE);
    }
    /**
     * titlePanel をつかんで移動させるためのリスナ
     */
    private class TitlePanelListener extends MouseAdapter {
        private Point from;

        @Override
        public void mousePressed(MouseEvent e) {
            from = e.getLocationOnScreen();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point to = e.getLocationOnScreen();

            int dx = to.x - from.x;
            int dy = to.y - from.y;

            Point p = getLocation();
            setLocation(new Point(p.x + dx, p.y + dy));

            from.x = to.x; from.y = to.y;
        }
    }

    /**
     * JPanel を登録して，大きさを調節する
     * @param panel
     */
    public void addContent(JPanel panel) {
        Dimension d = panel.getPreferredSize();
        contentPanel.add(panel);
        contentPanel.setPreferredSize(d);

        Rectangle r = parent.getBounds();
        int width = d.width + 10;
        int height = d.height + 60;
        int x = r.x + (r.width-width)/2;
        int y = r.y;
        this.setBounds(x, y, width, height);
    }

    /**
     * OK or Cancel を返す
     * @return
     */
    public int getResult() {
        return result;
    }

    /**
     * Dialog のタイトルを設定する
     * @param title
     */
    public void setTitle(String title) {
        titleLbl.setText(title);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titlePanel = new javax.swing.JPanel();
        titleLbl = new javax.swing.JLabel();
        contentPanel = new javax.swing.JPanel();
        btnPanel = new javax.swing.JPanel();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N
        setUndecorated(true);

        titlePanel.setName("titlePanel"); // NOI18N

        titleLbl.setFont(new java.awt.Font("Lucida Grande", 0, 9)); // NOI18N
        titleLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLbl.setText("TItle");
        titleLbl.setMaximumSize(new java.awt.Dimension(10000, 16));
        titleLbl.setMinimumSize(new java.awt.Dimension(72, 16));
        titleLbl.setName("titleLbl"); // NOI18N
        titleLbl.setPreferredSize(new java.awt.Dimension(72, 16));

        javax.swing.GroupLayout titlePanelLayout = new javax.swing.GroupLayout(titlePanel);
        titlePanel.setLayout(titlePanelLayout);
        titlePanelLayout.setHorizontalGroup(
            titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, titlePanelLayout.createSequentialGroup()
                    .addComponent(titleLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        titlePanelLayout.setVerticalGroup(
            titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
            .addGroup(titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(titlePanelLayout.createSequentialGroup()
                    .addComponent(titleLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        contentPanel.setName("contentPanel"); // NOI18N
        contentPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        btnPanel.setName("btnPanel"); // NOI18N

        okBtn.setText("OK");
        okBtn.setName("okBtn"); // NOI18N
        okBtn.setSelected(true);

        cancelBtn.setText("キャンセル");
        cancelBtn.setName("cancelBtn"); // NOI18N

        javax.swing.GroupLayout btnPanelLayout = new javax.swing.GroupLayout(btnPanel);
        btnPanel.setLayout(btnPanelLayout);
        btnPanelLayout.setHorizontalGroup(
            btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, btnPanelLayout.createSequentialGroup()
                .addContainerGap(193, Short.MAX_VALUE)
                .addComponent(cancelBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(okBtn)
                .addContainerGap())
        );
        btnPanelLayout.setVerticalGroup(
            btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnPanelLayout.createSequentialGroup()
                .addGroup(btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okBtn)
                    .addComponent(cancelBtn))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(titlePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(titlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SchemaCanvasDialog dialog = new SchemaCanvasDialog(new SchemaCanvasView(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btnPanel;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JButton okBtn;
    private javax.swing.JLabel titleLbl;
    private javax.swing.JPanel titlePanel;
    // End of variables declaration//GEN-END:variables

}
