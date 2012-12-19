package fi.om.municipalityinitiative.sql;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.*;
import com.mysema.query.types.path.*;

import javax.annotation.Generated;


/**
 * QMunicipalityInitiativeMunicipalityIdSeq is a Querydsl query type for QMunicipalityInitiativeMunicipalityIdSeq
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QMunicipalityInitiativeMunicipalityIdSeq extends com.mysema.query.sql.RelationalPathBase<QMunicipalityInitiativeMunicipalityIdSeq> {

    private static final long serialVersionUID = 214708752;

    public static final QMunicipalityInitiativeMunicipalityIdSeq municipalityInitiativeMunicipalityIdSeq = new QMunicipalityInitiativeMunicipalityIdSeq("municipality_initiative_municipality_id_seq");

    public final NumberPath<Long> cacheValue = createNumber("cache_value", Long.class);

    public final NumberPath<Long> incrementBy = createNumber("increment_by", Long.class);

    public final BooleanPath isCalled = createBoolean("is_called");

    public final BooleanPath isCycled = createBoolean("is_cycled");

    public final NumberPath<Long> lastValue = createNumber("last_value", Long.class);

    public final NumberPath<Long> logCnt = createNumber("log_cnt", Long.class);

    public final NumberPath<Long> maxValue = createNumber("max_value", Long.class);

    public final NumberPath<Long> minValue = createNumber("min_value", Long.class);

    public final StringPath sequenceName = createString("sequence_name");

    public final NumberPath<Long> startValue = createNumber("start_value", Long.class);

    public QMunicipalityInitiativeMunicipalityIdSeq(String variable) {
        super(QMunicipalityInitiativeMunicipalityIdSeq.class, forVariable(variable), "municipalityinitiative", "municipality_initiative_municipality_id_seq");
    }

    public QMunicipalityInitiativeMunicipalityIdSeq(Path<? extends QMunicipalityInitiativeMunicipalityIdSeq> path) {
        super(path.getType(), path.getMetadata(), "municipalityinitiative", "municipality_initiative_municipality_id_seq");
    }

    public QMunicipalityInitiativeMunicipalityIdSeq(PathMetadata<?> metadata) {
        super(QMunicipalityInitiativeMunicipalityIdSeq.class, metadata, "municipalityinitiative", "municipality_initiative_municipality_id_seq");
    }

}

