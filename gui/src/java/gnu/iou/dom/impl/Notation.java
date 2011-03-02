 /* 
  *  `gnu.iou.dom' 
  *  Copyright (C) 2006 John Pritchard.
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License as
  *  published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
  *  02111-1307 USA
  */

package gnu.iou.dom.impl;

/**
 * 
 * @author jdp
 */
public class Notation
    extends Node
    implements gnu.iou.dom.Notation
{
    private String notation_pid, notation_sid;

    public Notation(org.w3c.dom.Document owner, gnu.iou.dom.Name nname){
        super(owner,nname);
    }

    public final java.lang.String getPublicId(){
	return this.notation_pid;
    }
    public final void setPublicId(java.lang.String pid){
	this.notation_pid = pid;
    }
    public final java.lang.String getSystemId(){
	return this.notation_sid;
    }
    public final void setSystemId(java.lang.String sid){
	this.notation_sid = sid;
    }

}