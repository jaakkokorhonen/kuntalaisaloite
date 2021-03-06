<#import "../components/email-layout-html.ftl" as l />
<#import "../components/email-utils.ftl" as u />
<#import "../components/email-blocks.ftl" as b />

<#assign type="html" />

<#include "../includes/styles.ftl" />

<#escape x as x?html>

    <#assign title><@u.message "email.initiative" /></#assign>

    <@l.emailHtml title=title footer=true>
        <@b.mainContentBlock title>
            <@b.municipalityDecision type />
        </@b.mainContentBlock>

        <@u.spacer "15" />

    </@l.emailHtml>

</#escape>