package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.dao.ParticipantDao;
import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.dao.UserDao;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.ui.ParticipantListInfo;
import fi.om.municipalityinitiative.dto.ui.ParticipantUICreateDto;
import fi.om.municipalityinitiative.dto.user.VerifiedUser;
import fi.om.municipalityinitiative.exceptions.OperationNotAllowedException;
import fi.om.municipalityinitiative.service.email.EmailSubjectPropertyKeys;
import fi.om.municipalityinitiative.util.InitiativeState;
import fi.om.municipalityinitiative.util.InitiativeType;
import fi.om.municipalityinitiative.util.Maybe;
import fi.om.municipalityinitiative.util.hash.PreviousHashGetter;
import fi.om.municipalityinitiative.web.Urls;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.List;
import java.util.Set;

import static fi.om.municipalityinitiative.util.TestUtil.precondition;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ParticipantServiceIntegrationTest extends ServiceIntegrationTestBase{

    @Resource
    private ParticipantService participantService;

    @Resource
    private ParticipantDao participantDao;

    @Resource
    private UserDao userDao;

    private Long testMunicipalityId;

    @Override
    protected void childSetup() {
        testMunicipalityId = testHelper.createTestMunicipality("Some municipality");
    }

    @Test
    public void findPublicParticipants_for_verified_initiative() {
        Long initiativeId = createVerifiedInitiativeWithAuthor();
        assertThat(participantService.findPublicParticipants(0, initiativeId), hasSize(1));
    }

    @Test
    public void findPublicParticipants_for_normal_initiative() {
        Long initiativeId = createNormalInitiativeWithAuthor();
        assertThat(participantService.findPublicParticipants(0, initiativeId), hasSize(1));
    }

    @Test
    public void findPublicParticipants_limits_results() {
        Long initiativeId = testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testMunicipalityId));

        int participantCount = 101;
        createParticipants(initiativeId, participantCount);

        // Get without offset, should be limited
        assertThat(participantService.findPublicParticipants(0, initiativeId), hasSize(Urls.MAX_PARTICIPANT_LIST_LIMIT));

        int offset = 60;

        // Use offset, result is the content of "last page"
        List<ParticipantListInfo> lastOnes = participantService.findPublicParticipants(offset, initiativeId);
        assertThat(lastOnes, hasSize(participantCount- offset));

        assertThat(lastOnes.get(0).getParticipant().getName(), is(String.valueOf(participantCount - offset)));
        assertThat(lastOnes.get(lastOnes.size()-1).getParticipant().getName(), is("1"));

    }

    private void createParticipants(Long initiativeId, int participantCount) {
        for (int i = 0; i < participantCount; ++i) {
            testHelper.createDefaultParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withParticipantName(String.valueOf(i+1)));
        }
    }

    @Test
    public void findAllParticipants_for_verified_initiative() {
        Long initiativeId = createVerifiedInitiativeWithAuthor();
        assertParticipantListSize(initiativeId, 1);
    }

    @Test
    public void findAllParticipants_for_normal_initiative() {
        Long initiativeId = createNormalInitiativeWithAuthor();
        assertParticipantListSize(initiativeId, 1);
    }

    @Test
    public void findPublicParticipants_for_normal_initiative_sets_author_flag_true_if_author() {
        Long initiativeId = createNormalInitiativeWithAuthor();
        testHelper.createDefaultParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withPublicName(true));

        List<ParticipantListInfo> publicParticipants = participantService.findPublicParticipants(0, initiativeId);
        precondition(publicParticipants, hasSize(2));

        assertThat(publicParticipants.get(0).isAuthor(), is(false));
        assertThat(publicParticipants.get(1).isAuthor(), is(true));
    }

    @Test
    public void findPublicParticipants_for_verified_initiative_sets_author_flag_true_if_author() {
        Long initiativeId = createVerifiedInitiativeWithAuthor();
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withPublicName(true));

        List<ParticipantListInfo> publicParticipants = participantService.findPublicParticipants(0, initiativeId);
        precondition(publicParticipants, hasSize(2));

        assertThat(publicParticipants.get(0).isAuthor(), is(false));
        assertThat(publicParticipants.get(1).isAuthor(), is(true));
    }

    @Test
    public void delete_participant_decreses_participant_count() {
        Long initiativeId = testHelper.createDefaultInitiative(
                new TestHelper.InitiativeDraft(testMunicipalityId)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE)
                        .applyAuthor().withPublicName(false)
                        .toInitiativeDraft()
        );
        Long participantId = testHelper.createDefaultParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withPublicName(true));

        Initiative initiative = testHelper.getInitiative(initiativeId);
        precondition(initiative.getParticipantCountPublic(), is(1));
        precondition(initiative.getParticipantCount(), is(2));

        participantService.deleteParticipant(initiativeId, TestHelper.authorLoginUserHolder, participantId);

        Initiative updated = testHelper.getInitiative(initiativeId);
        assertThat(updated.getParticipantCount(), is(1));
        assertThat(updated.getParticipantCountPublic(), is(0));

        assertParticipantListSize(initiativeId, 1);
    }

    private void assertParticipantListSize(Long initiativeId, int size) {
        assertThat(participantService.findAllParticipants(initiativeId, TestHelper.authorLoginUserHolder, 0), hasSize(size));
    }

    @Test
    public void delete_verified_participant_decreases_participant_count_citizen() {
        Long initiativeId = testHelper.createDefaultInitiative(
                new TestHelper.InitiativeDraft(testMunicipalityId)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE_CITIZEN)
                        .applyAuthor().withPublicName(false)
                        .toInitiativeDraft()
        );
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withPublicName(true));
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withPublicName(true));

        Initiative initiative = testHelper.getInitiative(initiativeId);
        precondition(initiative.getParticipantCountPublic(), is(2));
        precondition(initiative.getParticipantCount(), is(3));

        assertParticipantListSize(initiativeId, 2);
        participantService.deleteParticipant(initiativeId, TestHelper.authorLoginUserHolder, testHelper.getLastVerifiedUserId());
        assertParticipantListSize(initiativeId, 1);

        Initiative updated = testHelper.getInitiative(initiativeId);
        assertThat(updated.getParticipantCount(), is(2));
        assertThat(updated.getParticipantCountPublic(), is(1));
    }
    @Test
    public void delete_verified_participant_decreases_participant_count_citizen_private_name() {
        Long initiativeId = testHelper.createDefaultInitiative(
                new TestHelper.InitiativeDraft(testMunicipalityId)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE_CITIZEN)
                        .applyAuthor().withPublicName(false)
                        .toInitiativeDraft()
        );
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withPublicName(false));
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withPublicName(false));

        Initiative initiative = testHelper.getInitiative(initiativeId);
        precondition(initiative.getParticipantCountPublic(), is(0));
        precondition(initiative.getParticipantCount(), is(3));

        assertParticipantListSize(initiativeId, 2);
        participantService.deleteParticipant(initiativeId, TestHelper.authorLoginUserHolder, testHelper.getLastVerifiedUserId());
        assertParticipantListSize(initiativeId, 1);

        Initiative updated = testHelper.getInitiative(initiativeId);
        assertThat(updated.getParticipantCount(), is(2));
        assertThat(updated.getParticipantCountPublic(), is(0));
    }
    @Test
    public void delete_verified_participant_decreases_participant_count_council() {
        Long initiativeId = testHelper.createVerifiedInitiative(
                new TestHelper.InitiativeDraft(testMunicipalityId)
                        .withState(InitiativeState.PUBLISHED)
                        .withType(InitiativeType.COLLABORATIVE_COUNCIL)
                        .applyAuthor().withPublicName(false)
                        .toInitiativeDraft()
        );
        testHelper.createVerifiedParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withPublicName(true));

        Initiative initiative = testHelper.getInitiative(initiativeId);
        precondition(initiative.getParticipantCountPublic(), is(1));
        precondition(initiative.getParticipantCount(), is(2));

        assertParticipantListSize(initiativeId, 2);
        participantService.deleteParticipant(initiativeId, TestHelper.authorLoginUserHolder, testHelper.getLastVerifiedUserId());
        assertParticipantListSize(initiativeId, 1);

        Initiative updated = testHelper.getInitiative(initiativeId);
        assertThat(updated.getParticipantCount(), is(1));
        assertThat(updated.getParticipantCountPublic(), is(0));
    }

    @Test(expected = OperationNotAllowedException.class)
    public void delete_participant_is_forbidden_if_initiative_sent_to_municipality() {
        Long initiativeId = testHelper.createDefaultInitiative(
                new TestHelper.InitiativeDraft(testMunicipalityId)
                        .withSent(new DateTime())
                        .applyAuthor().withPublicName(false)
                        .toInitiativeDraft()
        );
        Long participantId = testHelper.createDefaultParticipant(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId).withPublicName(true));

        Initiative initiative = testHelper.getInitiative(initiativeId);
        precondition(initiative.getParticipantCountPublic(), is(1));
        precondition(initiative.getParticipantCount(), is(2));

        participantService.deleteParticipant(initiativeId, TestHelper.authorLoginUserHolder, participantId);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void participating_allowance_is_checked() {
        Long initiative = testHelper.createCollaborativeReview(testMunicipalityId);

        ParticipantUICreateDto participant = participantUICreateDto();
        participantService.createParticipant(participant, initiative, null);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void participating_allowance_is_checked_when_directly_participating() {
        Long initiative = testHelper.createCollaborativeReview(testMunicipalityId);

        ParticipantUICreateDto participant = participantUICreateDto();
        participantService.createConfirmedParticipant(participant, initiative, TestHelper.authorLoginUserHolder);
    }


    @Test
    public void adding_participant_does_not_increase_denormalized_participantCount_but_accepting_does() throws MessagingException, InterruptedException {
        Long initiativeId = testHelper.create(testMunicipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);
        int originalParticipantCount = testHelper.getInitiative(initiativeId).getParticipantCount();

        Long participantId = participantService.createParticipant(participantUICreateDto(), initiativeId, null);
        assertThat(testHelper.getInitiative(initiativeId).getParticipantCount(), Matchers.is(originalParticipantCount));

        participantService.confirmParticipation(participantId, PreviousHashGetter.get());
        assertThat(getSingleInitiativeInfo().getParticipantCount(), Matchers.is(originalParticipantCount + 1));

        assertUniqueSentEmail(participantUICreateDto().getParticipantEmail(), EmailSubjectPropertyKeys.EMAIL_PARTICIPATION_CONFIRMATION_SUBJECT);
    }
    @Test
    public void adding_confirmed_participant_increases_denormalized_participantCount() throws MessagingException, InterruptedException {
        Long initiativeId = testHelper.create(testMunicipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);
        int originalParticipantCount = testHelper.getInitiative(initiativeId).getParticipantCount();
        testHelper.createVerifiedUser(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId));

        Long participantId = participantService.createConfirmedParticipant(participantUICreateDto(), initiativeId, TestHelper.lastLoggedInVerifiedUserHolder);

        assertThat(getSingleInitiativeInfo().getParticipantCount(), Matchers.is(originalParticipantCount + 1));
    }

    @Transactional
    @Test
    public void adding_confirmed_participant_to_normal_initiative_shows_in_user_session() throws MessagingException, InterruptedException {
        Long initiativeId = testHelper.create(testMunicipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);

        testHelper.createVerifiedUser(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId));

        Long participantId = participantService.createConfirmedParticipant(participantUICreateDto(), initiativeId, TestHelper.lastLoggedInVerifiedUserHolder);

        Maybe<VerifiedUser> verifiedUser = refreshVerifiedUser();

        Set<Long> initiativesWithParticipation = verifiedUser.getValue().getInitiativesWithParticipation();

        assertThat(initiativesWithParticipation, contains(initiativeId));
    }

    @Transactional
    @Test
    public void removing_confirmed_user_from_normal_initiative_also_removes_the_initiative_from_verified_user() throws MessagingException, InterruptedException {
        Long initiativeId = testHelper.create(testMunicipalityId, InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);

        testHelper.createVerifiedUser(new TestHelper.AuthorDraft(initiativeId, testMunicipalityId));

        Long participantId = participantService.createConfirmedParticipant(participantUICreateDto(), initiativeId, TestHelper.lastLoggedInVerifiedUserHolder);

        participantDao.deleteParticipant(initiativeId, participantId);

        Maybe<VerifiedUser> verifiedUser = refreshVerifiedUser();

        Set<Long> initiativesWithParticipation = verifiedUser.getValue().getInitiativesWithParticipation();

        assertThat(initiativesWithParticipation, empty());
    }

    private Maybe<VerifiedUser> refreshVerifiedUser() {
        return userDao.getVerifiedUser(TestHelper.lastLoggedInVerifiedUserHolder.getVerifiedUser().getHash());
    }

    @Test(expected = OperationNotAllowedException.class)
    public void accepting_participation_allowance_is_checked() {
        testHelper.createSingleSent(testMunicipalityId);
        participantService.confirmParticipation(testHelper.getLastParticipantId(), null);
    }

    private ParticipantUICreateDto participantUICreateDto() {
        ParticipantUICreateDto participant = new ParticipantUICreateDto();
        participant.setParticipantName("Some Name");
        participant.setParticipantEmail("participant@example.com");
        participant.setShowName(true);
        participant.setHomeMunicipality(testMunicipalityId);
        participant.assignInitiativeMunicipality(testMunicipalityId);
        return participant;
    }

    private Long createNormalInitiativeWithAuthor() {
        return testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testMunicipalityId).applyAuthor().toInitiativeDraft());
    }

    private Long createVerifiedInitiativeWithAuthor() {
        return testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipalityId).applyAuthor().toInitiativeDraft());
    }

    private Initiative getSingleInitiativeInfo() {
        return testHelper.getInitiative(testHelper.getLastInitiativeId());
    }



}
