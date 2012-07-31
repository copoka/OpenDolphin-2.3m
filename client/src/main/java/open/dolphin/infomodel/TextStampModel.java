/*
 * TextStamp.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
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
package open.dolphin.infomodel;

/**
 * TextStampModel
 * 
 * @author Kazushi Minagawa
 *
 */
public class TextStampModel extends InfoModel {
	
    private String text;

    /** Creates a new instance of TextStamp */
    public TextStampModel() {
    }

    public String getText() {
            return text;
    }

    public void setText(String val) {
            text = val;
    }

    @Override
    public String toString() {
            return getText();
    }
}
