package com.swp.myleague.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public class CommonFunc {

    private static final String LOCATION_DIRECTORY = "src/main/resources/static/images/Storage-Files";

    public static UUID convertStringToUUID(String str) {
        return UUID.fromString(str);
    }

    public static String uploadFile(MultipartFile file) {
        File newFile = new File(LOCATION_DIRECTORY + File.separator + file.getOriginalFilename());
        try {

            Files.copy(file.getInputStream(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return "/images/Storage-Files/" + file.getOriginalFilename();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] genQRCode(String data) {
        String result = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data="
                + URLEncoder.encode(data, StandardCharsets.UTF_8);
        try {
            URL url = new URL(result);
            InputStream in = url.openStream();
            return in.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static <T> T parse(String str, Class<T> clazz) {
        try {
            if (!str.startsWith(clazz.getSimpleName() + "(") || !str.endsWith(")")) {
                throw new IllegalArgumentException("Invalid format for class: " + clazz.getSimpleName());
            }

            String content = str.substring(clazz.getSimpleName().length() + 1, str.length() - 1);
            Map<String, String> fieldMap = parseFields(content);

            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String valueStr = fieldMap.get(field.getName());

                if (valueStr == null || "null".equals(valueStr)) {
                    field.set(instance, null);
                } else if (valueStr.startsWith(field.getType().getSimpleName() + "(")) {
                    // Nested object
                    Object nestedObj = parse(valueStr, field.getType());
                    field.set(instance, nestedObj);
                } else if (field.getType().isEnum()) {
                    // Parse enum safely
                    Object enumVal = Enum.valueOf((Class<Enum>) field.getType(), valueStr);
                    field.set(instance, enumVal);
                } else {
                    Object value = convert(valueStr, field.getType());
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse object from toString: " + e.getMessage(), e);
        }
    }

    /**
     * Tách key=value trong content, giữ nguyên nested object.
     * Ví dụ: club=Club(...), playerPosition=FW, ...
     */
    private static Map<String, String> parseFields(String content) {
        Map<String, String> map = new HashMap<>();
        int depth = 0;
        StringBuilder sb = new StringBuilder();
        String currentKey = null;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '=' && depth == 0 && currentKey == null) {
                currentKey = sb.toString().trim();
                sb.setLength(0);
            } else if (c == ',' && depth == 0 && currentKey != null) {
                map.put(currentKey, sb.toString().trim());
                currentKey = null;
                sb.setLength(0);
            } else {
                if (c == '(')
                    depth++;
                else if (c == ')')
                    depth--;
                sb.append(c);
            }
        }
        if (currentKey != null) {
            map.put(currentKey, sb.toString().trim());
        }
        return map;
    }

    /**
     * Convert String value sang đúng kiểu.
     */
    private static Object convert(String valueStr, Class<?> targetType) {
        if (valueStr == null || "null".equals(valueStr)) {
            return null;
        }
        if (targetType == String.class) {
            return valueStr;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(valueStr);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(valueStr);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(valueStr);
        } else if (targetType == UUID.class) {
            return UUID.fromString(valueStr);
        } else if (targetType == LocalDateTime.class) {
            // parse ISO string hoặc format phù hợp (ví dụ: "2024-07-16T15:30:00")
            return LocalDateTime.parse(valueStr);
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + targetType.getName());
        }
    }

}
