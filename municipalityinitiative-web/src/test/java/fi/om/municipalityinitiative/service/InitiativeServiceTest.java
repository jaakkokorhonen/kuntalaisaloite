package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.dto.SendToMunicipalityDto;
import fi.om.municipalityinitiative.exceptions.NotCollectableException;
import fi.om.municipalityinitiative.newdao.InitiativeDao;
import fi.om.municipalityinitiative.newdao.MunicipalityDao;
import fi.om.municipalityinitiative.newdto.ui.ContactInfo;
import fi.om.municipalityinitiative.newdto.ui.InitiativeUICreateDto;
import fi.om.municipalityinitiative.newdto.ui.InitiativeViewInfo;
import fi.om.municipalityinitiative.util.Maybe;
import fi.om.municipalityinitiative.web.Urls;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class InitiativeServiceTest {

    private InitiativeService service;
    private InitiativeDao initiativeDao;

    @BeforeClass
    public static void initUrls() {
        // This is pretty stupid that this is needed here. Static configurations are...
        Urls.initUrls("baseUrl");
    }

    @Before
    public void setup() {
        initiativeDao = mock(InitiativeDao.class);
        service = new InitiativeService();
        service.initiativeDao = initiativeDao;
        service.municipalityDao = mock(MunicipalityDao.class);
        service.emailService = mock(EmailService.class);
    }

    @Test
    public void fails_sending_to_municipality_if_not_collectable() {

        InitiativeViewInfo initiativeViewInfo = new InitiativeViewInfo();
        initiativeViewInfo.setManagementHash(Maybe.<String>absent());
        stub(initiativeDao.getById(any(Long.class))).toReturn(initiativeViewInfo);

        try {
            service.sendToMunicipality(0L, null, "anyhashcode", null);
            fail("Should have thrown exception");
        } catch (NotCollectableException e) {
            assertThat(e.getMessage(), containsString("Initiative is not collectable"));
        }
    }

    @Test
    public void fails_sending_to_municipality_if_already_sent() {

        InitiativeViewInfo initiativeViewInfo = new InitiativeViewInfo();
        initiativeViewInfo.setManagementHash(Maybe.of("anyHash"));
        initiativeViewInfo.setSentTime(Maybe.of(new DateTime()));
        stub(initiativeDao.getById(any(Long.class))).toReturn(initiativeViewInfo);

        try {
            service.sendToMunicipality(0L, null, "anyOtherHashCode", null);
            fail("Should have thrown exception");
        } catch (NotCollectableException e) {
            assertThat(e.getMessage(), containsString("Initiative already sent"));
        }
    }

    @Test
    public void fails_sending_to_municipality_if_hashcode_does_not_match() {

        InitiativeViewInfo initiativeViewInfo = new InitiativeViewInfo();
        initiativeViewInfo.setManagementHash(Maybe.of("some hash"));
        stub(initiativeDao.getById(any(Long.class))).toReturn(initiativeViewInfo);

        try {
            service.sendToMunicipality(0L, null, "another hash", null);
            fail("Should have thrown exception");
        } catch (AccessDeniedException e) {
            assertThat(e.getMessage(), containsString("Invalid initiative verifier"));
        }
    }

    @Test
    public void succeeds_in_sending_to_municipality() {

        InitiativeViewInfo initiativeViewInfo = new InitiativeViewInfo();
        initiativeViewInfo.setManagementHash(Maybe.of("hashCode"));
        stub(initiativeDao.getById(any(Long.class))).toReturn(initiativeViewInfo);

        ContactInfo newContactInfo = new ContactInfo();
        SendToMunicipalityDto sendToMunicipalityDto = new SendToMunicipalityDto();
        sendToMunicipalityDto.setContactInfo(newContactInfo);

        service.sendToMunicipality(0L, sendToMunicipalityDto, "hashCode", null);
        verify(initiativeDao).markAsSended(0L, newContactInfo);
    }

    @Test
    public void set_contact_info_to_SendToMunicipalityDto() {

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setPhone("phone");
        stub(initiativeDao.getContactInfo(any(Long.class))).toReturn(contactInfo);

        assertThat(service.getSendToMunicipalityData(0L).getContactInfo().getPhone(), is("phone"));
    }

    private InitiativeUICreateDto createDtoFillAllFields() {
        InitiativeUICreateDto createDto = new InitiativeUICreateDto();
        createDto.setFranchise(true);
        createDto.setMunicipalMembership(true);
        createDto.setHomeMunicipality(7L);
        createDto.setMunicipality(15L);
        createDto.setName("name field");
        createDto.setProposal("proposal");
        createDto.setShowName(true);
        createDto.setContactInfo(new ContactInfo());
        createDto.getContactInfo().setAddress("contact address");
        createDto.getContactInfo().setEmail("contact@email.com");
        createDto.getContactInfo().setName("contact name");
        createDto.getContactInfo().setPhone("123456789");
        return createDto;
    }

}
