package uk.nhs.fhir.makehtml.html;

/**
 * Created by kevinmayfield on 05/05/2017.
 */
public class Dstu2Fix {

    public static String dstu2links(String value)
    {
        if (value.contains("http://hl7.org/fhir/")) {

             if  (value.contains("http://hl7.org/fhir/v3")) {
                 // http://hl7.org/fhir/v3/MaritalStatus
                 // http://hl7.org/fhir/DSTU2/v3/MaritalStatus/index.html
                 value = value.replace("http://hl7.org/fhir/v3/", "http://hl7.org/fhir/DSTU2/v3/");
                 value += "/index.html";
             } if  (value.contains("http://hl7.org/fhir/ValueSet/")) {
                 value = value.replace("http://hl7.org/fhir/ValueSet/", "http://hl7.org/fhir/DSTU2/valueset-");
                 value += ".html";
             }
             else {
                 value = value.replace("http://hl7.org/fhir/", "http://hl7.org/fhir/DSTU2/valueset-");
                 value += ".html";
             }
        }
        return value;
    }
}
