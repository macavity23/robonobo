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
public class ProcessingInstruction
    extends Node
    implements gnu.iou.dom.ProcessingInstruction
{
    private String pi_target, pi_data;

    public ProcessingInstruction(org.w3c.dom.Document owner, gnu.iou.dom.Name nname){
        super(owner,nname);
    }

    public java.lang.String getTarget(){
	return this.pi_target;
    }
    public void setTarget(java.lang.String tgt){
	this.pi_target = tgt;
    }
    public java.lang.String getData(){
	return this.pi_data;
    }
    public void setData(java.lang.String data)
	throws org.w3c.dom.DOMException
    {
	this.pi_data = data;
    }

}
