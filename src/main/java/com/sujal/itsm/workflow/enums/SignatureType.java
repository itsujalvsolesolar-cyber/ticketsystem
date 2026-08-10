package com.sujal.itsm.workflow.enums;

public enum SignatureType {
    ELECTRONIC, // Standard click-to-approve (Name + Timestamp + IP)
    DRAWN,      // Canvas-drawn signature (Base64)
    UPLOADED    // Uploaded image file
}