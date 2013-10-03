package fi.om.municipalityinitiative.dao;

import com.mysema.query.Tuple;
import com.mysema.query.sql.postgres.PostgresQueryFactory;
import com.mysema.query.types.Expression;
import com.mysema.query.types.MappingProjection;
import fi.om.municipalityinitiative.dto.service.AttachmentFileInfo;
import fi.om.municipalityinitiative.sql.QAttachment;

import javax.annotation.Resource;
import java.util.List;

@SQLExceptionTranslated
public class JdbcAttachmentDao implements AttachmentDao{

    @Resource
    private PostgresQueryFactory queryFactory;

    @Override
    public Long addAttachment(Long initiativeId, String originalFilename, String contentType) {
        return queryFactory.insert(QAttachment.attachment)
                .set(QAttachment.attachment.initiativeId, initiativeId)
                .set(QAttachment.attachment.filename, originalFilename)
                .set(QAttachment.attachment.contentType, contentType)
                .executeWithKey(QAttachment.attachment.id);

    }

    @Override
    public List<Long> findAttachments(Long initiativeId) {
        return queryFactory.from(QAttachment.attachment)
                .where(QAttachment.attachment.initiativeId.eq(initiativeId))
                .list(QAttachment.attachment.id);

    }

    @Override
    public List<AttachmentFileInfo> find(Long initiativeId) {
        return queryFactory.from(QAttachment.attachment)
                .where(QAttachment.attachment.initiativeId.eq(initiativeId))
                .list(attachmentMapper);
    }

    @Override
    public AttachmentFileInfo getAttachment(Long attachmentId) {
        return queryFactory.from(QAttachment.attachment)
                .where(QAttachment.attachment.id.eq(attachmentId))
                .uniqueResult(attachmentMapper);
    }

    static Expression<AttachmentFileInfo> attachmentMapper
            =  new MappingProjection<AttachmentFileInfo>(AttachmentFileInfo.class,QAttachment.attachment.all()) {
        @Override
        protected AttachmentFileInfo map(Tuple row) {
            AttachmentFileInfo attachmentFileInfo = new AttachmentFileInfo();

            attachmentFileInfo.setAttachmentId(row.get(QAttachment.attachment.id));
            attachmentFileInfo.setContentType(row.get(QAttachment.attachment.contentType));
            attachmentFileInfo.setFileName(row.get(QAttachment.attachment.filename));
            attachmentFileInfo.setCreateTime(row.get(QAttachment.attachment.added));
            attachmentFileInfo.setInitiativeId(row.get(QAttachment.attachment.initiativeId));
            return attachmentFileInfo;
        }
    };
}
