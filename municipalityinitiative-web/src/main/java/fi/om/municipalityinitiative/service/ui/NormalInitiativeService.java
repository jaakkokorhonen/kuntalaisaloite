package fi.om.municipalityinitiative.service.ui;

import fi.om.municipalityinitiative.dao.*;
import fi.om.municipalityinitiative.dto.InitiativeCounts;
import fi.om.municipalityinitiative.dto.InitiativeSearch;
import fi.om.municipalityinitiative.dto.service.AuthorMessage;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.service.ManagementSettings;
import fi.om.municipalityinitiative.dto.ui.AuthorUIMessage;
import fi.om.municipalityinitiative.dto.ui.InitiativeListWithCount;
import fi.om.municipalityinitiative.dto.ui.InitiativeViewInfo;
import fi.om.municipalityinitiative.dto.ui.PrepareInitiativeUICreateDto;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.dto.user.User;
import fi.om.municipalityinitiative.exceptions.AccessDeniedException;
import fi.om.municipalityinitiative.service.email.EmailService;
import fi.om.municipalityinitiative.service.id.NormalAuthorId;
import fi.om.municipalityinitiative.util.Maybe;
import fi.om.municipalityinitiative.util.Membership;
import fi.om.municipalityinitiative.util.hash.RandomHashGenerator;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;


public class NormalInitiativeService {
    
    @Resource
    private ParticipantDao participantDao;
    
    @Resource
    private AuthorDao authorDao;
    
    @Resource
    private InitiativeDao initiativeDao;

    @Resource
    private EmailService emailService;
    
    @Resource
    private AuthorMessageDao authorMessageDao;

    @Resource
    private MunicipalityDao municipalityDao;

    @Transactional(readOnly = true)
    public InitiativeListWithCount findMunicipalityInitiatives(InitiativeSearch search, LoginUserHolder loginUserHolder) {

        // XXX: This switching from all to omAll is pretty nasty, because value must be set back to original after usage
        // because UI is not prepared to omAll value, it's only for dao actually.
        if (loginUserHolder.getUser().isOmUser() && search.getShow() == InitiativeSearch.Show.all) {
            search.setShow(InitiativeSearch.Show.omAll);
        }

        if (search.getShow().isOmOnly()) {
            loginUserHolder.assertOmUser();
        }

        InitiativeListWithCount initiativeListInfos = findInitiatives(search, loginUserHolder.getUser().isOmUser());
        if (search.getShow() == InitiativeSearch.Show.omAll)
            search.setShow(InitiativeSearch.Show.all);
        return initiativeListInfos;
    }

    private InitiativeListWithCount findInitiatives(InitiativeSearch search, boolean skipCache) {
        if (skipCache) {
            return initiativeDao.findUnCached(search);
        }
        return initiativeDao.findCached(search);
    }

    @Transactional(readOnly = false)
    public ManagementSettings getManagementSettings(Long initiativeId) {
        return ManagementSettings.of(initiativeDao.get(initiativeId));
    }



    @Transactional(readOnly = false)
    public Long prepareInitiative(PrepareInitiativeUICreateDto createDto, Locale locale) {

        Long municipality = createDto.getMunicipality();
        if (!municipalityDao.getMunicipality(municipality).isActive()) {
            throw new AccessDeniedException("Municipality is not active for initiatives: " + municipality);
        }


        Long initiativeId = initiativeDao.prepareInitiative(createDto.getMunicipality());
        Long participantId = participantDao.prepareConfirmedParticipant(
                initiativeId,
                createDto.getHomeMunicipality(),
                createDto.getParticipantEmail(),
                createDto.hasMunicipalMembership() ? createDto.getMunicipalMembership() : Membership.none,
                true);
        String managementHash = RandomHashGenerator.longHash();
        NormalAuthorId authorId = authorDao.createAuthor(initiativeId, participantId, managementHash);

        emailService.sendPrepareCreatedEmail(initiativeId, authorId, managementHash, locale);

        return initiativeId;
    }

    @Transactional(readOnly = false)
    public InitiativeViewInfo getPublicInitiative(Long initiativeId) {
        return getInitiative(initiativeId, new LoginUserHolder<>(User.anonym()));
    }

    @Transactional(readOnly = true)
    public InitiativeViewInfo getInitiative(Long initiativeId, LoginUserHolder loginUserHolder) {
        Initiative initiative = initiativeDao.get(initiativeId);
        if (!initiative.isPublic()) {
            loginUserHolder.assertViewRightsForInitiative(initiative.getId());
        }
        return InitiativeViewInfo.parse(initiative);
    }

    @Transactional(readOnly = true)
    public InitiativeCounts getInitiativeCounts(InitiativeSearch search, LoginUserHolder loginUserHolder) {
        if (loginUserHolder.getUser().isNotOmUser()) {
            return initiativeDao.getPublicInitiativeCounts(Maybe.fromNullable(search.getMunicipalities()), search.getType());
        }
        else {
            return initiativeDao.getAllInitiativeCounts(Maybe.fromNullable(search.getMunicipalities()), search.getType());
        }
    }

    @Transactional(readOnly = false)
    public void addAuthorMessage(AuthorUIMessage authorUIMessage, Locale locale) {
        String confirmationCode = RandomHashGenerator.shortHash();
        AuthorMessage authorMessage = new AuthorMessage(authorUIMessage, confirmationCode);
        authorMessageDao.put(authorMessage);
        emailService.sendAuthorMessageConfirmationEmail(authorMessage.getInitiativeId(), authorMessage, locale);
    }

    @Transactional(readOnly = false)
    public Long confirmAndSendAuthorMessage(String confirmationCode) {
        AuthorMessage authorMessage = authorMessageDao.pop(confirmationCode);

        emailService.sendAuthorMessages(authorMessage.getInitiativeId(), authorMessage);
        return authorMessage.getInitiativeId();

    }

}
