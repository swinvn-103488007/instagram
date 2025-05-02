package com.engineerpro.example.redis.dto.oauth_profile;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class GoogleProfile extends OauthProfile {
  String name;
  String sub;
  String given_name;
  String family_name;
  String picture;
  String email;
  Boolean email_verified;
}
