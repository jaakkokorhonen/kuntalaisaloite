package fi.om.municipalityinitiative.service.email;

import fi.om.municipalityinitiative.util.Locales;
import fi.om.municipalityinitiative.web.Urls;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class MailSendingEmailServiceStatusTest extends MailSendingEmailServiceTestBase {

    private Urls urls;

    @Before
    public void setup() {
        super.setup();
        urls = Urls.get(Locales.LOCALE_FI);
    }

    @Test
    public void om_accept_initiative_sets_subject_and_contains_all_information() throws Exception {
        emailService.sendStatusEmail(initiativeId(), EmailMessageType.ACCEPTED_BY_OM);

        assertThat(javaMailSenderFake.getSingleRecipient(), is(AUTHOR_EMAIL));
        assertThat(javaMailSenderFake.getSingleSentMessage().getSubject(), is("Kuntalaisaloitteesi on hyväksytty / Ditt invånarinitiativ har godkänts"));
        assertThat(javaMailSenderFake.getMessageContent().html, containsString(MODERATOR_COMMENT));
    }

    @Test
    public void om_accept_initiative_and_send_to_municipality_sets_subject_and_contains_all_information() throws Exception {
        emailService.sendStatusEmail(initiativeId(), EmailMessageType.ACCEPTED_BY_OM_AND_SENT);
        assertThat(javaMailSenderFake.getSingleRecipient(), is(AUTHOR_EMAIL));
        assertThat(javaMailSenderFake.getSingleSentMessage().getSubject(), is("Kuntalaisaloitteesi on hyväksytty ja lähetetty kuntaan / Ditt invånarinitiativ har godkänts och skickats till kommunen"));
        assertThat(javaMailSenderFake.getMessageContent().html, containsString(INITIATIVE_MUNICIPALITY));
        assertThat(javaMailSenderFake.getMessageContent().html, containsString(urls.view(initiativeId())));
        assertThat(javaMailSenderFake.getMessageContent().html, containsString("Kuntalaisaloitteesi on julkaistu Kuntalaisaloite.fi-palvelussa ja lähetetty kuntaan"));
        // TODO: assertThat(getMessageContent().html, containsString("SV Kuntalaisaloitteesi on julkaistu Kuntalaisaloite.fi-palvelussa ja lähetetty kuntaan"));
    }

    @Test
    public void om_reject_initiative_sets_subject_and_contains_all_information() throws Exception {
        emailService.sendStatusEmail(initiativeId(), EmailMessageType.REJECTED_BY_OM);

        assertThat(javaMailSenderFake.getSingleRecipient(), is(AUTHOR_EMAIL));
        assertThat(javaMailSenderFake.getSingleSentMessage().getSubject(), is("Kuntalaisaloitteesi on palautettu / Ditt invånarinitiativ har returnerats"));
        assertThat(javaMailSenderFake.getMessageContent().html, containsString(MODERATOR_COMMENT));
    }

    @Test
    public void author_publish_and_start_collecting_sets_subject_and_contains_all_information() throws Exception {
        emailService.sendStatusEmail(initiativeId(), EmailMessageType.PUBLISHED_COLLECTING);
        assertThat(javaMailSenderFake.getSingleRecipient(), is(AUTHOR_EMAIL));
        assertThat(javaMailSenderFake.getSingleSentMessage().getSubject(), is("Aloitteesi on julkaistu ja siihen kerätään osallistujia Kuntalaisaloite.fi-palvelussa / Ditt initiativ har publicerats och namninsamling pågår i webbtjänsten Invånarinitiativ.fi"));
        assertThat(javaMailSenderFake.getMessageContent().html, containsString(INITIATIVE_NAME));
        assertThat(javaMailSenderFake.getMessageContent().html, containsString(urls.view(initiativeId())));
    }

    @Test
    public void author_publish_and_send_to_municipality_sets_subject_and_contains_all_information() throws Exception {
        emailService.sendStatusEmail(initiativeId(), EmailMessageType.SENT_TO_MUNICIPALITY);
        assertThat(javaMailSenderFake.getSingleRecipient(), is(AUTHOR_EMAIL));
        assertThat(javaMailSenderFake.getMessageContent().html, containsString(INITIATIVE_NAME));
        assertThat(javaMailSenderFake.getSingleSentMessage().getSubject(), is("Aloitteesi on lähetetty kuntaan / Ditt initiativ har skickats till kommunen"));
    }


}