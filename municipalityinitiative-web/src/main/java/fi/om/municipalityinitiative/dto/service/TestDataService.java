package fi.om.municipalityinitiative.dto.service;

import com.google.common.base.Optional;
import fi.om.municipalityinitiative.dao.AuthorDao;
import fi.om.municipalityinitiative.dao.InitiativeDao;
import fi.om.municipalityinitiative.dao.ParticipantDao;
import fi.om.municipalityinitiative.dao.UserDao;
import fi.om.municipalityinitiative.dto.YouthInitiativeCreateDto;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.dto.ui.InitiativeDraftUIEditDto;
import fi.om.municipalityinitiative.dto.ui.ParticipantUICreateDto;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.dto.user.User;
import fi.om.municipalityinitiative.dto.user.VerifiedUser;
import fi.om.municipalityinitiative.service.YouthInitiativeService;
import fi.om.municipalityinitiative.service.id.NormalAuthorId;
import fi.om.municipalityinitiative.service.id.VerifiedUserId;
import fi.om.municipalityinitiative.util.*;
import fi.om.municipalityinitiative.util.hash.RandomHashGenerator;
import fi.om.municipalityinitiative.web.Urls;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Random;

public class TestDataService {

    @Resource
    InitiativeDao initiativeDao;

    @Resource
    AuthorDao authorDao;

    @Resource
    UserDao userDao;

    @Resource
    private ParticipantDao participantDao;

    @Resource
    private YouthInitiativeService youthInitiativeService;

    private static final Random randomizer = new Random();

    private Optional<String> previousHash;

    public Optional<String> getPreviousHash() {
        return previousHash;
    }

    @Transactional(readOnly = false)
    public Long createTestMunicipalityInitiative(TestDataTemplates.InitiativeTemplate template, LoginUserHolder<User> loginUserHolder) {

        this.previousHash = Optional.absent();

        if (template.getInitiative().getType().isNotVerifiable()) {

            if (template.getInitiative().getYouthInitiativeId().isPresent()) {
                return createYouthInitiative(template);
            }

            return createDefaultInitiative(template);
        }
        else {
            return createVerifiableInitiative(template, loginUserHolder.getVerifiedUser());
        }
    }

    private Long createDefaultInitiative(TestDataTemplates.InitiativeTemplate template) {
        String managementHash = RandomHashGenerator.randomString(10);

        this.previousHash = Optional.of(managementHash);

        Long initiativeId = initiativeDao.prepareInitiative(template.initiative.getMunicipality().getId());
        Long participantId = participantDao.prepareConfirmedParticipant(initiativeId, template.initiative.getMunicipality().getId(), null, Membership.community, template.getAuthor().getContactInfo().isShowName());
        NormalAuthorId authorId = authorDao.createAuthor(initiativeId, participantId, managementHash);

        InitiativeDraftUIEditDto editDto = new InitiativeDraftUIEditDto();
        editDto.setName(template.initiative.getName());
        editDto.setContactInfo(template.author.getContactInfo());
        editDto.setProposal(template.initiative.getProposal()
                + "\n\n"
                + "Linkki hallintasivulle: " + Urls.get(Locales.LOCALE_FI).loginAuthor(managementHash)
        );
        editDto.setExtraInfo(template.initiative.getExtraInfo());
        initiativeDao.editInitiativeDraft(initiativeId, editDto);
        authorDao.updateAuthorInformation(authorId, editDto.getContactInfo());

        initiativeDao.updateInitiativeType(initiativeId, template.initiative.getType());
        if (template.initiative.getType() == InitiativeType.SINGLE) {
            initiativeDao.markInitiativeAsSent(initiativeId);
        }
        initiativeDao.updateInitiativeState(initiativeId, template.initiative.getState());

        return initiativeId;
    }

    private Long createVerifiableInitiative(TestDataTemplates.InitiativeTemplate template, VerifiedUser currentVerifiedUser) {
        Long initiativeId = initiativeDao.prepareVerifiedInitiative(template.getInitiative().getMunicipality().getId(), template.getInitiative().getType());
        Maybe<VerifiedUser> userMaybe = userDao.getVerifiedUser(currentVerifiedUser.getHash());
        if (userMaybe.isNotPresent()) {
            ContactInfo contactInfo = currentVerifiedUser.getContactInfo();
            contactInfo.setEmail(template.getAuthor().getContactInfo().getEmail());
            userDao.addVerifiedUser(currentVerifiedUser.getHash(), contactInfo, currentVerifiedUser.getHomeMunicipality());
        }

        VerifiedUserId verifiedUserId = userDao.getVerifiedUserId(currentVerifiedUser.getHash()).get();
        participantDao.addVerifiedParticipant(initiativeId, verifiedUserId, template.getAuthor().getContactInfo().isShowName(), true);
        authorDao.addVerifiedAuthor(initiativeId, verifiedUserId);

        InitiativeDraftUIEditDto editDto = new InitiativeDraftUIEditDto();
        editDto.setName(template.initiative.getName());
        editDto.setContactInfo(template.author.getContactInfo());
        editDto.setProposal(template.initiative.getProposal());
        editDto.setExtraInfo(template.initiative.getExtraInfo());
        initiativeDao.editInitiativeDraft(initiativeId, editDto);
        initiativeDao.updateInitiativeState(initiativeId, template.initiative.getState());

        return initiativeId;
    }

    private Long createYouthInitiative(TestDataTemplates.InitiativeTemplate template) {

        YouthInitiativeCreateDto createDto = new YouthInitiativeCreateDto();

        createDto.setMunicipality(template.getInitiative().getMunicipality().getId());
        createDto.setExtraInfo(template.getInitiative().getExtraInfo());
        createDto.setName(template.getInitiative().getName());
        createDto.setProposal(template.getInitiative().getProposal());
        createDto.setYouthInitiativeId(template.getInitiative().getYouthInitiativeId().get());

        YouthInitiativeCreateDto.ContactInfo contactInfo = new YouthInitiativeCreateDto.ContactInfo();
        contactInfo.setEmail(template.author.getContactInfo().getEmail());
        contactInfo.setMunicipality(template.initiative.getMunicipality().getId());
        contactInfo.setName(template.author.getContactInfo().getName());
        contactInfo.setPhone(template.author.getContactInfo().getPhone());
        createDto.setContactInfo(contactInfo);

        return youthInitiativeService.prepareYouthInitiative(createDto).getInitiativeId();

    }

    @Transactional(readOnly = false)
    public void createTestParticipant(Long initiativeId, ParticipantUICreateDto createDto) {
        Long participantId = participantDao.create(ParticipantCreateDto.parse(createDto, initiativeId), "confirmationCode");
        participantDao.confirmParticipation(participantId, "confirmationCode");
    }

    @Transactional(readOnly = false)
    public void createVerifiedTestParticipant(Long initiativeId, ParticipantUICreateDto participantUICreateDto) {

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail(participantUICreateDto.getParticipantEmail());
        contactInfo.setName(participantUICreateDto.getParticipantName());
        VerifiedUserId verifiedUserId = userDao.addVerifiedUser(RandomHashGenerator.randomString(30), contactInfo, Maybe.<Municipality>absent());
        participantDao.addVerifiedParticipant(initiativeId, verifiedUserId, participantUICreateDto.getShowName(), participantUICreateDto.getShowName() && (randomizer.nextInt() % 5 == 0));
    }

}