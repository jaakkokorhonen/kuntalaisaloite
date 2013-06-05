package fi.om.municipalityinitiative.dao;

import fi.om.municipalityinitiative.dto.Author;
import fi.om.municipalityinitiative.dto.service.AuthorInvitation;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;

import java.util.List;
import java.util.Set;

public interface AuthorDao {

    void deleteAuthorInvitation(Long initiativeId, String confirmationCode);

    Long addAuthorInvitation(AuthorInvitation authorInvitation);

    AuthorInvitation getAuthorInvitation(Long initiativeId, String confirmationCode);

    void rejectAuthorInvitation(Long initiativeId, String confirmationCode);

    List<AuthorInvitation> findInvitations(Long initiativeId);

    List<Author> findAuthors(Long initiativeId);

    Long createAuthor(Long initiativeId, Long participantId, String managementHash);

    void updateAuthorInformation(Long authorId, ContactInfo contactInfo);

    List<String> getAuthorEmails(Long initiativeId);

    Set<Long> getAuthorsInitiatives(String managementHash);

    Long getAuthorId(String managementHash);

    Author getAuthor(Long authorId);

    void deleteAuthor(Long authorId);

    void updateManagementHash(Long authorId, String newManagementHash);
}