package fi.om.municipalityinitiative.newdao;

import fi.om.municipalityinitiative.newdto.MunicipalityInfo;

import java.util.List;

public interface MunicipalityDao {

    List<MunicipalityInfo> findMunicipalities();
}