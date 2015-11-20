<#import "../components/email-layout-html.ftl" as l />
<#import "../components/email-utils.ftl" as u />
<#import "../components/email-blocks.ftl" as b />

<#assign type="html" />

<#include "../includes/styles.ftl" />

<#escape x as x?html>

    <#assign title><@u.message "email.initiative" /> - ${initiative.municipality.getLocalizedName(locale)}</#assign>

    <@l.emailHtml title>


        <@b.contentBlock type>
            <@b.municipalityDecision type />
        </@b.contentBlock>


        <@u.spacer "15" />

    </@l.emailHtml>

</#escape>