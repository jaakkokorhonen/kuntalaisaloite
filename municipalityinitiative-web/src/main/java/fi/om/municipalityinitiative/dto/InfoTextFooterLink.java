package fi.om.municipalityinitiative.dto;

public class InfoTextFooterLink {

    final String uri;
    final String subject;

    public InfoTextFooterLink(String uri, String subject) {
        this.uri = uri;
        this.subject = subject;
    }

    public String getUri() {
        return uri;
    }

    public String getSubject() {
        return subject;
    }
}


