package com.sweepr.upnpdiscovery;

import android.util.Xml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import kotlin.text.Charsets;

public class UPnPDevice {

    private static final String LOCATION_TEXT = "LOCATION: ";
    private static final String SERVER_TEXT = "SERVER: ";
    private static final String USN_TEXT = "USN: ";
    private static final String ST_TEXT = "ST: ";

    private static final String LINE_END = "\r\n";

    // From SSDP Packet
    private final String mHostAddress;
    // SSDP Packet Header
    private String mHeader;
    private final String mLocation;
    private final String mServer;
    private final String mUSN;
    private final String mST;

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

    public UPnPDevice(@NonNull String hostAddress, @NonNull String header) {
        this.mHeader = header;
        this.mHostAddress = hostAddress;
        this.mLocation = parseHeader(header, LOCATION_TEXT);
        this.mServer = parseHeader(header, SERVER_TEXT);
        this.mUSN = parseHeader(header, USN_TEXT);
        this.mST = parseHeader(header, ST_TEXT);
    }

    public UPnPDevice(@NonNull String hostAddress, @NonNull String location, @NonNull String serialnumber, @NonNull String serviceType) {
        this.mHostAddress = hostAddress;
        this.mLocation = location;
        this.mUSN = serialnumber;
        this.mST = serviceType;
        this.mServer = "";
    }

    public void update(@NonNull String xml) {
        this.mXML = xml;
        xmlParse(xml);
    }

    @NonNull
    @VisibleForTesting
    public static UPnPDevice fromXml(@NonNull String xml) {
        final UPnPDevice device = new UPnPDevice("", "", "", "");
        device.update(xml);
        return device;
    }

    @NonNull
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

    private static String parseHeader(@NonNull String mSearchAnswer, @NonNull String whatSearch) {
        String result = "";
        int searchLinePos = mSearchAnswer.indexOf(whatSearch);
        if (searchLinePos != -1) {
            searchLinePos += whatSearch.length();
            int locColon = mSearchAnswer.indexOf(LINE_END, searchLinePos);
            result = mSearchAnswer.substring(searchLinePos, locColon);
        }
        return result;
    }

    private void xmlParse(@NonNull String xml) {

        try {
            final DescriptionModel model = readXmlDocument(xml);

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
        } catch (IOException ignore) {
        } catch (XmlPullParserException e) {
        }
    }

    private static final String ns = null;

    @Nullable
    private static DescriptionModel readXmlDocument(@Nullable String xml) throws IOException, XmlPullParserException {
        if (xml == null) {
            return null;
        }

        final InputStream in = new ByteArrayInputStream(xml.getBytes(Charsets.UTF_8));
        try {
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readDescriptionModel(parser);
        } finally {
            in.close();
        }
    }

    @Nullable
    private static DescriptionModel readDescriptionModel(XmlPullParser parser) throws XmlPullParserException, IOException {
        DescriptionModel result = null;

        parser.require(XmlPullParser.START_TAG, ns, "root");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            final String name = parser.getName();
            if (name.equals("URLBase")) {
                if (result == null) {
                    result = new DescriptionModel();
                }
                result.URLBase = readText(parser);
            } else {
                if (name.equalsIgnoreCase("device")) {
                    if (result == null) {
                        result = new DescriptionModel();
                    }
                    result.device = readDevice(parser);
                } else {
                    skip(parser);
                }
            }
        }

        return result;
    }

    private static Device readDevice(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "device");

        Device result = new Device();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (name.equals("deviceType")) {
                result.deviceType = readText(parser);
            } else if (name.equals("friendlyName")) {
                result.friendlyName = readText(parser);
            } else if (name.equals("presentationURL")) {
                result.presentationURL = readText(parser);
            } else if (name.equals("serialNumber")) {
                result.serialNumber = readText(parser);
            } else if (name.equals("modelName")) {
                result.modelName = readText(parser);
            } else if (name.equals("modelNumber")) {
                result.modelNumber = readText(parser);
            } else if (name.equals("modelURL")) {
                result.modelURL = readText(parser);
            } else if (name.equals("manufacturer")) {
                result.manufacturer = readText(parser);
            } else if (name.equals("manufacturerURL")) {
                result.manufacturerURL = readText(parser);
            } else if (name.equals("UDN")) {
                result.UDN = readText(parser);
            } else {
                skip(parser);
            }
        }
        return result;
    }

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.next() == XmlPullParser.TEXT) {
            final String result = parser.getText();
            parser.nextTag();
            return result;
        }
        return "";
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private static class Device {
        public String deviceType;
        public String friendlyName;
        public String presentationURL;
        public String serialNumber;
        public String modelName;
        public String modelNumber;
        public String modelURL;
        public String manufacturer;
        public String manufacturerURL;
        public String UDN;
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
