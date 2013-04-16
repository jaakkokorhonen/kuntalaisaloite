package fi.om.municipalityinitiative.dao;

import fi.om.municipalityinitiative.conf.IntegrationTestConfiguration;
import fi.om.municipalityinitiative.newdao.ParticipantDao;
import fi.om.municipalityinitiative.newdto.service.Participant;
import fi.om.municipalityinitiative.newdto.service.ParticipantCreateDto;
import fi.om.municipalityinitiative.newdto.ui.ParticipantCount;
import fi.om.municipalityinitiative.sql.QParticipant;
import fi.om.municipalityinitiative.util.InitiativeState;
import fi.om.municipalityinitiative.util.InitiativeType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.List;

import static fi.om.municipalityinitiative.util.TestUtil.precondition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
public class JdbcParticipantDaoTest {

    public static final String PARTICIPANTS_NAME = "Participants Name";
    public static final String PARTICIPANT_EMAIL = "participant@example.com";
    public static final boolean PARTICIPANT_FRANCHISE = true;
    public static final boolean PARTICIPANT_SHOW_NAME = true;
    public static final String CONFIRMATION_CODE = "confirmationCode";
    public static final String ALREADY_CONFIRMED = null;

    @Resource
    ParticipantDao participantDao;

    @Resource
    TestHelper testHelper;
    private Long testMunicipalityId;
    private Long testInitiativeId;
    private Long otherMunicipalityId;

    @Before
    public void setup() {
        testHelper.dbCleanup();
        testMunicipalityId = testHelper.createTestMunicipality("Municipality");
        testInitiativeId = testHelper.create(testMunicipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);

        otherMunicipalityId = testHelper.createTestMunicipality("Other Municipality");
    }

    @Test
    public void adds_new_participants() {
        precondition(testHelper.countAll(QParticipant.participant), is(1L));
        participantDao.create(participantCreateDto(), ALREADY_CONFIRMED);
        assertThat(testHelper.countAll(QParticipant.participant), is(2L));
    }

    @Test
    public void participant_information_is_saved() {
        precondition(participantDao.findPublicParticipants(testInitiativeId), hasSize(1));

        participantDao.create(participantCreateDto(), ALREADY_CONFIRMED);
        List<Participant> allParticipants = participantDao.findPublicParticipants(testInitiativeId);
        assertThat(allParticipants, hasSize(2));

        Participant participant = allParticipants.get(0);
        assertThat(participant.getName(), is(PARTICIPANTS_NAME));
        assertThat(participant.isFranchise(), is(PARTICIPANT_FRANCHISE));
        assertThat(participant.getHomeMunicipality().getId(), is(otherMunicipalityId));
        assertThat(participant.getParticipateDate(), is(notNullValue()));
        assertThat(participant.getEmail(), is(PARTICIPANT_EMAIL));
    }

    @Test
    public void counts_all_supports_according_to_right_of_voting_and_publicity_of_names() {
        Long municipalityId = testHelper.createTestMunicipality("Other municipality");
        Long initiativeId = testHelper.createTestInitiative(municipalityId);

        //createParticipant(initiativeId, true, true); // This is the default author created by testHelper

        createConfirmedParticipant(initiativeId, true, false);
        createConfirmedParticipant(initiativeId, true, false);

        createConfirmedParticipant(initiativeId, false, true);
        createConfirmedParticipant(initiativeId, false, true);
        createConfirmedParticipant(initiativeId, false, true);

        createConfirmedParticipant(initiativeId, false, false);
        createConfirmedParticipant(initiativeId, false, false);
        createConfirmedParticipant(initiativeId, false, false);
        createConfirmedParticipant(initiativeId, false, false);

        ParticipantCount participantCount = participantDao.getParticipantCount(initiativeId);
        assertThat(participantCount.getFranchise().getPublicNames(), is(1L));
        assertThat(participantCount.getFranchise().getPrivateNames(), is(2L));
        assertThat(participantCount.getNoFranchise().getPublicNames(), is(3L));
        assertThat(participantCount.getNoFranchise().getPrivateNames(), is(4L));

    }

    @Test
    public void wont_fail_if_counting_supports_when_no_supports() {
        ParticipantCount participantCount = participantDao.getParticipantCount(testInitiativeId);
        assertThat(participantCount.getFranchise().getPublicNames(), is(1L)); // This is the default author
        assertThat(participantCount.getFranchise().getPrivateNames(), is(0L));
        assertThat(participantCount.getNoFranchise().getPublicNames(), is(0L));
        assertThat(participantCount.getNoFranchise().getPrivateNames(), is(0L));
    }

    @Test
    public void getPublicParticipants_returns_public_names() {

        Long municipalityId = testHelper.createTestMunicipality("Other municipality");
        Long initiativeId = testHelper.createTestInitiative(municipalityId, "Any title", false, false);

        createConfirmedParticipant(initiativeId, false, false, "no right no public");
        createConfirmedParticipant(initiativeId, true, false, "yes right no public");
        createConfirmedParticipant(initiativeId, false, true, "no right yes public");
        createConfirmedParticipant(initiativeId, true, true, "yes right yes public");

        List<Participant> participants = participantDao.findPublicParticipants(initiativeId);

        assertThat(participants, hasSize(2));
        assertThat(participants.get(0).getName(), is("yes right yes public"));
        assertThat(participants.get(1).getName(), is("no right yes public"));
    }

    @Test
    public void getAllParticipants_returns_public_and_private_names() {

        Long municipalityId = testHelper.createTestMunicipality("Other municipality");
        Long initiativeId = testHelper.createTestInitiative(municipalityId, "Any title", false, false);

        createConfirmedParticipant(initiativeId, false, false, "no right no public");
        createConfirmedParticipant(initiativeId, true, false, "yes right no public");
        createConfirmedParticipant(initiativeId, false, true, "no right yes public");
        createConfirmedParticipant(initiativeId, true, true, "yes right yes public");

        List<Participant> participants = participantDao.findAllParticipants(initiativeId);

        assertThat(participants, hasSize(5)); // Four and the creator

    }

    @Test
    public void getPublicParticipants_returns_only_confirmed_participants() {
        precondition(participantDao.findPublicParticipants(testInitiativeId), hasSize(1));
        ParticipantCreateDto newParticipant = participantCreateDto();

        participantDao.create(newParticipant, CONFIRMATION_CODE);

        String confirmedParticipantName = "Some Confirmed Participant";
        newParticipant.setParticipantName(confirmedParticipantName);
        participantDao.create(newParticipant, ALREADY_CONFIRMED);

        List<Participant> publicParticipants = participantDao.findPublicParticipants(testInitiativeId);

        assertThat(publicParticipants, hasSize(2));
        assertThat(publicParticipants.get(0).getName(), is(confirmedParticipantName));

    }

    @Test
    public void getAllParticipants_returns_only_confirmed_participants() {
        precondition(participantDao.findAllParticipants(testInitiativeId), hasSize(1));
        ParticipantCreateDto newParticipant = participantCreateDto();

        participantDao.create(newParticipant, CONFIRMATION_CODE);

        String confirmedParticipantName = "Some Confirmed Participant";
        newParticipant.setParticipantName(confirmedParticipantName);
        participantDao.create(newParticipant, ALREADY_CONFIRMED);

        List<Participant> allParticipants = participantDao.findAllParticipants(testInitiativeId);

        assertThat(allParticipants, hasSize(2));
        assertThat(allParticipants.get(0).getName(), is(confirmedParticipantName));
    }

    @Test
    public void getParticipantCount_counts_only_confirmed_participants() {

        precondition(participantDao.getParticipantCount(testInitiativeId).getTotal(), is(1L));

        participantDao.create(participantCreateDto(), CONFIRMATION_CODE);
        participantDao.create(participantCreateDto(), ALREADY_CONFIRMED);

        assertThat(participantDao.getParticipantCount(testInitiativeId).getTotal(), is(2L));
    }


    @Test
    public void getAllParticipants_adds_municipality_name_and_franchise_to_participant_data() {

        Long otherMunicipality = testHelper.createTestMunicipality("Some other Municipality");
        createConfirmedParticipant(testInitiativeId, otherMunicipality, true, false, "Participant Name");

        List<Participant> participants = participantDao.findAllParticipants(testInitiativeId);

        Participant participant = participants.get(0);
        assertThat(participant.getHomeMunicipality().getNameFi(), is("Some other Municipality"));
        assertThat(participant.getHomeMunicipality().getNameSv(), is("Some other Municipality sv"));
        assertThat(participant.isFranchise(), is(true));
    }

    @Test
    public void getPublicParticipants_adds_municipality_name_and_franchise_to_participant_data() {

        Long otherMunicipality = testHelper.createTestMunicipality("Some other Municipality");
        createConfirmedParticipant(testInitiativeId, otherMunicipality, true, true, "Participant Name");

        List<Participant> participants = participantDao.findPublicParticipants(testInitiativeId);

        Participant participant = participants.get(0);
        assertThat(participant.getHomeMunicipality().getNameFi(), is("Some other Municipality"));
        assertThat(participant.getHomeMunicipality().getNameSv(), is("Some other Municipality sv"));
        assertThat(participant.isFranchise(), is(true));
    }

    @Test
    public void getAllParticipants_adds_participateTime_to_data() {
        List<Participant> participants = participantDao.findAllParticipants(testInitiativeId);
        Participant participant = participants.get(0);
        assertThat(participant.getParticipateDate(), is(notNullValue()));
    }

    @Test
    public void getPublicParticipants_adds_participateTime_to_data() {
        List<Participant> participants = participantDao.findPublicParticipants(testInitiativeId);
        Participant participant = participants.get(0);
        assertThat(participant.getParticipateDate(), is(notNullValue()));
    }

    private Long createConfirmedParticipant(Long initiativeId, Long homeMunicipality, boolean franchise, boolean publicName, String participantName) {
        ParticipantCreateDto participantCreateDto = new ParticipantCreateDto();
        participantCreateDto.setMunicipalityInitiativeId(initiativeId);
        participantCreateDto.setParticipantName(participantName);
        participantCreateDto.setHomeMunicipality(homeMunicipality);
        participantCreateDto.setFranchise(franchise);
        participantCreateDto.setShowName(publicName);
        return participantDao.create(participantCreateDto, ALREADY_CONFIRMED);
    }


    private Long createConfirmedParticipant(long initiativeId, boolean franchise, boolean publicName) {
        return createConfirmedParticipant(initiativeId, franchise, publicName, "Composers name");
    }

    private Long createConfirmedParticipant(long initiativeId, boolean franchise, boolean publicName, String participantName) {
        return createConfirmedParticipant(initiativeId, testMunicipalityId, franchise, publicName, participantName);
    }

    private ParticipantCreateDto participantCreateDto() {
        ParticipantCreateDto participantCreateDto = new ParticipantCreateDto();
        participantCreateDto.setMunicipalityInitiativeId(testInitiativeId);
        participantCreateDto.setParticipantName(PARTICIPANTS_NAME);
        participantCreateDto.setHomeMunicipality(otherMunicipalityId);
        participantCreateDto.setEmail(PARTICIPANT_EMAIL);
        participantCreateDto.setFranchise(PARTICIPANT_FRANCHISE);
        participantCreateDto.setShowName(PARTICIPANT_SHOW_NAME);
        return participantCreateDto;
    }


}
