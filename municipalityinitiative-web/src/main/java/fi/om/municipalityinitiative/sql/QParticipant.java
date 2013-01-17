package fi.om.municipalityinitiative.sql;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QParticipant is a Querydsl query type for QParticipant
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QParticipant extends com.mysema.query.sql.RelationalPathBase<QParticipant> {

    private static final long serialVersionUID = -1571974685;

    public static final QParticipant participant = new QParticipant("participant");

    public final BooleanPath franchise = createBoolean("franchise");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> municipalityId = createNumber("municipality_id", Long.class);

    public final NumberPath<Long> municipalityInitiativeId = createNumber("municipality_initiative_id", Long.class);

    public final StringPath name = createString("name");

    public final BooleanPath showName = createBoolean("show_name");

    public final com.mysema.query.sql.PrimaryKey<QParticipant> participantPk = createPrimaryKey(id);

    public final com.mysema.query.sql.ForeignKey<QMunicipalityInitiative> participantMunicipalityInitiativeId = createForeignKey(municipalityInitiativeId, "id");

    public final com.mysema.query.sql.ForeignKey<QMunicipalityInitiative> _municipalityInitiativeAuthorFk = createInvForeignKey(id, "author_id");

    public QParticipant(String variable) {
        super(QParticipant.class, forVariable(variable), "municipalityinitiative", "participant");
    }

    public QParticipant(Path<? extends QParticipant> path) {
        super(path.getType(), path.getMetadata(), "municipalityinitiative", "participant");
    }

    public QParticipant(PathMetadata<?> metadata) {
        super(QParticipant.class, metadata, "municipalityinitiative", "participant");
    }

}
