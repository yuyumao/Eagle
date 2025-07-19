package com.eagle.pojo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserAddressDTO {
    @NotBlank
    private String line1;
    private String line2;
    private String line3;

    @NotBlank
    private String town;

    @NotBlank
    private String county;

    @NotBlank
    private String postcode;

    public UserAddressDTO(String line1, String line2, String line3,
                                    String town, String county, String postcode) {
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.town = town;
        this.county = county;
        this.postcode = postcode;
    }
}