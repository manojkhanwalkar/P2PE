package util;

import java.time.Instant;

/** A decrypted, signature-verified payload plus the nonce and signing time taken from its signed JWS header. */
public record VerifiedMessage(String payload, String nonce, Instant issuedAt) {
}
