package io.github.spl21.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DeliveryFormDto {

    @NotBlank(message = "Delivery address cannot be empty.")
    private String address;

    @NotBlank(message = "Phone number cannot be empty.")
    @Pattern(regexp = "[0-9]{10}", message = "Please provide a valid 10-digit mobile number.")
    private String phone;

    public DeliveryFormDto() {
    }

}
