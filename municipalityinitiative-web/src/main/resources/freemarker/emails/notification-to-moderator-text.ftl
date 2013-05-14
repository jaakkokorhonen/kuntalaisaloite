<#import "../components/email-utils.ftl" as u />
<#import "../components/email-blocks.ftl" as b />

<#assign type="text" />

<@u.message "email.notification.to.moderator.title" />


<@b.initiativeDetails type=type showProposal=true showDate=true />
    
----
<@b.contactInfo type />
----
    
<@u.message "email.notification.to.moderator.moderateLink" />:
${urls.moderation(initiative.id)}

----

<@b.emailFooter type />