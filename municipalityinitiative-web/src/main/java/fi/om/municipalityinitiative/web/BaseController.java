package fi.om.municipalityinitiative.web;

import static fi.om.municipalityinitiative.web.Views.contextRelativeRedirect;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fi.om.municipalityinitiative.dto.EditMode;
import fi.om.municipalityinitiative.dto.FlowState;
import fi.om.municipalityinitiative.dto.FlowStateAnalyzer;
import fi.om.municipalityinitiative.dto.InitiativeConstants;
import fi.om.municipalityinitiative.dto.InitiativeState;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModelException;

public class BaseController {

    static final String REQUEST_MESSAGES_KEY = "requestMessages";
    
    public final String ALT_URI_ATTR = "altUri";
    public final String CURRENT_URI_ATTR = "currentUri";

    public final String OM_PICIW_ID = "omPiwicId";
    
    @Resource HttpUserService userService;
    
    @Resource BeansWrapper freemarkerObjectWrapper;

    @Resource FlowStateAnalyzer flowStateAnalyzer;
    
    private final boolean optimizeResources;
    
    private final String resourcesVersion;

    private final Optional<Integer> omPiwicId;
    
    public BaseController(boolean optimizeResources, String resourcesVersion) {
        this(optimizeResources, resourcesVersion, Optional.<Integer>absent());
    }
    
    public BaseController(boolean optimizeResources, String resourcesVersion, Optional<Integer> omPiwicId) {
        this.optimizeResources = optimizeResources;
        this.resourcesVersion = resourcesVersion;
        this.omPiwicId = omPiwicId;
        InfoRibbon.refreshInfoRibbonTexts();
    }
    
    @ModelAttribute
    public void addModelDefaults(Locale locale, HttpServletRequest request, Model model) {
        Urls urls = Urls.get(locale);
        model.addAttribute("currentUser", userService.getCurrentUser(false)); // Purely informative at this point
        model.addAttribute("locale", urls.getLang());
        model.addAttribute("altLocale", urls.getAltLang());
        model.addAttribute("urls", urls);
        model.addAttribute("fieldLabelKey", FieldLabelKeyMethod.INSTANCE);
        model.addAttribute(REQUEST_MESSAGES_KEY, getRequestMessages(request));
        model.addAttribute("flowStateAnalyzer", flowStateAnalyzer);
        model.addAttribute("summaryMethod", SummaryMethod.INSTANCE);
        model.addAttribute("optimizeResources", optimizeResources);
        model.addAttribute("resourcesVersion", resourcesVersion);
        model.addAttribute(CURRENT_URI_ATTR, urls.getBaseUrl() + request.getRequestURI());
        model.addAttribute("infoRibbon", InfoRibbon.getInfoRibbonText(locale));
        
        try {
            model.addAttribute("UrlConstants", freemarkerObjectWrapper.getStaticModels().get(Urls.class.getName()));
            model.addAttribute("InitiativeConstants", freemarkerObjectWrapper.getStaticModels().get(InitiativeConstants.class.getName()));
        } catch (TemplateModelException e) {
            throw new RuntimeException(e);
        }
        
        addEnum(InitiativeState.class, model);
        addEnum(EditMode.class, model);
        addEnum(RequestMessage.class, model);
        addEnum(RequestMessageType.class, model);
        addEnum(FlowState.class, model);
        addEnum(HelpPage.class, model);
        addEnum(InfoPage.class, model);
    }
    
    static void addRequestMessage(RequestMessage requestMessage, Model model, HttpServletRequest request) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        addListElement(flashMap, REQUEST_MESSAGES_KEY, requestMessage);
        if (model != null) {
            addListElement(model.asMap(), REQUEST_MESSAGES_KEY, requestMessage);
        }
    }
    
    private static <T> void addListElement(Map<? super String, ? super List<T>> map, String key, T value) {
        @SuppressWarnings("unchecked")
        List<T> list = (List<T>) map.get(key);
        if (list == null) {
            list = Lists.newArrayList();
            map.put(key, list);
        }
        list.add(value);
    }

    protected String redirectWithMessage(String targetUri, RequestMessage requestMessage, HttpServletRequest request) {
        addRequestMessage(requestMessage, null, request);
        return contextRelativeRedirect(targetUri);
    }
    
    

    @SuppressWarnings("unchecked")
    private List<RequestMessage> getRequestMessages(HttpServletRequest request) {
        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);
        if (flashMap != null) {
            return (List<RequestMessage>) flashMap.get(REQUEST_MESSAGES_KEY);
        } else {
            return Lists.newArrayList();
        }
    }

    private <T extends Enum<?>> void addEnum(Class<T> enumType, Model model) {
        Map<String, T> values = Maps.newHashMap();
        for (T value : enumType.getEnumConstants()) {
            values.put(value.name(), value);
        }
        model.addAttribute(enumType.getSimpleName(), values);
    }

    protected void addPiwicIdIfNotAuthenticated(Model model) {
        boolean isAuthenticated = userService.getCurrentUser(false).isAuthenticated();
        if (!isAuthenticated) {
            model.addAttribute(OM_PICIW_ID, omPiwicId.orNull());
        }
    }

}