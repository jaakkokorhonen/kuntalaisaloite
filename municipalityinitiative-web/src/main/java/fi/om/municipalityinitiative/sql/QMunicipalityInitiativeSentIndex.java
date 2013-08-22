package fi.om.municipalityinitiative.sql;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.DateTimePath;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QMunicipalityInitiativeSentIndex is a Querydsl query type for QMunicipalityInitiativeSentIndex
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QMunicipalityInitiativeSentIndex extends com.mysema.query.sql.RelationalPathBase<QMunicipalityInitiativeSentIndex> {

    private static final long serialVersionUID = 1497656322;

    public static final QMunicipalityInitiativeSentIndex municipalityInitiativeSentIndex = new QMunicipalityInitiativeSentIndex("municipality_initiative_sent_index");

    public final DateTimePath<org.joda.time.DateTime> sent = createDateTime("sent", org.joda.time.DateTime.class);

    public QMunicipalityInitiativeSentIndex(String variable) {
        super(QMunicipalityInitiativeSentIndex.class, forVariable(variable), "municipalityinitiative", "municipality_initiative_sent_index");
    }

    public QMunicipalityInitiativeSentIndex(Path<? extends QMunicipalityInitiativeSentIndex> path) {
        super(path.getType(), path.getMetadata(), "municipalityinitiative", "municipality_initiative_sent_index");
    }

    public QMunicipalityInitiativeSentIndex(PathMetadata<?> metadata) {
        super(QMunicipalityInitiativeSentIndex.class, metadata, "municipalityinitiative", "municipality_initiative_sent_index");
    }

}
