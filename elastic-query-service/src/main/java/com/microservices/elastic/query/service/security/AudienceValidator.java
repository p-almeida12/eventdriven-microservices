package com.microservices.elastic.query.service.security;

import com.microservices.config.ElasticQueryServiceConfigData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Qualifier("elasticQueryServiceAudienceValidator")
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final ElasticQueryServiceConfigData elasticQueryServiceConfigData;

    /**
     * Validates the audience of the provided JWT token.
     * <p>
     * This method checks if the token's audience contains a specific value as specified
     * in the {@link ElasticQueryServiceConfigData}. If the required audience is present,
     * validation is successful; otherwise, it fails with an {@link OAuth2Error}.
     * </p>
     *
     * @param jwt The {@link Jwt} token to validate.
     * @return {@link OAuth2TokenValidatorResult#success()} if validation passes, otherwise
     *         {@link OAuth2TokenValidatorResult#failure(OAuth2Error)} with a specific error message.
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience().contains(elasticQueryServiceConfigData.getCustomAudience())) {
            return OAuth2TokenValidatorResult.success();
        } else {
            OAuth2Error audienceError =
                    new OAuth2Error("invalid_token", "The required audience " +
                            elasticQueryServiceConfigData.getCustomAudience() + " is missing.",
                            null);
            return OAuth2TokenValidatorResult.failure(audienceError);
        }
    }

}
