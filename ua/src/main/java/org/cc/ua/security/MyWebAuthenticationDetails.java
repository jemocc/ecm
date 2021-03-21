package org.cc.ua.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class MyWebAuthenticationDetails extends WebAuthenticationDetails {
    private static final String verifyCode = "verifyCode";
    private final String savedVerifyCode;
    private final String requestVerifyCode;
    /**
     * Records the remote address and will also set the session Id if a session already
     * exists (it won't create one).
     *
     * @param request that the authentication request was received from
     */
    public MyWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        HttpSession session = request.getSession();
        this.savedVerifyCode = (String) session.getAttribute(verifyCode);
        this.requestVerifyCode = request.getParameter(verifyCode);
        if (this.savedVerifyCode != null) {
            session.removeAttribute(verifyCode);
        }
    }

    public String getSavedVerifyCode() {
        return savedVerifyCode;
    }

    public String getRequestVerifyCode() {
        return requestVerifyCode;
    }
}
