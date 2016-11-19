package de.neuland.hybris.http.helper;


public class JSessionCsrfPair {

    private String jSession;
    private String csrf;

    public JSessionCsrfPair(String jSession, String csrf) {
        this.jSession = jSession;
        this.csrf = csrf;
    }

    public String getJSession()
    {
        return jSession;
    }

    public String getCsrf()
    {
        return csrf;
    }

    @Override
    public String toString()
    {
        return "JSessionCsrfPair{" +
                "jSession='" + jSession + '\'' +
                ", csrf='" + csrf + '\'' +
                '}';
    }
}
