/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.html.core.internal.contenttype;

import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.wst.sse.core.internal.encoding.CodedIO;
import org.eclipse.wst.sse.core.internal.encoding.EncodingMemento;
import org.eclipse.wst.sse.core.internal.encoding.IResourceCharsetDetector;
import org.eclipse.wst.xml.core.internal.contenttype.EncodingParserConstants;

public class HTMLResourceEncodingDetector extends AbstractResourceEncodingDetector implements IResourceCharsetDetector {

	private HTMLHeadTokenizer fTokenizer;

	/**
	 * There is no spec defined encoding for HTML (historically), so null is
	 * returned.
	 */
	public String getSpecDefaultEncoding() {
		return null;
	}

	private boolean canHandleAsUnicodeStream(String tokenType) {
		boolean canHandleAsUnicodeStream = false;
		if (tokenType == EncodingParserConstants.UTF83ByteBOM) {
			canHandleAsUnicodeStream = true;
			String enc = "UTF-8"; //$NON-NLS-1$
			createEncodingMemento(enc, EncodingMemento.DETECTED_STANDARD_UNICODE_BYTES);
			fEncodingMemento.setUTF83ByteBOMUsed(true);
		}
		else if (tokenType == EncodingParserConstants.UTF16BE) {
			canHandleAsUnicodeStream = true;
			String enc = "UTF-16BE"; //$NON-NLS-1$
			createEncodingMemento(enc, EncodingMemento.DETECTED_STANDARD_UNICODE_BYTES);
		}
		else if (tokenType == EncodingParserConstants.UTF16LE) {
			canHandleAsUnicodeStream = true;
			String enc = "UTF-16"; //$NON-NLS-1$
			createEncodingMemento(enc, EncodingMemento.DETECTED_STANDARD_UNICODE_BYTES);
		}
		return canHandleAsUnicodeStream;
	}

	/**
	 * @return Returns the tokenizer.
	 */
	private HTMLHeadTokenizer getTokenizer() {
		// TODO: need to work on 'reset' in tokenizer, so new instance isn't
		// always needed
		//if (fTokenizer == null) {
		fTokenizer = new HTMLHeadTokenizer();
		//		}
		return fTokenizer;
	}

	private boolean isLegalString(String valueTokenType) {
		if (valueTokenType == null)
			return false;
		else
			return valueTokenType.equals(EncodingParserConstants.StringValue) || valueTokenType.equals(EncodingParserConstants.UnDelimitedStringValue) || valueTokenType.equals(EncodingParserConstants.InvalidTerminatedStringValue) || valueTokenType.equals(EncodingParserConstants.InvalidTermintatedUnDelimitedStringValue);
	}

	protected void parseInput() throws IOException {
		checkInContent();
		if (fEncodingMemento == null) {
			checkHeuristics();
		}
	}

	/**
	 *  
	 */
	private void checkHeuristics() throws IOException {
		boolean noHeuristic = false;
		String heuristicEncoding = null;
		try {
			fReader.reset();
			fReader.mark(CodedIO.MAX_MARK_SIZE);
			byte[] bytes = new byte[CodedIO.MAX_MARK_SIZE];
			int nRead = 0;
			for (int i = 0; i < bytes.length; i++) {
				if (fReader.ready()) {
					int oneByte = fReader.read();
					nRead++;
					if (oneByte <= 0xFF) {
						bytes[i] = (byte) oneByte;
					}
					else {
						noHeuristic = true;
						break;
					}
				}
				else {
					break;
				}
			}
			if (!noHeuristic) {
				heuristicEncoding = EncodingGuesser.guessEncoding(bytes, nRead);
			}
		}
		catch (IOException e) {
			// if any IO exception, then not a heuristic case
		}
		finally {
			fReader.reset();
		}
		if (heuristicEncoding != null) {
			createEncodingMemento(heuristicEncoding, EncodingMemento.GUESSED_ENCODING_FROM_STREAM);
		}

	}

	private void checkInContent() throws IOException {
		HTMLHeadTokenizer tokenizer = getTokenizer();
		tokenizer.reset(fReader);
		HeadParserToken token = null;
		String tokenType = null;
		String contentTypeValue = null;
		do {
			token = tokenizer.getNextToken();
			tokenType = token.getType();
			if (canHandleAsUnicodeStream(tokenType)) {
				// side effect of canHandle is to create appropriate
				// memento
			}
			else if (tokenType == HTMLHeadTokenizerConstants.MetaTagContentType) {
				if (tokenizer.hasMoreTokens()) {
					HeadParserToken valueToken = tokenizer.getNextToken();
					String valueTokenType = valueToken.getType();
					if (isLegalString(valueTokenType)) {
						contentTypeValue = valueToken.getText();

					}
				}
			}

		}
		while (tokenizer.hasMoreTokens());
		if (contentTypeValue != null) {
			parseContentTypeValue(contentTypeValue);
		}
	}

	private void parseContentTypeValue(String contentType) {
		String charset = null;
		Pattern pattern = Pattern.compile(";\\s*charset\\s*=\\s*"); //$NON-NLS-1$
		String[] parts = pattern.split(contentType);
		if (parts.length > 0) {
			// if only one item, it can still be charset instead of
			// contentType
			if (parts.length == 1) {
				if (parts[0].length() > 6) {
					String checkForCharset = parts[0].substring(0, 7);
					if (checkForCharset.equalsIgnoreCase("charset")) { //$NON-NLS-1$
						int eqpos = parts[0].indexOf('=');
						eqpos = eqpos + 1;
						if (eqpos < parts[0].length()) {
							charset = parts[0].substring(eqpos);
							charset = charset.trim();
						}
					}
				}
			}
			else {
				//fContentType = parts[0];
			}
		}
		if (parts.length > 1) {
			charset = parts[1];
		}
		
		if (charset != null && charset.length() > 0) {
			createEncodingMemento(charset, EncodingMemento.FOUND_ENCODING_IN_CONTENT);
		}
	}

}