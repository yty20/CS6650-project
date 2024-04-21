import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.ResortsApi;

import java.io.File;
import java.util.*;

public class ResortsApiExample {

    public static void main(String[] args) {

        ResortsApi apiInstance = new ResortsApi();
        ResortIDSeasonsBody body = new ResortIDSeasonsBody(); // ResortIDSeasonsBody | Specify new Season value
        Integer resortID = 56; // Integer | ID of the resort of interest
        try {
            apiInstance.addSeason(body, resortID);
        } catch (ApiException e) {
            System.err.println("Exception when calling ResortsApi#addSeason");
            e.printStackTrace();
        }
    }
}