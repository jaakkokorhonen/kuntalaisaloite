package fi.om.municipalityinitiative.newdto.json;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.om.municipalityinitiative.json.JsonId;
import fi.om.municipalityinitiative.json.LocalDateJsonSerializer;
import fi.om.municipalityinitiative.newdto.ui.InitiativeListInfo;
import fi.om.municipalityinitiative.web.Urls;
import org.joda.time.LocalDate;

public class InitiativeListJson {

    private InitiativeListInfo initiative;

    public InitiativeListJson(InitiativeListInfo initiative) {
        this.initiative = initiative;
    }

    @JsonId(path= Urls.INITIATIVE)
    public Long getId() {
        return initiative.getId();
    }

    public String getName() {
        return initiative.getName();
    }

    public MunicipalityJson getMunicipality() {
        return new MunicipalityJson(initiative.getMunicipality());
    }

    @JsonSerialize(using=LocalDateJsonSerializer.class)
    public LocalDate getCreateTime() {
        return initiative.getCreateTime();
    }

    public boolean isCollectable() {
        return initiative.isCollectable();
    }

    @JsonSerialize(using=LocalDateJsonSerializer.class)
    public LocalDate getSentTime() {
        return initiative.getSentTime().isPresent() ? initiative.getSentTime().get() : null;
    }

    public long getParticipantCount() {
        return initiative.getParticipantCount();
    }

}
