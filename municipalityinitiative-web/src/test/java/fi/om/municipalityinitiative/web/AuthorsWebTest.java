package fi.om.municipalityinitiative.web;

import static fi.om.municipalityinitiative.web.MessageSourceKeys.MSG_SUCCESS_INVITATION_SENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.dto.service.AuthorInvitation;
import org.junit.Before;
import org.junit.Test;

import fi.om.municipalityinitiative.util.InitiativeState;
import fi.om.municipalityinitiative.util.InitiativeType;
import org.openqa.selenium.By;

public class AuthorsWebTest  extends WebTestBase {


    /**
     * Localization keys as constants.
     */
    private static final String MSG_SUCCESS_SEND = "success.send.title";
    private static final String MSG_ADD_AUTHORS_LINK = "addAuthors.link";
    private static final String MSG_BTN_ADD_AUTHOR = "action.addAuthor";
    private static final String MSG_BTN_SEND = "action.sendInvitation";
    private static final String MSG_INVITATION_UNCONFIRMED = "invitation.unconfirmed";
    
    
    /**
     * Form values as constants.
     */
    private static final String MUNICIPALITY_1 = "Vantaa";
    private static final String CONTACT_NAME = "Teppo Testaaja";
    private static final String CONTACT_EMAIL = "test@test.com";
    public static final String CONTACT_ADDRESS = "Joku Katu jossain 89";
    public static final String CONTACT_PHONE = "040111222";
    public static final String HYLKÄÄ_KUTSU = "invitation.reject";
    public static final String HYVÄKSY_KUTSUN_HYLKÄÄMINEN = "invitation.reject.confirm";
    private Long municipalityId;
    private Long initiativeId;

    @Before
    public void setup() {
        testHelper.dbCleanup();
        municipalityId = testHelper.createTestMunicipality(MUNICIPALITY_1);
        initiativeId = testHelper.create(municipalityId, InitiativeState.ACCEPTED, InitiativeType.COLLABORATIVE);
    }

    @Test
    public void add_author() {

        loginAsAuthorForLastTestHelperCreatedInitiative();
        
        open(urls.management(initiativeId));
        clickLinkContaining(getMessage(MSG_ADD_AUTHORS_LINK));
        
        clickLinkContaining(getMessage(MSG_BTN_ADD_AUTHOR));
        
        inputText("authorName", CONTACT_NAME);
        inputText("authorEmail", CONTACT_EMAIL);
        
        getElemContaining(getMessage(MSG_BTN_SEND), "button").click();
        
        assertMsgContainedByClass("msg-success", MSG_SUCCESS_INVITATION_SENT);
        assertTextContainedByXPath("//div[@class='view-block last']//span[@class='status']", getMessage(MSG_INVITATION_UNCONFIRMED));
    }
    
    @Test
    public void author_invitation_acceptance_dialog_has_given_values_prefilled_and_submitting_logs_user_in_as_author_with_given_information() throws InterruptedException {
        AuthorInvitation invitation = testHelper.createInvitation(initiativeId, CONTACT_NAME, CONTACT_EMAIL);
        open(urls.invitation(invitation.getInitiativeId(), invitation.getConfirmationCode()));

        clickDialogButton("Hyväksy kutsu");

        assertThat(getElementByLabel("Etu- ja sukunimi", "input").getAttribute("value"), containsString(CONTACT_NAME));
        assertThat(getElementByLabel("Sähköpostiosoite", "input").getAttribute("value"), containsString(CONTACT_EMAIL));

        getElementByLabel("Puhelin", "input").sendKeys(CONTACT_PHONE);
        getElementByLabel("Osoite", "textarea").sendKeys(CONTACT_ADDRESS);
        clickDialogButton("Hyväksy ja tallenna tiedot");

        assertTextContainedByClass("msg-success", "Liittymisesi vastuuhenkilöksi on nyt vahvistettu ja olet kirjautunut sisään palveluun.");

        clickDialogButton("Muokkaa aloitetta");
        assertThat(getElementByLabel("Etu- ja sukunimi", "input").getAttribute("value"), containsString(CONTACT_NAME));
        assertThat(getElementByLabel("Sähköpostiosoite", "input").getAttribute("value"), containsString(CONTACT_EMAIL));
        assertThat(getElementByLabel("Puhelin", "input").getAttribute("value"), containsString(CONTACT_PHONE));
        assertThat(getElementByLabel("Osoite", "textarea").getText(), containsString(CONTACT_ADDRESS));

        assertInvitationPageIsGone(invitation);

    }

    @Test
    public void reject_author_invitation() throws InterruptedException {
        overrideDriverToFirefox(true);
        AuthorInvitation invitation = testHelper.createInvitation(initiativeId, CONTACT_NAME, CONTACT_EMAIL);
        open(urls.invitation(invitation.getInitiativeId(), invitation.getConfirmationCode()));

        clickDialogButtonMsg(HYLKÄÄ_KUTSU);
        clickDialogButtonMsg(HYVÄKSY_KUTSUN_HYLKÄÄMINEN);

        assertTextContainedByClass("msg-success", "Olet hylännyt kutsun vastuuhenkilöksi eikä tietojasi ole tallennettu aloitteeseen");

        assertInvitationPageIsGone(invitation);

    }
    
    @Test
    public void author_removes_participant(){
        Long publishedInitiativeId = testHelper.create(municipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);
        
        testHelper.createParticipant(new TestHelper.AuthorDraft(publishedInitiativeId, municipalityId));
        
        loginAsAuthorForLastTestHelperCreatedInitiative();
        open(urls.management(publishedInitiativeId));
        
        clickLinkContaining("Osallistujahallinta");
        clickLinkContaining("Poista osallistuja");
        
        // NOTE: We could assert that modal has correct Participant details,
        //       but as DOM is updated after the modal is loaded we would need a tiny delay for that
        
        getElemContaining("Poista", "button").click();
        
        assertTextContainedByClass("msg-success", "Osallistuja poistettu");
        
    }
    
    @Test
    public void author_removes_author(){
        testHelper.createAuthorAndParticipant(new TestHelper.AuthorDraft(initiativeId, municipalityId));
        
        loginAsAuthorForLastTestHelperCreatedInitiative();
        open(urls.management(initiativeId));
        
        clickLinkContaining("Lisää vastuuhenkilöitä");
        clickLinkContaining("Poista vastuuhenkilö");
        
        // NOTE: We could assert that modal has correct Author details,
        //       but as DOM is updated after the modal is loaded we would need a tiny delay for that
        
        getElemContaining("Poista vastuuhenkilö", "button").click();
        
        assertTextContainedByClass("msg-success", "Vastuuhenkilö poistettu");
    }

    private void assertInvitationPageIsGone(AuthorInvitation invitation) {
        open(urls.invitation(invitation.getInitiativeId(), invitation.getConfirmationCode()));
        assertThat(getElement(By.tagName("h1")).getText(), is(getMessage("error.410.invitation.not.valid.error.message.title")));
    }

}