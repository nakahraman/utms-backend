package com.utms.backend.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {
    public void sendSms(String phoneNumber, String message) {
        // Burada gerçek bir SMS API'sine HTTP isteği atılacak
        System.out.println("SMS Gönderiliyor -> " + phoneNumber + " Mesaj: " + message);
    }
}
