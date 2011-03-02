 /* 
  *  `gnu.iou' 
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

package gnu.iou.fil ;

/**
 * <p> Coder class marking interface in support of the model for inner
 * classes 'enc' and 'dec'.  Implementors have a public simple
 * constructor (no args).  
 * </p>
 * 
 * <p> The coder or container class is intended as a factory for the
 * {@link Registry} to instantiate its encoders and decoders as
 * directed by the {@link Registry}. </p>
 * 
 * <p> Implementors must employ {@link coder$format} and {@link
 * coder$transform} specifications for filters employing {@link
 * consumer} and {@link producer}. </p>
 * 
 * 
 * @see Registry
 * @see base64
 * @see oaep
 * 
 * @author John Pritchard (jdp@syntelos.com)
 */
public interface coder
{
    /**
     * <p> Implementors use the {@link producer} marking on decoders,
     * and the {@link consumer} marking on encoders.  For example
     * {@link base64}. </p>
     */
    public interface format
	extends coder
    {}
    /**
     * <p> Implementors use the {@link consumer} marking on decoders,
     * and the {@link producer} marking on encoders.  For example
     * {@link oaep}. </p>
     */
    public interface transform
	extends coder
    {}


    /**
     * @return Registry name, eg, "base64" or "oaep".
     */
    public java.lang.String getName();

    /**
     * Instantiate new decoder 
     */
    public fii decoder( java.io.InputStream in);
    /**
     * Instantiate new decoder 
     */
    public fii decoder( gnu.iou.bbuf buf);
    /**
     * Instantiate new encoder 
     */
    public foo encoder( java.io.OutputStream out);
    /**
     * Instantiate new encoder 
     */
    public foo encoder( gnu.iou.bbuf buf);

}
