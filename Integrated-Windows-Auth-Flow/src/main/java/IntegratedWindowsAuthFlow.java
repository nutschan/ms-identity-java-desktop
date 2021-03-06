// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IntegratedWindowsAuthenticationParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.Future;

public class IntegratedWindowsAuthFlow {

    final static String PUBLIC_CLIENT_ID = "Enter_the_Application_Id_here";
    final static String AUTHORITY_ORGANIZATION = "https://login.microsoftonline.com/organizations/";
    final static String GRAPH_DEFAULT_SCOPE = "https://graph.microsoft.com/.default";
    final static String USER_NAME = "Enter_User_Name";

    public static void main(String args[]) throws Exception {

        IAuthenticationResult result = getAccessTokenByIntegratedAuth();

        try {
            String usersListFromGraph = getUsersListFromGraph(result.accessToken());
            System.out.println("Users in the Tenant = " + usersListFromGraph);

            System.out.println("Press any key to exit ...");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    private static IAuthenticationResult getAccessTokenByIntegratedAuth() throws Exception {
        PublicClientApplication app = PublicClientApplication.builder(PUBLIC_CLIENT_ID)
                .authority(AUTHORITY_ORGANIZATION)
                .build();

        IntegratedWindowsAuthenticationParameters parameters =
                IntegratedWindowsAuthenticationParameters.builder(
                        Collections.singleton(GRAPH_DEFAULT_SCOPE), USER_NAME)
                        .build();

        Future<IAuthenticationResult> future = app.acquireToken(parameters);

        IAuthenticationResult result = future.get();
        return result;
    }

    private static String getUsersListFromGraph(String accessToken) throws IOException {
        URL url = new URL("https://graph.microsoft.com/v1.0/users");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept","application/json");

        int httpResponseCode = conn.getResponseCode();
        if(httpResponseCode == 200) {

            StringBuilder response;
            try(BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))){

                String inputLine;
                response = new StringBuilder();
                while (( inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            return response.toString();
        } else {
            return String.format("Connection returned HTTP code: %s with message: %s",
                    httpResponseCode, conn.getResponseMessage());
        }
    }
}