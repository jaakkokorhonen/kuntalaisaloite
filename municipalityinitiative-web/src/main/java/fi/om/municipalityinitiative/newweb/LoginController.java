package fi.om.municipalityinitiative.newweb;

import fi.om.municipalityinitiative.service.UserService;
import fi.om.municipalityinitiative.web.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Locale;

import static fi.om.municipalityinitiative.web.Urls.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class LoginController extends BaseLoginController {

    @Resource
    UserService userService;

    public LoginController(String baseUrl, boolean optimizeResources, String resourcesVersion) {
        super(baseUrl, optimizeResources, resourcesVersion);
    }

    @RequestMapping(value = {LOGIN_FI, LOGIN_SV}, method = RequestMethod.GET)
    public String loginGet(@RequestParam(required=false) String target, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=ISO-8859-1");
        model.addAttribute("target", target);
        return Views.DUMMY_LOGIN_VIEW;
    }

    @RequestMapping(value = {LOGIN_FI, LOGIN_SV}, method = RequestMethod.POST)
    public RedirectView login(@RequestParam String u,
                              @RequestParam String p,
                              @RequestParam String target,
                              Model model, Locale locale, HttpServletRequest request) {
        userService.login(u, p, request);

        return new RedirectView(getValidLoginTarget(target, Urls.get(locale)), false, true, false);
    }

    @RequestMapping(value =  {SINGLE_LOGIN_FI, SINGLE_LOGIN_SV}, method = RequestMethod.GET)
    public String singleLoginGet(@PathVariable Long id,
                                 @RequestParam(PARAM_MANAGEMENT_CODE) String managementHash,
                                 Model model, Locale locale, HttpServletRequest request) {
        model.addAttribute("initiativeId", id);
        model.addAttribute("managementHash", managementHash);
        model.addAttribute(Urls.get(locale));
        return Views.SINGLE_LOGIN_VIEW;

    }

    @RequestMapping(value =  {SINGLE_LOGIN_FI, SINGLE_LOGIN_SV}, method = RequestMethod.POST)
    public RedirectView singleLogin(@PathVariable Long id,
                                    @RequestParam(PARAM_MANAGEMENT_CODE) String managementHash,
                                       Model model, Locale locale, HttpServletRequest request) {
        userService.login(id, managementHash, request);
        return new RedirectView(Urls.get(locale).getManagement(id), false, true, false);

    }

    @RequestMapping(value={LOGOUT_FI, LOGOUT_SV}, method=GET)
    public String logout(Locale locale, HttpServletRequest request, HttpServletResponse response) {
        Urls urls = Urls.get(locale);
        userService.logout(request);
        return redirectWithMessage(urls.frontpage(), RequestMessage.LOGOUT, request);
    }
}