package open.dolphin.stampbox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import open.dolphin.client.ClientContext;
import open.dolphin.infomodel.ModuleInfoBean;
import org.apache.log4j.Logger;

/**
 * DefaultStampTreeBuilder
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class ASpStampTreeBuilder extends AbstractStampTreeBuilder {
    
    /** Control staffs */
    private StampTreeNode rootNode;
    private StampTreeNode node;
    private ModuleInfoBean info;
    private LinkedList<StampTreeNode> linkedList;
    private List<StampTree> products;
    
    private Logger logger;
    
    
    /** Creates new DefaultStampTreeBuilder */
    public ASpStampTreeBuilder() {
        logger = ClientContext.getBootLogger();
    }
    
    /**
     * Returns the product of this builder
     * @return vector that contains StampTree instances
     */
    @Override
    public List<StampTree> getProduct() {
        return products;
    }
    
    @Override
    public void buildStart() {
        products = new ArrayList<StampTree>();
        if (logger != null) {
            logger.debug("Build StampTree start");
        }
    }
    
    @Override
    public void buildRoot(String name, String entity) {
        // New root
        if (logger != null) {
            logger.debug("Root=" + name);
        }
        linkedList = new LinkedList<StampTreeNode>();
        
        // TreeInfo を rootNode に保存する
        TreeInfo treeInfo = new TreeInfo();
        treeInfo.setName(name);
        treeInfo.setEntity(entity);
        rootNode = new StampTreeNode(treeInfo);
        linkedList.addFirst(rootNode);
    }
    
    @Override
    public void buildNode(String name) {
        // New node
        if (logger != null) {
            logger.debug("Node=" + name);
        }
        
        node = new StampTreeNode(name);
        getCurrentNode().add(node);
        
        // Add the new node to be current node
        linkedList.addFirst(node);
    }
    
    @Override
    public void buildStampInfo(String name,
            String role,
            String entity,
            String editable,
            String memo,
            String id) {
        
        if (logger != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append(",");
            sb.append(role);
            sb.append(",");
            sb.append(entity);
            sb.append(",");
            sb.append(editable);
            sb.append(",");
            sb.append(memo);
            sb.append(",");
            sb.append(id);
            logger.debug(sb.toString());
        }
        
         // ASP Tree なのでエディタから発行を無視する
        if (name.equals("エディタから発行...") && (id == null) && (role.equals("p")) ) {
            return;
        }
        
        info = new ModuleInfoBean();
        info.setStampName(name);
        info.setStampRole(role);
        info.setEntity(entity);
        if (editable != null) {
            info.setEditable(Boolean.valueOf(editable).booleanValue());
        }
        if (memo != null) {
            info.setStampMemo(memo);
        }
        if ( id != null ) {
            info.setStampId(id);
        }
        
        // StampInfo から TreeNode を生成し現在のノードへ追加する
        node = new StampTreeNode(info);
        getCurrentNode().add(node);
    }
    
    @Override
    public void buildNodeEnd() {
        if (logger != null) {
            logger.debug("End node");
        }
        linkedList.removeFirst();
    }
    
    @Override
    public void buildRootEnd() {
        
        StampTree tree = new StampTree(new StampTreeClientModel(rootNode));
        products.add(tree);
        
        if (logger != null) {
            int pCount = products.size();
            logger.debug("End root " + "count=" + pCount);
        }
    }
    
    @Override
    public void buildEnd() {
        if (logger != null) {
            logger.debug("Build end");
        }
    }
    
    private StampTreeNode getCurrentNode() {
        return linkedList.getFirst();
    }
    
}