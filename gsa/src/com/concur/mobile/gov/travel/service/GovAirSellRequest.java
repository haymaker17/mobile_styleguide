/**
 * 
 */
package com.concur.mobile.gov.travel.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import android.util.Xml;
import android.util.Xml.Encoding;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.air.service.AirSellRequest;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.util.ViewUtil;

/**
 * An extension of <code>AirSellRequest</code> for booking a flight for gov users.
 */
public class GovAirSellRequest extends AirSellRequest {

    public String existingTANumber;

    public String perdiemLocationID;
    public String country;
    public String zipCode;
    public String state;
    public String name;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {

        StringBuilder strBldr = new StringBuilder();

        strBldr.append("<AirSellCriteria>");
        ViewUtil.addXmlElement(strBldr, "CcId", ccId);
        if (fields != null) {
            TravelCustomField.serializeToXMLForWire(strBldr, fields, false);
        }
        ViewUtil.addXmlElement(strBldr, "ExistingTANumber", existingTANumber);
        ViewUtil.addXmlElement(strBldr, "FareId", fareId);
        ViewUtil.addXmlElement(strBldr, "PerdiemLocationID", perdiemLocationID);
        ViewUtil.addXmlElement(strBldr, "ProgramId", programId);
        ViewUtil.addXmlElementTF(strBldr, "RefundableOnly", refundableOnly);
        ViewUtil.addXmlElement(strBldr, "TripName", tripName);
        strBldr.append("<USGovtPerDiemLocation>");
        if (country != null) {
            addElement(strBldr, "Country", country);
        }
        if (name != null) {
            addElement(strBldr, "Name", name);
        }
        if (state != null) {
            addElement(strBldr, "State", state);
        }
        if (zipCode != null) {
            addElement(strBldr, "ZipCode", zipCode);
        }
        strBldr.append("</USGovtPerDiemLocation>");
        ViewUtil.addXmlElement(strBldr, "ViolationCode", violationCode);
        ViewUtil.addXmlElement(strBldr, "ViolationJustification", violationJustification);
        // TODO: Need to set violation reasons.
        // See http://172.17.61.100/qawiki/index.php/Mobile_WS_Travel_Endpoints#AirSell
        // <Violations>
        // <ViolationReason>
        // <RuleValueID>1111</RuleValueID>
        // <ViolationCode>3434</ViolationCode>
        // <ViolationJustification>zoom-zoom</ViolationJustification>
        // </ViolationReason>
        // </Violations>
        strBldr.append("</AirSellCriteria>");
        return strBldr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        GovAirSellReply reply = new GovAirSellReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            Encoding encoding = null;
            try {
                String encodingHeader = response.getContentEncoding();
                encoding = (encodingHeader != null) ? Encoding.valueOf(encodingHeader) : Xml.Encoding.UTF_8;
            } catch (Exception e) {
                // Couldn't parse the encoding in the header, so just default to UTF-8;
                encoding = Xml.Encoding.UTF_8;
            }
            reply = GovAirSellReply.parseXMLReply(is, encoding);
        }
        return reply;
    }

    @Override
    protected int getSoTimeout() {
        return 300000;
    }

}
