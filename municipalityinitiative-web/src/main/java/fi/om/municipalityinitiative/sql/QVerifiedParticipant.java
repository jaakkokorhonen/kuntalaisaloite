package fi.om.municipalityinitiative.sql;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.NumberPath;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QVerifiedParticipant is a Querydsl query type for QVerifiedParticipant
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QVerifiedParticipant extends com.mysema.query.sql.RelationalPathBase<QVerifiedParticipant> {

    private static final long serialVersionUID = -1693721093;

    public static final QVerifiedParticipant verifiedParticipant = new QVerifiedParticipant("verified_participant");

    public final NumberPath<Long> initiativeId = createNumber("initiative_id", Long.class);

    public final DatePath<org.joda.time.LocalDate> participateTime = createDate("participate_time", org.joda.time.LocalDate.class);

    public final BooleanPath showName = createBoolean("show_name");

    public final BooleanPath verified = createBoolean("verified");

    public final NumberPath<Long> verifiedUserId = createNumber("verified_user_id", Long.class);

    public final com.mysema.query.sql.PrimaryKey<QVerifiedParticipant> verifiedParticipantPk = createPrimaryKey(initiativeId, verifiedUserId);

    public final com.mysema.query.sql.ForeignKey<QVerifiedUser> verifiedParticipantVerifiedUserFk = createForeignKey(verifiedUserId, "id");

    public final com.mysema.query.sql.ForeignKey<QMunicipalityInitiative> verifiedParticipantInitiativeFk = createForeignKey(initiativeId, "id");

    public QVerifiedParticipant(String variable) {
        super(QVerifiedParticipant.class, forVariable(variable), "municipalityinitiative", "verified_participant");
    }

    public QVerifiedParticipant(Path<? extends QVerifiedParticipant> path) {
        super(path.getType(), path.getMetadata(), "municipalityinitiative", "verified_participant");
    }

    public QVerifiedParticipant(PathMetadata<?> metadata) {
        super(QVerifiedParticipant.class, metadata, "municipalityinitiative", "verified_participant");
    }

}

