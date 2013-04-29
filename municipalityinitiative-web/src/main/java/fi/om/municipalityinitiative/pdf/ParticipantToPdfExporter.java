package fi.om.municipalityinitiative.pdf;

import com.google.common.collect.Lists;
import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import fi.om.municipalityinitiative.newdto.service.Initiative;
import fi.om.municipalityinitiative.newdto.service.Municipality;
import fi.om.municipalityinitiative.newdto.service.Participant;
import fi.om.municipalityinitiative.util.Locales;
import fi.om.municipalityinitiative.util.Membership;
import org.joda.time.DateTime;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class ParticipantToPdfExporter {

    public static final String DATETIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    public static final String DATE_FORMAT = "dd.MM.yyyy";
    public static final FontFamily FONT_FAMILY = Font.FontFamily.HELVETICA;

    private static Font mainTitle = new Font(FONT_FAMILY, 16, Font.BOLD);
    private static Font subTitle = new Font(FONT_FAMILY, 14, Font.BOLD);
    private static Font redFont = new Font(FONT_FAMILY, 12, Font.NORMAL, BaseColor.RED);
    private static Font bodyText = new Font(FONT_FAMILY, 10, Font.NORMAL);
    private static Font bodyTextItalic = new Font(FONT_FAMILY, 10, Font.ITALIC);
    private static Font smallBold = new Font(FONT_FAMILY, 10, Font.BOLD);


    private Document document;

    private final Initiative initiative;
    private final List<Participant> participants;

    public ParticipantToPdfExporter(Initiative initiative, List<Participant> participants) {
        this.initiative = initiative;
        this.participants = participants;
    }

    public void createPdf(OutputStream outputStream) {
        if (document != null) {
            document.close();
        }
        try {
            document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            addMetaData();
            addTitlePage();
            addParticipants();

            document.close();
            document = null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private void addParticipants() throws DocumentException {
        //To change body of created methods use File | Settings | File Templates.

        Paragraph preface = new Paragraph();

        addEmptyLine(preface, 2);
        preface.add(new Paragraph("Osallistujat", subTitle));

        addEmptyLine(preface, 1);
        createTable(preface, participants);

        document.add(preface);
    }

    // TODO: Localize

    /*
     * iText License:
     * - http://itextpdf.com/terms-of-use/agpl.php
     * - In accordance with Section 7(b) of the GNU Affero General Public License,
     *   you must retain the producer line in every PDF that is created or manipulated using iText.
     */

    // iText allows to add metadata to the PDF which can be viewed in your Adobe
    // Reader
    // under File -> Properties
    private void addMetaData() {
        document.addTitle("Kuntalaisaloite " + initiative.getMunicipality().getLocalizedName(Locales.LOCALE_FI));
        document.addSubject(initiative.getName());
        document.addKeywords("Java, PDF, iText"); // TODO: Remove
        document.addAuthor("Lars Vogel");
        document.addCreator("Lars Vogel");
    }

    private void addTitlePage()
            throws DocumentException {
        Paragraph preface = new Paragraph();
        // We add one empty line
//        addEmptyLine(preface, 1);
        // Lets write a big header
        Municipality municipality = initiative.getMunicipality();
        preface.add(new Paragraph("Kuntalaisaloite - " + municipality.getLocalizedName(Locales.LOCALE_FI) + " / Invånarinitiativ - " + municipality.getLocalizedName(Locales.LOCALE_SV), mainTitle));
        preface.add(new Paragraph("Aloite lähetetty kuntaan / Initiativet skickats till kommun " + new DateTime().toString(DATETIME_FORMAT), bodyText));
        addEmptyLine(preface, 1);

        // Will create: Report generated by: _name, _date
        preface.add(new Paragraph("Aloitteen otsikko / Initiativets titel", subTitle));
        preface.add(new Paragraph(initiative.getName(), bodyText));

        document.add(preface);

    }

    private static void createTable(Paragraph subCatPart, List<Participant> participants)
            throws DocumentException {
        PdfPTable table = new PdfPTable(5);

        table.setWidthPercentage(100);
        table.setWidths(new int[] {6, 10, 42, 18, 12});
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        table.addCell(createCell(" ", true));
        table.addCell(createCell("Päivämäärä\nDatum", true));
        table.addCell(createCell("Nimi\nNamn", true));
        table.addCell(createCell("Kotikunta\nHemkommun", true));
        table.addCell(createCell("Jäsenyys", true));

        table.setHeaderRows(1);

        int count = 0;
        participants = Lists.reverse(participants);
        for (Participant participant : participants) {
            ++count;
            table.addCell(createCell(String.valueOf(count), false));
            table.addCell(createCell(participant.getParticipateDate().toString(DATE_FORMAT), false));
            table.addCell(createCell(participant.getName(), false));
            table.addCell(createCell(participant.getHomeMunicipality().getNameFi() + ", " + participant.getHomeMunicipality().getNameSv(), false));
            table.addCell(createCell(participant.getMembership() == Membership.none ? "" : participant.getMembership().name(), false));
        }

        subCatPart.add(table);
    }

    private static PdfPCell createCell(String header, boolean tableHead) {
        Font fontStyle = bodyText;

        if (tableHead) {
            fontStyle = smallBold;
        }

        PdfPCell c1 = new PdfPCell(new Phrase(header, fontStyle));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c1.setPadding(4);

        return c1;
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
