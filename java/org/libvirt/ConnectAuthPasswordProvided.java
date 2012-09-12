package org.libvirt;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Modified ConnectAuthDefault, that takes the password in the ctor rather than prompting for it.
 *
 * @author stoty lstoll
 */
public final class ConnectAuthPasswordProvided extends ConnectAuth {

    private String password;

    public ConnectAuthPasswordProvided(String password) {
        credType = new CredentialType[] { CredentialType.VIR_CRED_AUTHNAME, CredentialType.VIR_CRED_ECHOPROMPT,
                CredentialType.VIR_CRED_REALM, CredentialType.VIR_CRED_PASSPHRASE, CredentialType.VIR_CRED_NOECHOPROMPT };
        this.password = password;
    }

    @Override
    public int callback(Credential[] cred) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            for (Credential c : cred) {
                String response = "";
                switch (c.type) {
                    case VIR_CRED_USERNAME:
                    case VIR_CRED_AUTHNAME:
                    case VIR_CRED_ECHOPROMPT:
                    case VIR_CRED_REALM:
                        response = password;
                    case VIR_CRED_PASSPHRASE:
                    case VIR_CRED_NOECHOPROMPT:
                        response = password;
                        break;
                }
                if (response.equals("") && !c.defresult.equals("")) {
                    c.result = c.defresult;
                } else {
                    c.result = response;
                }
                if (c.result.equals("")) {
                    return -1;
                }
            }
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }

}
