package util;


import data.*;


public class KMSConnector {

    public static final String DEFAULT_URL = "https://localhost:8280/";

    private final Connection connection;

    public KMSConnector(String url)
    {
        connection = new Connection(url);
    }


    public  KeyResponse  getKAandSignPubKeys(KeyRequest request)
    {
        return connection.send(request, "issuekey", KeyResponse.class);
    }

}
