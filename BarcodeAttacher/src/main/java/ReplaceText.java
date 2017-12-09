import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDTextState;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

public final class ReplaceText {
	/**
	 * Default constructor.
	 */

	private static Stack<PDGraphicsState> graphicsStack = new Stack<PDGraphicsState>();
	private Matrix textMatrix=new Matrix();;

	private ReplaceText() {
		// example class should not be instantiated
	}

	/**
	 * This will remove all text from a PDF document.
	 *
	 * @param args
	 *            The command line arguments.
	 *
	 * @throws IOException
	 *             If there is an error parsing the document.
	 */
	public static void main(String[] args) throws IOException {
		/*
		 * if (args.length != 4) { usage(); } else {
		 */
		PDDocument document = null;
		try {
			document = PDDocument.load(
					new File("C:\\\\Users\\\\mduraimani\\\\Desktop\\\\ioc\\\\Temp. Trial file print1pg- Copy.pdf"));
			if (document.isEncrypted()) {
				System.err.println("Error: Encrypted documents are not supported for this example.");
				System.exit(1);
			}

			// System.out.println(args[0] + " => " + args[1]);

			document = _ReplaceText(document, "ARUMUGAM STREET, 1 ST", "BarcodeFound");
			document.save("C:\\\\Users\\\\mduraimani\\\\Desktop\\\\ioc\\\\Temp. Trial file print1pg- Copy1.pdf");
		} finally {
			if (document != null) {
				document.close();
			}
		}

	}

	private static PDDocument _ReplaceText(PDDocument document, String searchString, String replacement)
			throws IOException {
		ReplaceText replaceText=new ReplaceText();
		if (null == searchString || searchString.isEmpty() || replacement == null || replacement.isEmpty()) {
			return document;
		}

		for (PDPage page : document.getPages()) {
			graphicsStack.clear();
			graphicsStack.push(new PDGraphicsState(page.getCropBox()));
			PDFStreamParser parser = new PDFStreamParser(page);
			parser.parse();
			List tokens = (List) parser.getTokens();

			for (int j = 0; j < tokens.size(); j++) {
				Object next = ((java.util.List<Object>) tokens).get(j);
				if (next instanceof Operator) {
					Operator op = (Operator) next;

					String pstring = "";
					int prej = 0;

					// Tj and TJ are the two operators that display strings in a PDF
					if (op.getName().equals("Tj")) {
						// Tj takes one operator and that is the string to display so lets update that
						// operator
						COSString previous = (COSString) tokens.get(j - 1);
						String string = previous.getString();
						string = string.replaceFirst(searchString, replacement);
						previous.setValue(string.getBytes());
					} else if (op.getName().equals("TJ")) {
						COSArray previous = (COSArray) tokens.get(j - 1);
						for (int k = 0; k < previous.size(); k++) {
							Object arrElement = previous.getObject(k);
							if (arrElement instanceof COSString) {
								COSString cosString = (COSString) arrElement;
								String string = cosString.getString();

								if (j == prej) {
									pstring += string;
								} else {
									prej = j;
									pstring = string;
								}
							}
						}
						System.out.println(pstring);
						//replaceText.sat(previous);
						if (searchString.equals(pstring.trim())) {
							COSString cosString2 = (COSString) previous.getObject(0);
							PDImageXObject imageXObject=PDImageXObject.createFromFile("C:\\Users\\mduraimani\\Desktop\\ioc\\barcode(1).png", document);
							cosString2.setValue(replacement.getBytes());

							int total = previous.size() - 1;
							for (int k = total; k > 0; k--) {
								previous.remove(k);
							}
						}
					}
				}
			}

			// now that the tokens are updated we will replace the page content stream.
			PDStream updatedStream = new PDStream(document);
			OutputStream out = updatedStream.createOutputStream(COSName.FLATE_DECODE);
			ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
			tokenWriter.writeTokens(tokens);
			out.close();
			page.setContents(updatedStream);
		}

		return document;
	}

	/**
	 * This will print the usage for this document.
	 */
	private static void usage() {
		System.err.println(
				"Usage: java " + ReplaceText.class.getName() + " <old-text> <new-text> <input-pdf> <output-pdf>");
	}

	private void sat(COSArray array) throws IOException {
		PDTextState textState = ((PDGraphicsState) getGraphicsState()).getTextState();
		float fontSize = textState.getFontSize();
		float horizontalScaling = textState.getHorizontalScaling() / 100f;
		PDFont font = textState.getFont();
		boolean isVertical = false;
		if (font != null) {
			isVertical = font.isVertical();
		}

		for (COSBase obj : array) {
			if (obj instanceof COSNumber) {
				float tj = ((COSNumber) obj).floatValue();

				// calculate the combined displacements
				float tx, ty;
				if (isVertical) {
					tx = 0;
					ty = -tj / 1000 * fontSize;
				} else {
					tx = -tj / 1000 * fontSize * horizontalScaling;
					ty = 0;
				}

				applyTextAdjustment(tx, ty);
			} else if (obj instanceof COSString) {
				byte[] string = ((COSString) obj).getBytes();
				showText(string);
			} else {
				throw new IOException("Unknown type in array for TJ operation:" + obj);
			}
		}

	}

	private Object getGraphicsState() {
		// TODO Auto-generated method stub
		return graphicsStack.peek();
	}

	protected void applyTextAdjustment(float tx, float ty) throws IOException {
		// update the text matrix
		textMatrix.concatenate(Matrix.getTranslateInstance(tx, ty));
	}

	public void saveGraphicsState() {
		graphicsStack.push(graphicsStack.peek().clone());
	}

	protected void showText(byte[] string) throws IOException {
		PDGraphicsState state = (PDGraphicsState) getGraphicsState();
		PDTextState textState = state.getTextState();

		// get the current font
		PDFont font = textState.getFont();
		if (font == null) {
			// LOG.warn("No current font, will use default");
			font = PDFontFactory.createDefaultFont();
		}

		float fontSize = textState.getFontSize();
		float horizontalScaling = textState.getHorizontalScaling() / 100f;
		float charSpacing = textState.getCharacterSpacing();

		// put the text state parameters into matrix form
		Matrix parameters = new Matrix(fontSize * horizontalScaling, 0, // 0
				0, fontSize, // 0
				0, textState.getRise()); // 1

		// read the stream until it is empty
		InputStream in = new ByteArrayInputStream(string);
		while (in.available() > 0) {
			// decode a character
			int before = in.available();
			int code = font.readCode(in);
			int codeLength = before - in.available();
			String unicode = font.toUnicode(code);

			// Word spacing shall be applied to every occurrence of the single-byte
			// character code
			// 32 in a string when using a simple font or a composite font that defines code
			// 32 as
			// a single-byte code.
			float wordSpacing = 0;
			if (codeLength == 1 && code == 32) {
				wordSpacing += textState.getWordSpacing();
			}

			// text rendering matrix (text space -> device space)
			Matrix ctm = state.getCurrentTransformationMatrix();
			Matrix textRenderingMatrix = parameters.multiply(textMatrix).multiply(ctm);

			// get glyph's position vector if this is vertical text
			// changes to vertical text should be tested with PDFBOX-2294 and PDFBOX-1422
			if (font.isVertical()) {
				// position vector, in text space
				Vector v = font.getPositionVector(code);

				// apply the position vector to the horizontal origin to get the vertical origin
				textRenderingMatrix.translate(v);
			}

			// get glyph's horizontal and vertical displacements, in text space
			Vector w = font.getDisplacement(code);

			// process the decoded glyph
			saveGraphicsState();
			Matrix textMatrixOld = textMatrix;
		//	Matrix textLineMatrixOld = textLineMatrix;
			//showGlyph(textRenderingMatrix, font, code, unicode, w);
			textMatrix = textMatrixOld;
			//textLineMatrix = textLineMatrixOld;
			//restoreGraphicsState();

			// calculate the combined displacements
			float tx, ty;
			if (font.isVertical()) {
				tx = 0;
				ty = w.getY() * fontSize + charSpacing + wordSpacing;
			} else {
				tx = (w.getX() * fontSize + charSpacing + wordSpacing) * horizontalScaling;
				ty = 0;
			}

			// update the text matrix
			textMatrix.concatenate(Matrix.getTranslateInstance(tx, ty));
		}
	}

}
