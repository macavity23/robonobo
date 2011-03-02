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
 * {@link gnu.iou.b64 Base64 } encoder and decoder establishes the
 * pattern of <code>coder.enc</code> and <code>coder.dec</code>.
 * 
 * @author jdp
 */
public class base64 
    implements coder.format
{
    public final static java.lang.String NAME = "base64";

    /**
     * <p> Decode stream from {@link gnu.iou.b64 Base64 }. </p>
     * 
     * @author Robert Harder
     * @author John Pritchard
     */
    public static class dec
	extends fii
	implements producer
    {

        private final static int bufferLength = 3;

        private int position = -1;
        private byte[] buffer = new byte[bufferLength];
        private int numSigBytes;
        
	public dec( java.io.InputStream in){
	    super(in);
	}
	public dec( gnu.iou.bbuf buf){
	    super(buf);
	}

        /**
         * <p> Reads enough of the input stream to convert
         * from Base64 and returns the next byte.</p>
         */
        public int read() throws java.io.IOException { 
            if ( 0 > this.position){
		byte[] b4 = new byte[4];
		int cc = 0;
		for ( cc = 0; cc < 4; cc++ ){
		    // Read four "meaningful" bytes
		    int b = 0;
		    do {
			b = this.in.read(); 
		    }
		    while( b >= 0 && gnu.iou.b64.DECODABET[ b & 0x7f ] <= gnu.iou.b64.WHITE_SPACE_ENC );
		    
		    if ( b < 0 )
			break;
		    else
			b4[cc] = (byte)b;
		}
		
		if ( cc == 4 ){
		    this.numSigBytes = gnu.iou.b64.decode4to3( b4, 0, this.buffer, 0);
		    this.position = 0;
		}
		else if (0 == cc)
		    return -1;
		else 
		    throw new java.io.IOException( "Improperly padded Base64 input." );
            }
	    //
            if ( -1 < this.position){
                if ( this.numSigBytes <= this.position)
                    return -1;
                else {
                    int b = this.buffer[this.position++];
                    if ( this.position >= this.bufferLength )
                        this.position = -1;
                    return (b & 0xff);
                }
            }
            else
                throw new java.io.IOException( "Error in Base64 code reading stream." );
        }
        
        
        /**
         * <p> Calls {@link #read()} repeatedly until the end of
         * stream is reached or <var>len</var> bytes are read.</p>
         *
         * @param dest array to hold values
         * @param off offset for array
         * @param len max number of bytes to read into array
         * @return bytes read into array or -1 if end of stream is encountered.
         */
        public int read( byte[] dest, int off, int len ) throws java.io.IOException {
            int cc;
            int ch;
            for( cc = 0; cc < len; cc++ ){
                ch = read();
                if(-1 < ch)
                    dest[off+cc] = (byte)ch;
                else if (0 == cc)
                    return -1;
                else
                    break;
            }
            return cc;
        }
    }

    /**
     * <p> Encode stream to {@link gnu.iou.b64 Base64 }. </p>
     * 
     * @author Robert Harder
     * @author John Pritchard
     */
    public static class enc
	extends foo
	implements consumer
    {

        private final static int bufferLength = 3;

        private int     position = 0;
        private byte[]  buffer = new byte[ bufferLength ];
        private int     linelen = 0;
        private byte[]  b4 = new byte[4];

	public enc( java.io.OutputStream out){
	    super(out);
	}
	public enc( gnu.iou.bbuf buf){
	    super(buf);
	}

        /**
         * <p> Three bytes are buffered for encoding, before the
         * target stream actually gets a <code>write()</code>
         * call.</p>
         */
        public void write(int bb) throws java.io.IOException {
	    this.buffer[this.position++] = (byte)bb;
	    if ( bufferLength <= this.position){
		this.out.write( gnu.iou.b64.encode3to4( this.b4, this.buffer, bufferLength));
		this.linelen += 4;
		if( linelen >= gnu.iou.b64.MAX_LINE_LENGTH ){
		    this.out.write( gnu.iou.b64.NEW_LINE );
		    this.linelen = 0;
		}
		this.position = 0;
	    }
	}
        /**
         * <p> Calls {@link #write(int)} repeatedly until
         * <var>len</var> bytes are written.</p>
         */
        public void write( byte[] bbs, int off, int len ) throws java.io.IOException {
            for( int cc = 0; cc < len; cc++)
                this.write( bbs[off+cc]);
        }        
        /**
         * <p> Pads the buffer without closing the stream.</p>
         */
        public void flush() throws java.io.IOException {
            if (0 < this.position){
		this.out.write( gnu.iou.b64.encode3to4( this.b4, this.buffer, this.position));
		this.position = 0;
	    }
	    super.flush();
        }
    }

    public base64(){
	super();
    }
    public java.lang.String getName(){
	return NAME;
    }
    public fii decoder( java.io.InputStream in){
	return new dec(in);
    }
    public fii decoder( gnu.iou.bbuf buf){
	return new dec(buf);
    }
    public foo encoder( java.io.OutputStream out){
	return new enc(out);
    }
    public foo encoder( gnu.iou.bbuf buf){
	return new enc(buf);
    }

}
