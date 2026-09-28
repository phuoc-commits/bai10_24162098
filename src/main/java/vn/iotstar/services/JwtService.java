package vn.iotstar.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public long getExpirationTime() {
        return jwtExpiration;
    }

    // 1. Tạo JWT Token
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        try {
            // Tạo Header với thuật toán HS256
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

            // Xây dựng Payload / Claims
            Date now = new Date();
            Date expirationDate = new Date(now.getTime() + jwtExpiration);

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issueTime(now)
                    .expirationTime(expirationDate);

            // Thêm các custom claims nếu có
            extraClaims.forEach(claimsBuilder::claim);

            JWTClaimsSet claimsSet = claimsBuilder.build();

            // Ký Token bằng Secret Key (Secret key tối thiểu 256-bit / 32 bytes)
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            JWSSigner signer = new MACSigner(secretKey.getBytes());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo JWT token với Nimbus", e);
        }
    }

    // 2. Trích xuất Username (Subject) từ Token
    public String extractUsername(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            return null;
        }
    }

    // 3. Trích xuất Ngày hết hạn từ Token
    public Date extractExpiration(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            return null;
        }
    }

    // 4. Kiểm tra Token có hợp lệ hay không
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Kiểm tra Chữ ký (Signature)
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes());
            boolean isSignatureValid = signedJWT.verify(verifier);

            // Kiểm tra Username
            String username = signedJWT.getJWTClaimsSet().getSubject();
            boolean isUsernameEquals = username != null && username.equals(userDetails.getUsername());

            // Kiểm tra Hạn dùng
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean isNotExpired = expiration != null && expiration.after(new Date());

            return isSignatureValid && isUsernameEquals && isNotExpired;
        } catch (Exception e) {
            return false;
        }
    }
}