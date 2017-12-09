import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class PDFBoxTest {

	public static void main(String args[]) throws Exception {
		// Loading an existing document
		File file = new File("C:\\Users\\mduraimani\\Desktop\\ioc\\Temp. Trial file print1pg- Copy.pdf");
		PDDocument doc = PDDocument.load(file);

		// Retrieving the page
		PDPage page = doc.getPage(0);

		// Creating PDImageXObject object
		//PDFTextStripper pdfStripper = new PDFTextStripper();
		// pdfStripper.processPages(doc.getPages());
		// PDResources pdResources=pdfStripper.getResources();
		// pdResources.
		// pdfStripper.beginText();
		// pdfStripper.getResources().
		//String text = pdfStripper.getText(doc);
	//	System.out.println(text);
		// PDPageContentStream contentStream = new PDPageContentStream(doc, page);
		// createPDF(text,contentStream);

		//List<PDField> fields = doc.getDocumentCatalog().getAcroForm().getFields();
/*		for (PDField field : fields) {
			System.out.println(field.getValueAsString());
		}
*/
		PDImageXObject pdImage = PDImageXObject.createFromFile("C:\\Users\\mduraimani\\Desktop\\ioc\\barcode (1).png", doc);

		// creating the PDPageContentStream object
		 PDPageContentStream contents = new PDPageContentStream(doc, page,AppendMode.APPEND,false);
		 PDRectangle mediaBox = page.getMediaBox();
		// contents.
		// contents.beginText();
		// Drawing the image in the PDF document
		 float d=(mediaBox.getHeight() - 2 * 72);
		 System.out.println(" mediaBox.getHeight() "+ mediaBox.getHeight()+"  mediaBox.getHeight() - 2 * 72  -"+d);
		 contents.drawImage(pdImage, 390, (mediaBox.getHeight() -557.2f-20),90,38);
//409.5 : 557.2
		System.out.println("Image inserted");

		// Closing the PDPageContentStream object
		 contents.close();

		// Saving the document
		 doc.save("C:\\Users\\mduraimani\\Desktop\\ioc\\sample.pdf");

		// Closing the document
		doc.close();

	}

	private static void createPDF(String text, PDPageContentStream contentStream2) throws IOException {
		// File file = new File("C:\\Users\\mduraimani\\Desktop\\ioc\\Temp. Trial file
		// print1pg- Copy.pdf");
		PDDocument doc = new PDDocument();

		// Retrieving the page
		PDPage blankPage = new PDPage();

		// Adding the blank page to the document
		doc.addPage(blankPage);

		PDPageContentStream contentStream = new PDPageContentStream(doc, blankPage);

		// Begin the Content stream
		contentStream.beginText();

		// Setting the font to the Content stream
		// contentStream.setFont( PDType1Font.COURIER, 16 );

		// Setting the leading
		// contentStream.setLeading(14.5f);

		// Setting the position for the line
		// contentStream.newLineAtOffset(25, 725);

		// Adding text in the form of string
		contentStream.showText(text);
		// contentStream.newLine();
		// Ending the content stream
		contentStream.endText();

		System.out.println("Content added");

		// Closing the content stream
		contentStream.close();

		// Saving the document
		doc.save("C:\\\\Users\\\\mduraimani\\\\Desktop\\\\ioc\\\\my_doc.pdf");

		// Closing the document
		doc.close();
	}
}
