package com.sweepr.upnpdiscovery;

import android.content.Context;

import com.google.gson.JsonSyntaxException;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class UPnPDevice {

    private static String LOCATION_TEXT = "LOCATION: ";
    private static String SERVER_TEXT = "SERVER: ";
    private static String USN_TEXT = "USN: ";
    private static String ST_TEXT = "ST: ";

    private static final String LINE_END = "\r\n";

    // From SSDP Packet
    private String mHostAddress;
    // SSDP Packet Header
    private String mHeader;
    private String mLocation;
    private String mServer;
    private String mUSN;
    private String mST;

    // XML content
    private String mXML;

    // From desctiption XML
    private String mDeviceType;
    private String mFriendlyName;
    private String mPresentationURL;
    private String mSerialNumber;
    private String mModelName;
    private String mModelNumber;
    private String mModelURL;
    private String mManufacturer;
    private String mManufacturerURL;
    private String mUDN;
    private String mURLBase;

    public UPnPDevice(String hostAddress, String header, Context context) {
        this.mHeader = header;
        this.mHostAddress = hostAddress;
        this.mLocation = parseHeader(header, LOCATION_TEXT);
        this.mServer = parseHeader(header, SERVER_TEXT);
        this.mUSN = parseHeader(header, USN_TEXT);
        this.mST = parseHeader(header, ST_TEXT);
    }

    public UPnPDevice(String hostAddress, String location, String serialnumber, String serviceType, Context context) {
        this.mHostAddress = hostAddress;
        this.mLocation = location;
        this.mUSN = serialnumber;
        this.mST = serviceType;
        this.mServer = "";
    }

    public void update(String xml) {
        this.mXML = xml;
        xmlParse(xml);
    }

    public String toString() {
        return "FriendlyName: " + mFriendlyName + LINE_END +
                "ModelName: " + mModelName + LINE_END +
                "HostAddress: " + mHostAddress + LINE_END +
                "Location: " + mLocation + LINE_END +
                "Server: " + mServer + LINE_END +
                "USN: " + mUSN + LINE_END +
                "ST: " + mST + LINE_END +
                "DeviceType: " + mDeviceType + LINE_END +
                "PresentationURL: " + mPresentationURL + LINE_END +
                "SerialNumber: " + mSerialNumber + LINE_END +
                "ModelURL: " + mModelURL + LINE_END +
                "ModelNumber: " + mModelNumber + LINE_END +
                "Manufacturer: " + mManufacturer + LINE_END +
                "ManufacturerURL: " + mManufacturerURL + LINE_END +
                "UDN: " + mUDN + LINE_END +
                "URLBase: " + mURLBase;
    }

    private String parseHeader(String mSearchAnswer, String whatSearch) {
        String result = "";
        int searchLinePos = mSearchAnswer.indexOf(whatSearch);
        if (searchLinePos != -1) {
            searchLinePos += whatSearch.length();
            int locColon = mSearchAnswer.indexOf(LINE_END, searchLinePos);
            result = mSearchAnswer.substring(searchLinePos, locColon);
        }
        return result;
    }

    private void xmlParse(String xml) {
        XmlParserCreator parserCreator = new XmlParserCreator() {
            @Override
            public XmlPullParser createParser() {
                try {
                    return XmlPullParserFactory.newInstance().newPullParser();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        GsonXml gsonXml = new GsonXmlBuilder()
                .setXmlParserCreator(parserCreator)
                .create();
        DescriptionModel model = null;
        try {
            model = gsonXml.fromXml(xml, DescriptionModel.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (model != null) {
            this.mFriendlyName = model.device.friendlyName == null ? "" : model.device.friendlyName;
            this.mDeviceType = model.device.deviceType == null ? "" : model.device.deviceType;
            this.mPresentationURL = model.device.presentationURL == null ? "" : model.device.presentationURL;
            this.mSerialNumber = model.device.serialNumber == null ? "" : model.device.serialNumber;
            this.mModelName = model.device.modelName == null ? "" : model.device.modelName;
            this.mModelNumber = model.device.modelNumber == null ? "" : model.device.modelNumber;
            this.mModelURL = model.device.modelURL == null ? "" : model.device.modelURL;
            this.mManufacturer = model.device.manufacturer == null ? "" : model.device.manufacturer;
            this.mManufacturerURL = model.device.manufacturerURL == null ? "" : model.device.manufacturerURL;
            this.mUDN = model.device.UDN == null ? "" : model.device.UDN;
            this.mURLBase = model.URLBase == null ? "" : model.URLBase;
        }

    }

    private static class Device {
        private String deviceType;
        private String friendlyName;
        private String presentationURL;
        private String serialNumber;
        private String modelName;
        private String modelNumber;
        private String modelURL;
        private String manufacturer;
        private String manufacturerURL;
        private String UDN;

    }

    private static class DescriptionModel {
        private Device device;
        private String URLBase;

    }

    public String getHostAddress() {
        return mHostAddress;
    }

    public String getHeader() {
        return mHeader;
    }

    public String getST() {
        return mST;
    }

    public String getUSN() {
        return mUSN;
    }

    public String getServer() {
        return mServer;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getDescriptionXML() {
        return mXML;
    }

    public String getDeviceType() {
        return mDeviceType;
    }

    public String getFriendlyName() {
        return mFriendlyName;
    }

    public String getPresentationURL() {
        return mPresentationURL;
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }

    public String getModelName() {
        return mModelName;
    }

    public String getModelNumber() {
        return mModelNumber;
    }

    public String getModelURL() {
        return mModelURL;
    }

    public String getManufacturer() {
        return mManufacturer;
    }

    public String getManufacturerURL() {
        return mManufacturerURL;
    }

    public String getUDN() {
        return mUDN;
    }

    public String getURLBase() {
        return mURLBase;
    }

}
