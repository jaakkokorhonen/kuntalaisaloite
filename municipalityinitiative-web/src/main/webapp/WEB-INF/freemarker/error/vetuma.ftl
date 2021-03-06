<#import "../components/layout.ftl" as l />
<#import "../components/utils.ftl" as u />

<#escape x as x?html>
<@l.error "error.vetuma.title">

    <#if ageError?? && ageError>

        <h1><@u.message "error.vetuma.under.aged.title"/></h1>

        <p><@u.messageHTML key="error.vetuma.under.aged.description" /></p>

        <p><@u.message "error.vetuma.under.aged.instruction" /></p>

    <#else>

        <h1><@u.message "error.vetuma.title"/></h1>

        <!-- Error: Vetuma authentication failed -->

        <p><@u.messageHTML key="error.vetuma.description" /></p>

        <p><@u.message "error.vetuma.instruction" /></p>

        <ul>
            <li><@u.message "error.vetuma.instruction.1"/></li>
            <li><@u.message "error.vetuma.instruction.2"/></li>
            <li><@u.message "error.vetuma.instruction.3"/></li>
        </ul>
    </#if>

    <p><@u.messageHTML key="error.vetuma.linkToFrontpage" args=[urls.baseUrl] /></p>

</@l.error>
</#escape>