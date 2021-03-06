package fi.om.municipalityinitiative.dto.service;

import fi.om.municipalityinitiative.dto.ui.AuthorInvitationUIConfirmDto;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.dto.ui.ParticipantUICreateDto;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.dto.user.User;
import fi.om.municipalityinitiative.dto.user.VerifiedUser;
import fi.om.municipalityinitiative.service.id.VerifiedUserId;
import fi.om.municipalityinitiative.util.Membership;
import fi.om.municipalityinitiative.util.ReflectionTestUtils;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;

public class ParticipantCreateDtoTest {

    @Test
    public void parse_from_ParticipantUiCreateDto() throws Exception {
        ParticipantUICreateDto uiCreateDto = ReflectionTestUtils.modifyAllFields(new ParticipantUICreateDto());

        ParticipantCreateDto participantCreateDto = ParticipantCreateDto.parse(uiCreateDto, 117L);
        assertThat(participantCreateDto.getHomeMunicipality(), is(uiCreateDto.getHomeMunicipality()));
        assertThat(participantCreateDto.getMunicipalityInitiativeId(), is(117L));
        assertThat(participantCreateDto.isShowName(), is(uiCreateDto.getShowName()));
        assertThat(participantCreateDto.getParticipantName(), is(uiCreateDto.getParticipantName()));
        assertThat(participantCreateDto.getEmail(), is(uiCreateDto.getParticipantEmail()));
        assertThat(participantCreateDto.getMunicipalMembership(), is(uiCreateDto.getMunicipalMembership()));

        ReflectionTestUtils.assertNoNullFields(participantCreateDto);
    }

    @Test
    public void municipal_membership_is_none_if_municipalities_are_the_same() {
        ParticipantUICreateDto participantUICreateDto = new ParticipantUICreateDto();
        participantUICreateDto.assignInitiativeMunicipality(1L);
        participantUICreateDto.setHomeMunicipality(1L);
        participantUICreateDto.setMunicipalMembership(Membership.company);

        assertThat(ParticipantCreateDto.parse(participantUICreateDto, null).getMunicipalMembership(), is(Membership.none));
    }

    @Test
    public void municipal_membership_is_given_if_municipalities_are_not_the_same() {
        ParticipantUICreateDto participantUICreateDto = new ParticipantUICreateDto();
        participantUICreateDto.assignInitiativeMunicipality(1L);
        participantUICreateDto.setHomeMunicipality(2L);
        participantUICreateDto.setMunicipalMembership(Membership.company);

        assertThat(ParticipantCreateDto.parse(participantUICreateDto, null).getMunicipalMembership(), is(Membership.company));
    }

    @Test
    public void parse_from_invitationConfirmDto() {
        AuthorInvitationUIConfirmDto invitationUIConfirmDto = ReflectionTestUtils.modifyAllFields(new AuthorInvitationUIConfirmDto());

        ParticipantCreateDto participantCreateDto = ParticipantCreateDto.parse(invitationUIConfirmDto, 121L);

        assertThat(participantCreateDto.getMunicipalMembership(), is(invitationUIConfirmDto.getMunicipalMembership()));
        assertThat(participantCreateDto.getEmail(), is(invitationUIConfirmDto.getContactInfo().getEmail()));
        assertThat(participantCreateDto.getHomeMunicipality(), is(invitationUIConfirmDto.getHomeMunicipality()));
        assertThat(participantCreateDto.getMunicipalityInitiativeId(), is(121L));
        assertThat(participantCreateDto.getParticipantName(), is(invitationUIConfirmDto.getContactInfo().getName()));

        ReflectionTestUtils.assertNoNullFields(participantCreateDto);

    }

    @Test
    public void parse_from_invitationConfirmDto_and_LoginUserHolder() {
        ParticipantUICreateDto uiCreateDto = ReflectionTestUtils.modifyAllFields(new ParticipantUICreateDto());

        long randomId = 123L;
        String randomHash = "dfjafdoaslfkj";
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setName("Paavo Pesusieni");


        VerifiedUser user = User.verifiedUser(new VerifiedUserId(randomId), randomHash, contactInfo, null, null, null);

        ParticipantCreateDto participantCreateDto = ParticipantCreateDto.parseParticipantFromVerifiedUser(uiCreateDto, new LoginUserHolder<>(user).getVerifiedUser(), 121L);

        assertThat(participantCreateDto.getMunicipalMembership(), is(uiCreateDto.getMunicipalMembership()));
        assertThat(participantCreateDto.getEmail(), isEmptyOrNullString());
        assertThat(participantCreateDto.getHomeMunicipality(), is(uiCreateDto.getHomeMunicipality()));
        assertThat(participantCreateDto.getMunicipalityInitiativeId(), is(121L));
        assertThat(participantCreateDto.getParticipantName(), is(user.getContactInfo().getName()));


    }

}
