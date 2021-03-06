<#import "/spring.ftl" as spring />
<#import "components/forms.ftl" as f />
<#import "components/layout.ftl" as l />
<#import "components/utils.ftl" as u />
<#import "components/edit-blocks.ftl" as edit />
<#import "components/elements.ftl" as e />

<#escape x as x?html>

<#--
 * Layout parameters for HTML-title and navigation.
 *
 * @param page is "page.update"
 * @param pageTitle can be assigned as custom HTML title
-->
<#assign page="page.update" />

<@l.main page pageTitle!"">

    <#assign locationSelected = updateData.locations?? && updateData.locations?size gt 0/>

    <#-- Create form errors summary -->
    <@u.errorsSummary path="updateData.*" prefix="updateData."/>

    <noscript>
        <@f.cookieWarning springMacroRequestContext.requestUri />
    </noscript>

    <div class="msg-block">
        <div class="system-msg msg-info">
            <h2><@u.message "initiative.update.title" /></h2>
            <p><@u.message "initiative.update.description" /></p>
            <@f.fieldRequiredInfo />
        </div>
    </div>

    <@e.initiativeTitle initiative />
    
    <div class="view-block public first">
        <div class="initiative-content-row">
            <@e.initiativeView initiative />
        </div>
        
        <div class="initiative-content-row last">
            <h2><@u.message key="initiative.authors.title" args=[authors?size] /></h2>
            <@e.initiativeContactInfo showTitle=false authorList=authors showTitle=false />
        </div>
    </div>

    <#-- FORM. Use class 'sodirty' to enable dirtylisten. -->
    <form action="${springMacroRequestContext.requestUri}" method="POST" id="form-initiative" class="sodirty dirtylisten <#if hasErrors>has-errors</#if>" novalidate>

        <@f.securityFilters />

        <div class="form-block-container">
            <@edit.blockHeader key="initiative.updateInitiative.title" step=1 />
            <@edit.updateInitiativeBlock "updateData" locationSelected/>
        </div>

        <div class="form-block-container">
            <@edit.blockHeader key="initiative.updateCurrentAuthor.title" step=2 />
            <#if initiative.isVerifiable()>
                <@edit.currentVerifiedAuthorBlock "updateData" />
            <#else>
                <@edit.currentAuthorBlock "updateData" />
            </#if>
        </div>
        
        <div class="" id="form-action-panel">
            <button class="large-button" value="true" name="${UrlConstants.ACTION_UPDATE_INITIATIVE}" type="submit"><span class="large-icon save-and-send"><@u.messageHTML "action.updateInitiative" /></span></button>
            <a href="${previousPageURI!urls.baseUrl+"/"+locale}" class="large-button"><span class="large-icon cancel"><@u.messageHTML "action.cancelUpdateInitiative" /></span></a>
        </div>
        
        
    </form>

<#--
 * Create page modals and jsMessage
 *
 * Uses jsRender for templating.
 * Same content is generated for NOSCRIPT and for modals.
 *
 * Modals:
 *  Form modified notification (dirtyform)
 *
 * jsMessage:
 *  Warning if cookies are disabled
-->
<@u.modalTemplate />
<@u.jsMessageTemplate />
<#if googleMapsEnabled>
    <@u.jsGoogleMapsLib />
</#if>
<script type="text/javascript">
    var modalData = {};

    modalData.mapContainer = function() {
        return [{
            title:      '<@u.message "map.mapModalTitle" />',
            content:    '<#noescape>${edit.mapContainer?replace("'","&#39;")}</#noescape>'
        }]
    };

    modalData.initiaveMunicipality = '${initiative.municipality.getName(locale)}';

    <#-- Modal: Form modified notification. Uses dirtyforms jQuery-plugin. -->
    modalData.formModifiedNotification = function() {
        return [{
            title:      '<@u.message "form.modified.notification.title" />',
            content:    '<@u.messageHTML "form.modified.notification" />'
        }]
    };

    var messageData = {};

    <#-- jsMessage: Warning if cookies are not enabled -->
    messageData.warningCookiesDisabled = function() {
        return [{
            type:      'warning',
            content:    '<h3><@u.message "warning.cookieError.title" /></h3><div><@u.messageHTML key="warning.cookieError.description" args=[springMacroRequestContext.requestUri] /></div>'
        }]
    };

    var videoWarning = '<@u.message "warning.videoUrl"/>';
    var invalidUrlWarning = '<@u.message "invalidUrl" />';

</script>

<@edit.sessionExpired />

</@l.main>
</#escape>
