package com.ssaika.ssiren.domain.notification.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FcmPushService {

    private static final String FIREBASE_APP_NAME = "ssiren-fcm";

    private final Object initializationLock = new Object();

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    private volatile FirebaseMessaging firebaseMessaging;

    public FcmSendResult sendToToken(
        String token,
        String title,
        String body,
        Map<String, String> data) {
        Message message = Message.builder()
            .setToken(token)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .putAllData(data == null ? Map.of() : data)
            .build();

        try {
            String messageId = getFirebaseMessaging().send(message);
            return FcmSendResult.success(token, messageId);
        } catch (FirebaseMessagingException e) {
            return FcmSendResult.failure(token, isInvalidToken(e), e.getMessage());
        }
    }

    private FirebaseMessaging getFirebaseMessaging() {
        if (firebaseMessaging != null) {
            return firebaseMessaging;
        }

        synchronized (initializationLock) {
            if (firebaseMessaging == null) {
                firebaseMessaging = FirebaseMessaging.getInstance(initializeFirebaseApp());
            }
        }

        return firebaseMessaging;
    }

    private FirebaseApp initializeFirebaseApp() {
        return FirebaseApp.getApps().stream()
            .filter(app -> FIREBASE_APP_NAME.equals(app.getName()))
            .findFirst()
            .orElseGet(() -> FirebaseApp.initializeApp(buildFirebaseOptions(), FIREBASE_APP_NAME));
    }

    private FirebaseOptions buildFirebaseOptions() {
        try (InputStream credentialsStream = openCredentialsStream()) {
            return FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .build();
        } catch (IOException e) {
            throw new CustomException("Firebase 인증 정보를 불러올 수 없습니다.",
                ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private InputStream openCredentialsStream() throws IOException {
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        }
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            return new FileInputStream(serviceAccountPath);
        }

        throw new CustomException("Firebase 인증 정보가 설정되지 않았습니다.",
            ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private boolean isInvalidToken(FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.UNREGISTERED
            || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }

    public record FcmSendResult(
        String token,
        boolean success,
        boolean invalidToken,
        String messageId,
        String errorMessage
    ) {

        public static FcmSendResult success(String token, String messageId) {
            return new FcmSendResult(token, true, false, messageId, null);
        }

        public static FcmSendResult failure(
            String token,
            boolean invalidToken,
            String errorMessage) {
            return new FcmSendResult(token, false, invalidToken, null, errorMessage);
        }
    }
}
