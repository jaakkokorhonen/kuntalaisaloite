<#import "/spring.ftl" as spring />
<#import "utils.ftl" as u />
<#import "forms.ftl" as f />

<#escape x as x?html> 

<#--
 * blockHeader
 *
 * Block header for management-view.
 * 
 * @param key is for example "initiative.basicDetails.title"
 * @param step is the number of current block
 -->
<#macro blockHeader key step=0>
    <div id="step-header-${step}" class="content-block-header edit ${(step == 1)?string('open','')}">
        <h2>${step}. <@u.message key!"" /></h2><span class="arrow hidden"> </span>
    </div>
</#macro>

<#--
 * buttons
 *
 * Save and cancel buttons used in block-edit-mode.
 * 
 * @param type is the type of the button: next, save-and-send, save
 * @param nextStep is the number of the following block
 -->
<#macro buttons type="" nextStep="0">
    <#if type == "next">
        <button type="submit" id="action-send-confirm" name="action-send-confirm" class="small-button" value="true" ><span class="small-icon next"><@u.message "action.prepare.next" /></span></button>
    <#elseif type == "verify">
        <button value="true" class="small-button" name="action-send-confirm" id="action-send-confirm" type="submit"><span class="small-icon ${user.isVerifiedUser()?string("save-and-send","next")}"><@u.message "action.prepare."+user.isVerifiedUser()?string("create","authenticate") /></span></button>
    <#elseif type == "save">
        <button type="submit" id="action-send-confirm" name="action-send-confirm" class="small-button" value="true" ><span class="small-icon save-and-send"><@u.message "action.prepare.send" /></span></button>
    </#if>
</#macro>

<#--
 * municipalityBlock
 *
 * Choose municipality
 * Prints help-texts and validation errors in this block
 *
 * @param step is the number of current block
 -->
<#macro municipalityBlock municipality="">      
    
    <div class="input-block-extra">
        <div class="input-block-extra-content">
            <@f.helpText "help.municipality" />
            <@f.helpText "help.homeMunicipality" />
        </div>
    </div>

    <div class="input-block-content">
        <#assign href="${urls.help(HelpPage.ORGANIZERS.getUri(locale))}" />
        <@u.systemMessage path="initiative.municipality.description" type="info" showClose=false args=[href] />
    </div>
    
    <div class="input-block-content">       
        <@f.municipalitySelect path="initiative.municipality" options=municipalities required="required" cssClass="municipality-select" preSelected=municipality onlyActive=true/>
    </div>
    <div class="input-block-content">
        <#if user.isVerifiedUser() && user.homeMunicipality.present>
            <input type="hidden" name="homeMunicipality" value="${user.homeMunicipality.value.id}" />
            <div class="input-header"><@u.message "contactInfo.homeMunicipality" /></div>
            <div id="verifiedHomeMunicipality" class="input-placeholder" data-initiative-municipality="${user.homeMunicipality.value.id}"><@u.solveMunicipality user.homeMunicipality /></div>
        <#else>
            <@f.municipalitySelect path="initiative.homeMunicipality" options=municipalities required="required" cssClass="municipality-select" preSelected=municipality />
        </#if>
    </div>
    <br class="clear" />
    
    <noscript>
        <div class="input-block-content">
            <div class="system-msg msg-info">
                <#assign href= "${urls.help(HelpPage.ORGANIZERS.getUri(locale))}" />
                <@u.messageHTML key="initiative.municipality.different" args=[href] />
            </div>
        </div>
    </noscript>
    
    <div id="municipalMembership" class="municipality-not-equal js-hide">
        <div class="input-block-content hidden">
            <#assign href="${urls.help(HelpPage.ORGANIZERS.getUri(locale))}" />
            <@u.systemMessage path="initiative.municipality.notEqual" type="info" showClose=false args=[href] />
        </div>
        <div class="input-block-content">
            <@f.radiobutton path="initiative.municipalMembership" required="required" options={
                "community":"initiative.municipalMembership.community",
                "company":"initiative.municipalMembership.company",
                "property":"initiative.municipalMembership.property"

            } attributes="" />
            <br/>
            <@f.radiobutton path="initiative.municipalMembership" required="required" options={
                "none":"initiative.municipalMembership.none"
            } attributes="" header=false/>

        </div>
        
        <div class="input-block-content is-not-member no-top-margin js-hide hidden">
            <@u.systemMessage path="warning.initiative.notMember" type="warning" showClose=false />
        </div>
    </div>
</#macro>

<#--
 * chooseInitiativeType
 *
 * Choose the type of the initiative
 * - Normal
 * - 2% and 5% with VETUMA
 *
 * Prints help-texts and validation errors in this block
 -->
<#macro chooseInitiativeType>
    <div class="input-block-content">
        <div class="input-header">
            <@u.message "initiative.initiativeType" /> <span class="icon-small required trigger-tooltip"></span>
        </div>
    </div>
    
    <div class="initiative-types cf">
        <@spring.bind "initiative.initiativeType" /> 
        <@f.showError />
        
        <@initiativeTypeBlock InitiativeType.UNDEFINED true />
        <@initiativeTypeBlock InitiativeType.COLLABORATIVE_COUNCIL enableVerifiedInitiatives />
        <@initiativeTypeBlock InitiativeType.COLLABORATIVE_CITIZEN enableVerifiedInitiatives />
    </div>
</#macro>

<#--
 * initiativeTypeBlock
 *
 * Generates a selection for initiative type
 *
 * NOTE that initiative type UNDEFINED refers to types SINGLE and COLLABORATIVE.
 * But since we cannot determine the type yet in this phase, we use type UNDEFINED. 
 *
 * @param type is the type of the initiative
 * @param enabled enables/disables this selection
 -->
<#macro initiativeTypeBlock type enabled=false>
    <#assign verifiable=false />
    <#if type == InitiativeType.COLLABORATIVE_COUNCIL || type == InitiativeType.COLLABORATIVE_CITIZEN>
        <#assign verifiable=true />
    </#if>
    <#if type == InitiativeType.COLLABORATIVE_COUNCIL>
        <#assign typeNumber = 2 />
    <#elseif type == InitiativeType.COLLABORATIVE_CITIZEN>
        <#assign typeNumber = 3 />
    <#else>
        <#assign typeNumber = 1 />
    </#if>

    <#if enabled>
        <label class="initiative-type enabled ${(spring.stringStatusValue == type)?string("selected","")}" data-verifiable="${verifiable?string}">
    <#else>
        <label class="initiative-type trigger-tooltip" title="<@u.message "initiative.initiativeType.disabled.tooltip" />">
    </#if>
        <span class="inner">
            <#-- TODO: Finalize type icon -->
            <span class="icon-32 secondary">${typeNumber}</span><span class="type"><@u.message "initiative.initiativeType."+type /><#if type == "UNDEFINED"><br/><br/></#if></span>
            <span class="description"><@u.message "initiative.initiativeType."+type+".description" /></span>
        </span>
        <#if enabled>
            <span class="action open">
                <span class="checkbox hidden <#if spring.stringStatusValue == type>checked</#if>"></span>
                <input type="radio" name="${spring.status.expression}" value="${type}" class="js-hide" required
                <#if spring.stringStatusValue == type>checked="checked"</#if>
                <@spring.closeTag/>
                <span class="push" data-choose="<@u.message "initiative.initiativeType.choose" />" data-chosen="<@u.message "initiative.initiativeType.chosen" />">
                    <#if spring.stringStatusValue == type>
                        <@u.message "initiative.initiativeType.chosen" />
                    <#else>
                        <@u.message "initiative.initiativeType.choose" />
                    </#if>
                </span>
            </span>
            
        <#else>
            <span class="action blocked">
                <span class="checkbox disabled"></span>
                <span class="push"><@u.message "initiative.initiativeType.disabled" /></span>
            </span>
        </#if>
    </label>

</#macro>

<#--
 * authorEmailBlock
 *
 * Add confirmation email for author
 * Prints help-texts and validation errors in this block
 *
 * Noscript user gets two different versions weather he/she is authenticated or not.
 -->
<#macro authorEmailBlock noscript=false>
    <div class="input-block-extra">
        <div class="input-block-extra-content">
            <@f.helpText "help.participantEmail" />
        </div>
    </div>
    
    <div class="input-block-content">
        <#if noscript>
            <@u.systemMessage path="initiative.participantEmail.description.noscript"+user.isVerifiedUser()?string(".verifiedUser","") type="info" showClose=false />
        <#else>
            <@u.systemMessage path="initiative.participantEmail.description" type="info" showClose=false />
        </#if>
    </div>

    <div class="input-block-content">
        <@f.textField path="initiative.participantEmail" required="required" optional=false cssClass="large" maxLength=InitiativeConstants.CONTACT_EMAIL_MAX />
    </div>
</#macro>


<#--
 * initiativeBlock
 *
 * Add/edit initiative title, content and extra-info
 * Prints help-texts and validation errors in this block
 *
 * @param locked locks some field from editing
 -->
<#macro initiativeBlock path>
    <div class="input-block cf">
        <div class="input-block-extra">
            <div class="input-block-extra-content">
                <@f.helpText "help.name" />
                <@f.helpText "help.proposal" />
                <@f.helpText "help.extraInfo" />
            </div>
        </div>

        <div class="input-block-content">
            <#assign href="${urls.help(HelpPage.ORGANIZERS.getUri(locale))}" />
            <@u.systemMessage path="initiative.proposal.description" type="info" showClose=false args=[href] />
        </div>
        
        <div class="input-block-content">
            <@f.textField path=path+".name" key="initiative.name" required="required" optional=true cssClass="large" maxLength=InitiativeConstants.INITIATIVE_NAME_MAX />
        </div>
        
        <div class="input-block-content no-top-margin">
            <@f.textarea path=path+".proposal" key="initiative.proposal" required="required" optional=false cssClass="textarea-tall" maxLength=InitiativeConstants.INITIATIVE_PROPOSAL_MAX?string("#") />
        </div>
        
        <div class="input-block-content">
            <@f.textarea path=path+".extraInfo" key="initiative.extraInfo" required="" optional=true cssClass="textarea" maxLength=InitiativeConstants.INITIATIVE_EXTRA_INFO_MAX?string("#") />
        </div>
        
    </div>
</#macro>

<#--
 * updateInitiativeBlock
 *
 * Title and content are NOT editable
 * Update initiative extra-info details
 *
 * Prints help-texts and validation errors in this block
 *
 * @param locked locks some field from editing
 -->
<#macro updateInitiativeBlock path>
    <div class="input-block cf">
        <div class="input-block-extra">
            <div class="input-block-extra-content">
                <@f.helpText "help.extraInfo" />
                <#if initiative.collaborative && initiative.state == InitiativeState.PUBLISHED>
                    <@f.helpText "help.externalParticipantCount" />
                </#if>
            </div>
        </div>

        <div class="input-block-content">
            <@f.textarea path=path+".extraInfo" required="" optional=true cssClass="textarea" key="initiative.extraInfo" maxLength=InitiativeConstants.INITIATIVE_EXTRA_INFO_MAX?string("#") />
        </div>
        
        <#if initiative.collaborative && initiative.state == InitiativeState.PUBLISHED>
            <div class="input-block-content">
                <@f.textField path=path+".externalParticipantCount" required="" cssClass="small" optional=false  maxLength=7 />
            </div>
        </#if>
    </div>
</#macro>


<#--
 * currentAuthorBlock
 *
 * Used in normal initiatives
 *
 * Add author details
 *  - Name, Home municipality, suffrage
 *  - Email address, phone, street address
 *
 * Prints help-texts and validation errors in this block
 *
 * @param step is the number of current block
 -->
<#macro currentAuthorBlock path>
    <div class="input-block cf">
        <div class="input-block-extra">
            <div class="input-block-extra-content">
                <@f.helpText "help.contactInfo.name" />
                <@f.helpText "help.contactInfo.contactDetails" />
            </div>
        </div>

        <div class="input-block-content">
            <@u.systemMessage path="contactInfo.ownDetails.description" type="info" showClose=false />  
        </div>
        
        <div class="input-block-content">
            <div class="column col-2of3">
                <@f.textField path=path+".contactInfo.name" required="required" optional=false cssClass="medium" maxLength=InitiativeConstants.CONTACT_NAME_MAX key="contactInfo.name" />
                
            </div>
            <div class="column col-1of3 last">
                <div class="input-header"><@u.message "contactInfo.homeMunicipality" /></div>
                <div class="input-placeholder"><@u.solveMunicipality author.municipality/></div>
            </div>
            <br class="clear" />
            <@f.formCheckbox path=path+".contactInfo.showName" checked=true key="contactInfo.showName" />
        </div>

        <div class="input-block-content">
            <@u.systemMessage path="contactInfo.description" type="info" showClose=false />
        </div>

        <div class="input-block-content">
            <div class="input-header">
                <@u.message "contactInfo.title" />
            </div>
            
            <@f.contactInfo path=path+".contactInfo" mode="full" />
        </div>
    </div>
</#macro>

<#--
 * currentVerifiedAuthorBlock
 *
 * Used in VETUMA initiatives
 *
 * Add author details
 *  - Name, Home municipality, suffrage
 *  - Email address, phone, street address
 *
 * Prints help-texts and validation errors in this block
 *
 * @param step is the number of current block
 -->
<#macro currentVerifiedAuthorBlock path>
    <div class="input-block cf">
        <div class="input-block-extra">
            <div class="input-block-extra-content">
                <@f.helpText "help.contactInfo.name" />
                <@f.helpText "help.contactInfo.contactDetails" />
            </div>
        </div>

        <div class="input-block-content">
            <@u.systemMessage path="contactInfo.ownDetails.description" type="info" showClose=false />
        </div>

        <div class="input-block-content">
            <div class="column col-2of3">
                <label class="input-header"><@u.message "contactInfo.verified.name" /></label>
                <div class="input-placeholder">${author.contactInfo.name}</div>
            </div>
            <div class="column col-1of3 last">
                <div class="input-header"><@u.message "contactInfo.homeMunicipality" /></div>

                <div class="input-placeholder"><@u.solveMunicipality author.municipality/></div>
            </div>
            <br class="clear" />
            <@f.formCheckbox path=path+".contactInfo.showName" checked=true key="contactInfo.showName" />
        </div>

        <div class="input-block-content">
            <@u.systemMessage path="contactInfo.description" type="info" showClose=false />
        </div>

        <div class="input-block-content">
            <div class="input-header">
                <@u.message "contactInfo.title" />
            </div>

            <@f.verifiedContactInfo path=path+".contactInfo" />
        </div>
    </div>
</#macro>
      
</#escape> 

