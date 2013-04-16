<#import "../components/email-layout-html.ftl" as l />
<#import "../components/email-utils.ftl" as u />
<#import "../components/email-blocks.ftl" as b />

<#assign type="html" />

<#include "../includes/styles.ftl" />

<#escape x as x?html>

<#assign title><@u.message "email.notification.to.moderator.title" /></#assign>

<@l.emailHtml "notification-to-moderator" title>

    <@b.mainContentBlock title>
        <@b.initiativeDetails type />
    </@b.mainContentBlock>
    
    <@u.spacer "15" />
    
    <@b.contentBlock type>
        <@b.contactInfo type />
    </@b.contentBlock>
    
    <@u.spacer "15" />
    
    <@b.contentBlock type>
        <h4 style="${h4!""}"><@u.message "email.notification.to.moderator.moderateLink" /></h4>
        <p style="${pBottomMargin}"><@u.link urls.moderation(initiative.id) /></p>
    </@b.contentBlock>

    <@u.spacer "15" />

</@l.emailHtml>

</#escape>