package fi.om.municipalityinitiative.service.ui;

import fi.om.municipalityinitiative.dto.InitiativeCounts;
import fi.om.municipalityinitiative.dto.InitiativeSearch;
import fi.om.municipalityinitiative.dto.service.AuthorMessage;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.service.ManagementSettings;
import fi.om.municipalityinitiative.dto.ui.*;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.dto.user.User;
import fi.om.municipalityinitiative.service.EncryptionService;
import fi.om.municipalityinitiative.service.email.EmailService;
import fi.om.municipalityinitiative.service.operations.PublicInitiativeServiceOperations;
import fi.om.municipalityinitiative.util.FixState;
import fi.om.municipalityinitiative.util.InitiativeState;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.Locale;

import static fi.om.municipalityinitiative.service.operations.PublicInitiativeServiceOperations.ParticipantCreatedData;
import static fi.om.municipalityinitiative.service.operations.PublicInitiativeServiceOperations.PreparedInitiativeData;


public class PublicInitiativeService {

    @Resource
    private PublicInitiativeServiceOperations operations;

    @Resource
    private EmailService emailService;

    @Resource
    private EncryptionService encryptionService;

    public InitiativeListWithCount findMunicipalityInitiatives(InitiativeSearch search, LoginUserHolder loginUserHolder) {

        // XXX: This switching from all to omAll is pretty nasty, because value must be set back to original after usage
        // because UI is not prepared to omAll value, it's only for dao actually.
        if (loginUserHolder.getUser().isOmUser() && search.getShow() == InitiativeSearch.Show.all) {
            search.setShow(InitiativeSearch.Show.omAll);
        }

        if (search.getShow().isOmOnly()) {
            loginUserHolder.assertOmUser();
        }

        InitiativeListWithCount initiativeListInfos = operations.findInitiatives(search, loginUserHolder.getUser().isOmUser());
        if (search.getShow() == InitiativeSearch.Show.omAll)
            search.setShow(InitiativeSearch.Show.all);
        return initiativeListInfos;
    }

    @Transactional(readOnly = false)
    public ManagementSettings getManagementSettings(Long initiativeId) {
        return operations.getManagementSettings(initiativeId);
    }

    @Transactional(readOnly = false)
    public Long createParticipant(ParticipantUICreateDto participant, Long initiativeId, Locale locale) {

        ParticipantCreatedData participantCreatedData
                = operations.doCreateParticipant(participant, initiativeId);

        emailService.sendParticipationConfirmation(
                initiativeId,
                participant.getParticipantEmail(),
                participantCreatedData.participantId,
                participantCreatedData.confirmationCode,
                locale
        );

        return participantCreatedData.participantId;
    }

    @Transactional(readOnly = false)
    public Long prepareInitiative(PrepareInitiativeUICreateDto createDto, Locale locale) {

        PreparedInitiativeData preparedInitiativeData = operations.doPrepareInitiative(createDto);
        emailService.sendPrepareCreatedEmail(preparedInitiativeData.initiativeId, preparedInitiativeData.authorId, preparedInitiativeData.managementHash, locale);

        return preparedInitiativeData.initiativeId;
    }

    @Transactional(readOnly = false)
    public InitiativeViewInfo getPublicInitiative(Long initiativeId) {
        return getInitiative(initiativeId, new LoginUserHolder<>(User.anonym()));
    }

    @Transactional(readOnly = false)
    public InitiativeViewInfo getInitiative(Long initiativeId, LoginUserHolder loginUserHolder) {
        Initiative initiative = operations.getInitiative(initiativeId);
        if (!initiative.isPublic()) {
            loginUserHolder.assertViewRightsForInitiative(initiative.getId());
        }
        return InitiativeViewInfo.parse(initiative);
    }

    public InitiativeCounts getInitiativeCounts(InitiativeSearch search, LoginUserHolder loginUserHolder) {
        if (loginUserHolder.getUser().isNotOmUser()) {
            return operations.getInitiativeCounts(search, false);
        }
        else return operations.getInitiativeCounts(search,true);
    }

    public Long confirmParticipation(Long participantId, String confirmationCode) {
        return operations.doConfirmParticipation(participantId, confirmationCode);
    }

    @Transactional(readOnly = false)
    public void addAuthorMessage(AuthorUIMessage authorUIMessage, Locale locale) {
        AuthorMessage authorMessage = operations.doAddAuthorMessage(authorUIMessage);
        emailService.sendAuthorMessageConfirmationEmail(authorMessage.getInitiativeId(), authorMessage, locale);
    }

    @Transactional(readOnly = false)
    public Long confirmAndSendAuthorMessage(String confirmationCode) {
        AuthorMessage authorMessage = operations.doConfirmAuthorMessage(confirmationCode);

        emailService.sendAuthorMessages(authorMessage.getInitiativeId(), authorMessage);
        return authorMessage.getInitiativeId();

    }

    public boolean isVerifiableInitiative(Long initiativeId) {
        return operations.isVerifiableInitiative(initiativeId);
    }

}
