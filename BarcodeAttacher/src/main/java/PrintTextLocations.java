import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class PrintTextLocations extends PDFTextStripper {

    public static StringBuilder tWord = new StringBuilder();
    public static String seek;
    public static String[] seekA;
    public static List wordList = new ArrayList();
    public static boolean is1stChar = true;
    public static boolean lineMatch;
    public static int pageNo = 1;
    public static double lastYVal;

    public PrintTextLocations()
            throws IOException {
        super.setSortByPosition(true);
    }

    public static void main(String[] args)
            throws Exception {
        PDDocument document = null;
        seekA = (String[]) Arrays.asList("Barcode","share").toArray();
        seek = "Barcode";
        try {
            File input = new File("C:\\Users\\mduraimani\\Desktop\\ioc\\Temp. Trial file print1pg- Copy1.pdf");
            document = PDDocument.load(input);
            if (document.isEncrypted()) {
                try {
                  //  document.decrypt("");
                } catch (Exception e) {//InvalidPasswordException
                    System.err.println("Error: Document is encrypted with a password.");
                    System.exit(1);
                }
            }
            PrintTextLocations printer = new PrintTextLocations();
            PDPageTree allPages = document.getDocumentCatalog().getPages();

            for (int i = 0; i < allPages.getCount(); i++) {
                PDPage page = (PDPage) allPages.get(i);
               // PDStream contents = page;

                if (page != null) {
                    printer.processPages(allPages);
                }
                pageNo += 1;
            }
        } finally {
            if (document != null) {
                System.out.println(wordList);
                document.close();
            }
        }
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        String tChar = text.getUnicode();//getCharacter();
        System.out.println("String[" + text.getXDirAdj() + ","
                + text.getYDirAdj() + " fs=" + text.getFontSize() + " xscale="
                + text.getXScale() + " height=" + text.getHeightDir() + " space="
                + text.getWidthOfSpace() + " width="
                + text.getWidthDirAdj() + "]" + text.getUnicode()
                //getCharacter()
                );
        String REGEX = "[,.\\[\\](:;!?)/]";
        char c = tChar.charAt(0);
        lineMatch = matchCharLine(text);
        if ((!tChar.matches(REGEX)) && (!Character.isWhitespace(c))) {
            if ((!is1stChar) && (lineMatch == true)) {
                appendChar(tChar);
            } else if (is1stChar == true) {
                setWordCoord(text, tChar);
            }
        } else {
            endWord();
        }
    }

    protected void appendChar(String tChar) {
        tWord.append(tChar);
        is1stChar = false;
    }

    protected void setWordCoord(TextPosition text, String tChar) {
        tWord.append("(").append(pageNo).append(")[").append(roundVal(Float.valueOf(text.getXDirAdj()))).append(" : ").append(roundVal(Float.valueOf(text.getYDirAdj()))).append("] ").append(tChar);
        is1stChar = false;
    }

    protected void endWord() {
        String newWord = tWord.toString().replaceAll("[^\\x00-\\x7F]", "");
        String sWord = newWord.substring(newWord.lastIndexOf(' ') + 1);
        if (!"".equals(sWord)) {
            if (Arrays.asList(seekA).contains(sWord)) {
                wordList.add(newWord);
            } else if ("SHOWMETHEMONEY".equals(seek)) {
                wordList.add(newWord);
            }
        }
        tWord.delete(0, tWord.length());
        is1stChar = true;
    }

    protected boolean matchCharLine(TextPosition text) {
        Double yVal = roundVal(Float.valueOf(text.getYDirAdj()));
        if (yVal.doubleValue() == lastYVal) {
            return true;
        }
        lastYVal = yVal.doubleValue();
        endWord();
        return false;
    }

    protected Double roundVal(Float yVal) {
        DecimalFormat rounded = new DecimalFormat("0.0'0'");
        Double yValDub = new Double(rounded.format(yVal));
        return yValDub;
    }
}