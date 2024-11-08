package org.avasthi.java.cli;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.*;

public class PDFParser {
    public static void main(String[] args) throws IOException {

        PDFTextStripper stripper = new PDFTextStripper()
        {
            @Override
            protected void startPage(PDPage page) throws IOException
            {
                startOfLine = true;
                super.startPage(page);
            }

            @Override
            protected void writeLineSeparator() throws IOException
            {
                startOfLine = true;
                super.writeLineSeparator();
            }

/*            @Override
            protected void writeString(String text, java.util.List<TextPosition> textPositions) throws IOException {
                if (startOfLine)
                {
                    fw.println();
                    startOfLine = false;
                }
                fw.print(text);
                fw.print(' ');
            }*/

            boolean startOfLine = true;
        };
        File output = new File(args[0] + "1.csv");
        PrintWriter fw = new PrintWriter(output);
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(args[0])))
        {
                stripper.writeText(document, fw);
        }
        fw.close();
        BufferedReader br  = new BufferedReader(new FileReader(output));
        String line = null;
        PrintWriter finalOut = new PrintWriter(new File(args[0].replace("pdf", "txt")));
        while ((line = br.readLine()) != null) {
            StringBuilder sb = new StringBuilder(line.replaceFirst(" ", "|"));

            int lastIndex = sb.lastIndexOf(" ");
            sb.replace(lastIndex, lastIndex + 1, "|");
            finalOut.println(sb.toString());
        }
        finalOut.close();
    }
}
