package open.dolphin.tr;

import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * Transferable class of the PTrain.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class OrderListTransferable implements Transferable, ClipboardOwner {

    /** Data Flavor of this class */
    public static DataFlavor orderListFlavor = new DataFlavor(open.dolphin.tr.OrderList.class, "Order List");

    public static final DataFlavor[] flavors = {OrderListTransferable.orderListFlavor};

    private OrderList list;


    /** Creates new OrderListTransferable */
    public OrderListTransferable(OrderList list) {
        this.list = list;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
    	return flavors;
    }

    @Override
    public boolean isDataFlavorSupported( DataFlavor flavor ) {
        return orderListFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
	    throws UnsupportedFlavorException, IOException {

        if (flavor.equals(orderListFlavor)) {
            return list;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public String toString() {
        return "Order List Transferable";
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        AbstractKarteTransferHandler.clearVariables();
    }
}