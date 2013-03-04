<#import "../components/email-utils.ftl" as u />
<#import "../components/email-blocks.ftl" as b />

<@u.message "email.initiative" /> - ${emailInfo.municipality.finnishName!""}

<@b.initiativeDetails "text" />

----

<@b.contactInfo "text" />

<@u.message "email.municipality.sendFrom" />:
${emailInfo.url}

<@u.message "email.footer" />