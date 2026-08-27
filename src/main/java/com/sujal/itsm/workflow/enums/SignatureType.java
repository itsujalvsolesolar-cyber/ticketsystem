package com.sujal.itsm.workflow.enums;

public enum SignatureType {
    ELECTRONIC, // Standard click-to-approve (Name + Timestamp + IP)
    TYPED,
    DRAWN,      // Canvas-drawn signature (Base64)
    UPLOADED    // Uploaded image file
}