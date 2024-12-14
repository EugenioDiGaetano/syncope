package org.apache.syncope.core.spring.security.utils;

public enum Algorithm {
    AES,
    BCRYPT,
    SMD5,
    SSHA256,
    SHA256,
    NOT_AN_ALGO,
    NULL
}