package fi.om.municipalityinitiative.dao;

import fi.om.municipalityinitiative.conf.IntegrationTestConfiguration;
import fi.om.municipalityinitiative.dto.service.AttachmentFileInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.om.municipalityinitiative.util.TestUtil.precondition;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
@Transactional
public class JdbcAttachmentDaoTest {

    @Resource
    TestHelper testHelper;

    @Resource
    AttachmentDao attachmentDao;

    private Long initiativeId;
    private Long municipalityId;

    @Before
    public void setup() {
        testHelper.dbCleanup();
        municipalityId = testHelper.createTestMunicipality("mun");
        initiativeId = createInitiative();
    }

    @Test
    public void find_attachments_finds_only_accepted_attachments() {
        testHelper.addAttachment(initiativeId, "kakka kuva", false);
        testHelper.addAttachment(initiativeId, "hyvä kuva", true);

        List<AttachmentFileInfo> attachments = attachmentDao.findAttachments(initiativeId);
        assertThat(attachments, hasSize(1));
        assertThat(attachments.get(0).getDescription(), is("hyvä kuva"));
    }

    @Test
    public void find_all_attachments() {
        testHelper.addAttachment(initiativeId, "kakka kuva", false);
        testHelper.addAttachment(initiativeId, "hyvä kuva", true);

        List<AttachmentFileInfo> attachments = attachmentDao.findAllAttachments(initiativeId);
        assertThat(attachments, hasSize(2));
    }

    @Test
    public void add_and_get_attachment() {
        Long attachmentID = attachmentDao.addAttachment(initiativeId, "description", "contentType");
        AttachmentFileInfo attachment = attachmentDao.getAttachment(attachmentID);
        assertThat(attachment.getDescription(), is("description"));
        assertThat(attachment.getContentType(), is("contentType"));
        assertThat(attachment.getCreateTime(), is(notNullValue()));
        assertThat(attachment.isAccepted(), is(false));
        assertThat(attachment.getInitiativeId(), is(initiativeId));
        assertThat(attachment.getAttachmentId(), is(attachmentID));
    }


    @Test
    public void accept_initiatives_attachments() {
        testHelper.addAttachment(initiativeId, "description", false);

        Long anotherInitiativeId = createInitiative();
        testHelper.addAttachment(anotherInitiativeId, "asd", false);

        precondition(attachmentDao.findAttachments(initiativeId), hasSize(0));
        precondition(attachmentDao.findAttachments(anotherInitiativeId), hasSize(0));

        attachmentDao.acceptAttachments(initiativeId);

        assertThat(attachmentDao.findAttachments(initiativeId), hasSize(1));
        assertThat(attachmentDao.findAttachments(anotherInitiativeId), hasSize(0));
    }

    @Test
    public void reject_initiative_attachments() {
        testHelper.addAttachment(initiativeId, "description", true);

        Long anotherInitiativeId = createInitiative();
        testHelper.addAttachment(anotherInitiativeId, "asd", true);

        precondition(attachmentDao.findAttachments(initiativeId), hasSize(1));
        precondition(attachmentDao.findAttachments(anotherInitiativeId), hasSize(1));

        attachmentDao.rejectAttachments(initiativeId);

        assertThat(attachmentDao.findAttachments(initiativeId), hasSize(0));
        assertThat(attachmentDao.findAttachments(anotherInitiativeId), hasSize(1));
    }

    private Long createInitiative() {
        return testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(municipalityId).applyAuthor().toInitiativeDraft());
    }

}
