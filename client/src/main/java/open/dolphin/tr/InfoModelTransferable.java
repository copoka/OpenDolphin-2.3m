/*
 * InfoModelTransferable.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2004 Digital Globe, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *	
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *	
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.tr;

import java.awt.datatransfer.*;
import java.io.IOException;
import open.dolphin.infomodel.IInfoModel;

     
/**
 * Transferable class of the IInfoModel.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */ 
public final class InfoModelTransferable implements Transferable, ClipboardOwner {

    /** Data Flavor of this class */
    public static DataFlavor infoModelFlavor = new DataFlavor(open.dolphin.infomodel.IInfoModel.class, "Info Model");

    public static final DataFlavor[] flavors = {InfoModelTransferable.infoModelFlavor};
      
    private IInfoModel model;

    /** Creates new InfoModelTransferable */
    public InfoModelTransferable(IInfoModel model) {
        this.model = model;
    }

    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
    	return flavors;
    }
     
    @Override
    public boolean isDataFlavorSupported( DataFlavor flavor ) {
        return infoModelFlavor.equals(flavor);
    }

    @Override
    public synchronized Object getTransferData(DataFlavor flavor)
	    throws UnsupportedFlavorException, IOException {

        if (flavor.equals(infoModelFlavor)) {
            return model;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public String toString() {
        return "InfoModelTransferable";
    }
  
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}