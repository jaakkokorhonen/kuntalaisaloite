package fi.om.municipalityinitiative.newweb;

import fi.om.municipalityinitiative.newdto.InitiativeSearch;
import fi.om.municipalityinitiative.newdto.ui.*;
import fi.om.municipalityinitiative.service.InitiativeService;
import fi.om.municipalityinitiative.service.MunicipalityService;
import fi.om.municipalityinitiative.service.ParticipantService;
import fi.om.municipalityinitiative.service.ValidationService;
import fi.om.municipalityinitiative.util.InitiativeState;
import fi.om.municipalityinitiative.util.InitiativeType;
import fi.om.municipalityinitiative.util.Maybe;
import fi.om.municipalityinitiative.web.BaseController;
import fi.om.municipalityinitiative.web.RequestMessage;
import fi.om.municipalityinitiative.web.SearchParameterGenerator;
import fi.om.municipalityinitiative.web.Urls;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Locale;

import static fi.om.municipalityinitiative.web.Urls.*;
import static fi.om.municipalityinitiative.web.Views.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class InitiativeViewController extends BaseController {

    @Resource
    private MunicipalityService municipalityService;
    
    @Resource
    private InitiativeService initiativeService;

    @Resource
    private ValidationService validationService;

    @Resource
    private ParticipantService participantService;

    public InitiativeViewController(boolean optimizeResources, String resourcesVersion) {
        super(optimizeResources, resourcesVersion);
    }

    @RequestMapping(value={SEARCH_FI, SEARCH_SV}, method=GET)
    public String search(InitiativeSearch search, Model model, Locale locale, HttpServletRequest request) {
        Urls urls = Urls.get(locale);
        model.addAttribute(ALT_URI_ATTR, urls.alt().search());
        
        List<MunicipalityInfo> municipalities = municipalityService.findAllMunicipalities(locale);

        model.addAttribute("initiatives", initiativeService.findMunicipalityInitiatives(search));
        model.addAttribute("municipalities", municipalities);
        model.addAttribute("locale", locale);
        model.addAttribute("currentSearch", search);
        model.addAttribute("searchParameters", new SearchParameterGenerator(search));
        model.addAttribute("currentMunicipality", solveMunicipalityFromListById(municipalities, search.getMunicipality()));
        model.addAttribute("initiativeCounts", initiativeService.getInitiativeCounts(Maybe.fromNullable(search.getMunicipality())));
        return SEARCH_VIEW;
    }

    @RequestMapping(value={ VIEW_FI, VIEW_SV }, method=GET)
    public String view(@PathVariable("id") Long initiativeId,
            @RequestParam(PARAM_MANAGEMENT_CODE) String managementHash,
            Model model, Locale locale, HttpServletRequest request) {
        Urls urls = Urls.get(locale);
        model.addAttribute(ALT_URI_ATTR, urls.alt().view(initiativeId));
        
        InitiativeViewInfo initiativeInfo = initiativeService.getMunicipalityInitiative(initiativeId, locale);

        // TODO: Use initiativeState PUBLISHED when user can publish initiative
        if (initiativeInfo.getState() == InitiativeState.ACCEPTED) {
            if (initiativeInfo.isCollectable()){// TODO: If not sent to municipality
    
                addModelAttributesToCollectView(model,
                        initiativeInfo,
                        municipalityService.findAllMunicipalities(locale),
                        participantService.getParticipantCount(initiativeId),
                        participantService.findPublicParticipants(initiativeId));
                model.addAttribute("participant", new ParticipantUICreateDto());
    
                return PUBLIC_COLLECT_VIEW;
            }
            else {
                model.addAttribute("initiative", initiativeInfo);
                return PUBLIC_SINGLE_VIEW;
            }
        // Returns preview for authors
        } else if (managementHash.equals(initiativeInfo.getManagementHash().get())){
            model.addAttribute("initiative", initiativeInfo);
            return PUBLIC_SINGLE_VIEW;
        } else {
            return ERROR_404_VIEW;
        }
    }

    @RequestMapping(value={ VIEW_FI, VIEW_SV }, method=POST)
    public String participate(@PathVariable("id") Long initiativeId,
                              @ModelAttribute("participant") ParticipantUICreateDto participant,
                              BindingResult bindingResult, Model model, Locale locale, HttpServletRequest request) {

        if (validationService.validationSuccessful(participant, bindingResult, model)) {
            initiativeService.createParticipant(participant, initiativeId);
            Urls urls = Urls.get(locale);
            return redirectWithMessage(urls.view(initiativeId), RequestMessage.PARTICIPATE, request);
        }
        else {
            addModelAttributesToCollectView(model,
                    initiativeService.getMunicipalityInitiative(initiativeId, locale),
                    municipalityService.findAllMunicipalities(locale),
                    participantService.getParticipantCount(initiativeId),
                    participantService.findPublicParticipants(initiativeId));
            model.addAttribute("participants", participantService.findPublicParticipants(initiativeId));
            return PUBLIC_COLLECT_VIEW;
        }
    }
    
    @RequestMapping(value={ PARITICIPANT_LIST_FI, PARITICIPANT_LIST_SV }, method=GET)
    public String participantList(@PathVariable("id") Long initiativeId, Model model, Locale locale, HttpServletRequest request) {
        Urls urls = Urls.get(locale);
        model.addAttribute(ALT_URI_ATTR, urls.alt().view(initiativeId));
        
        InitiativeViewInfo initiativeInfo = initiativeService.getMunicipalityInitiative(initiativeId, locale);

        if (initiativeInfo.isCollectable()){
            model.addAttribute("initiative",  initiativeInfo);
            model.addAttribute("participantCount", participantService.getParticipantCount(initiativeId));
            model.addAttribute("participants", participantService.findPublicParticipants(initiativeId));
            
            String managementURI = urls.management(initiativeId, initiativeInfo.getManagementHash().get());
            
            if (request.getHeader("referer") != null && request.getHeader("referer").equals(managementURI)) {
                model.addAttribute("previousPageURI", managementURI);
            } else {
                model.addAttribute("previousPageURI", urls.view(initiativeId));
            }

            model.addAttribute("locale", locale);

            return PARTICIPANT_LIST;
        }
        else {
            model.addAttribute("initiative", initiativeInfo);
            return PUBLIC_SINGLE_VIEW;
        }
    }

    @RequestMapping(value={ PENDING_CONFIRMATION_FI, PENDING_CONFIRMATION_SV }, method=GET)
    public String pendingConfirmation(@PathVariable("id") Long initiativeId, Model model, Locale locale, HttpServletRequest request) {
        
        Urls urls = Urls.get(locale);
        
        InitiativeViewInfo initiativeInfo = initiativeService.getMunicipalityInitiative(initiativeId, locale);

        model.addAttribute("initiative", initiativeInfo);

        if (initiativeInfo.getState().equals(InitiativeState.DRAFT) && getRequestAttribute(request) != null) {
            return PENDING_CONFIRMATION;
        } else {
            return redirectWithMessage(urls.prepare(), RequestMessage.PREPARE_CONFIRM_EXPIRED, request);
        }
    }

    @RequestMapping(value={IFRAME_FI, IFRAME_SV}, method=GET)
    public String iframe(InitiativeSearch search, Model model, Locale locale, HttpServletRequest request) {
        Urls urls = Urls.get(locale);
        model.addAttribute(ALT_URI_ATTR, urls.alt().search());
        
        List<MunicipalityInfo> municipalities = municipalityService.findAllMunicipalities(locale);

        model.addAttribute("initiatives", initiativeService.findMunicipalityInitiatives(search));
        model.addAttribute("municipalities", municipalities);
        model.addAttribute("currentSearch", search);
        model.addAttribute("searchParameters", new SearchParameterGenerator(search));
        model.addAttribute("currentMunicipality", solveMunicipalityFromListById(municipalities, search.getMunicipality()));
        model.addAttribute("initiativeCounts", initiativeService.getInitiativeCounts(Maybe.fromNullable(search.getMunicipality())));
        return IFRAME_VIEW;
    }

    private static String solveMunicipalityFromListById(List<MunicipalityInfo> municipalities, Long municipalityId){
        if (municipalityId == null)
            return null;
        for (MunicipalityInfo municipality : municipalities) {
            if (municipality.getId().equals(municipalityId))
                return municipality.getName();
        }
        return null;
    }
}