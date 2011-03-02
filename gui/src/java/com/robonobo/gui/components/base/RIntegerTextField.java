/**
 * 
 */
package com.robonobo.gui.components.base;

import static com.robonobo.common.util.TextUtil.*;

import javax.swing.text.*;

import com.robonobo.common.util.TextUtil;

/**
 * A textfield that only allows integers
 * 
 * @author macavity
 */
@SuppressWarnings("serial")
public class RIntegerTextField extends RTextField {
	private boolean allowNegative;

	public RIntegerTextField(Integer initialValue, boolean allowNegative) {
		super((initialValue == null) ? "" : String.valueOf(initialValue));
		this.allowNegative = allowNegative;
		if(initialValue != null)
			setText(initialValue.toString());
	}

	@Override
	protected Document createDefaultModel() {
		return new IntegerDocument();
	}

	class IntegerDocument extends PlainDocument {
		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			boolean ok = true;
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				// Only allow - at start
				if (allowNegative && offs == 0 && i == 0) {
					if (c == '-')
						continue;
				}
				if (c < '0' || c > '9') {
					ok = false;
					break;
				}
			}
			if (ok)
				super.insertString(offs, str, a);
		}
	}

	/**
	 * May be null if text is empty
	 */
	public Integer getIntValue() {
		String txt = getText();
		if(isEmpty(txt))
			return null;
		return Integer.valueOf(txt);
	}
}